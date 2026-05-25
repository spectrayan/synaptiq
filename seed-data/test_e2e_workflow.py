#!/usr/bin/env python3
"""
Synaptiq E2E Workflow Test — ABA Goal Generation
=================================================
End-to-end test that:
1. Authenticates with built-in auth
2. Seeds the ABA multi-agent workflow via REST API
3. Executes the workflow with a sample client profile
4. Streams SSE events and captures output
5. Validates results and saves them

Usage:
    python seed-data/test_e2e_workflow.py [--profile early-learner|school-age-social|...]

Requires: Backend running at http://localhost:8080
"""
import argparse
import json
import logging
import os
import sys
import time
from datetime import datetime
from pathlib import Path

import requests

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%H:%M:%S",
)
logger = logging.getLogger(__name__)

BASE_URL = os.environ.get("SYNAPTIQ_API_URL", "http://localhost:8080")
TENANT_ID = "demo-tenant"

DATA_DIR = Path(__file__).parent / "data"
RESULTS_DIR = Path(__file__).parent / "results"


def load_json(relative_path: str):
    filepath = DATA_DIR / relative_path
    with open(filepath, "r", encoding="utf-8") as f:
        return json.load(f)


def sanitize_spec_for_api(spec: dict) -> dict:
    """Strip fields not recognized by the REST API's FlowSettingsSpec DTO.

    The JSON on disk is the 'full' spec (used by the seed scripts and the
    internal FlowSettings model). The REST API DTO is a strict subset.
    """
    # Top-level allowed keys in FlowSettingsSpec
    allowed_top = {"id", "name", "description", "entrypoint", "agents", "edges", "mcpServers", "policy", "flowType"}
    clean = {k: v for k, v in spec.items() if k in allowed_top}

    # Policy → ExecutionPolicySpec (only certain nested shapes)
    if "policy" in clean and isinstance(clean["policy"], dict):
        pol = clean["policy"]
        api_policy = {}
        if "resources" in pol:
            api_policy["resources"] = pol["resources"]
        if "parallel" in pol:
            api_policy["parallel"] = pol["parallel"]
        clean["policy"] = api_policy if api_policy else None

    return clean


class SynaptiqClient:
    """Minimal REST client for the Synaptiq API."""

    def __init__(self, base_url: str, tenant_id: str):
        self.base_url = base_url.rstrip("/")
        self.tenant_id = tenant_id
        self.token = None
        self.session = requests.Session()

    def _headers(self):
        headers = {
            "Content-Type": "application/json",
            "X-Tenant-ID": self.tenant_id,
        }
        if self.token:
            headers["Authorization"] = f"Bearer {self.token}"
        return headers

    def login(self, email: str = "admin@synaptiq.local", password: str = "admin1234"):
        """Authenticate and store JWT token."""
        logger.info("🔐 Logging in as %s...", email)
        resp = self.session.post(
            f"{self.base_url}/api/v1/auth/login",
            json={"email": email, "password": password},
        )
        if resp.status_code == 200:
            data = resp.json()
            self.token = data.get("idToken") or data.get("token") or data.get("access_token")
            logger.info("  ✅ Authenticated (token: %s...)", self.token[:20] if self.token else "N/A")
            return True
        else:
            logger.error("  ❌ Login failed: %d %s", resp.status_code, resp.text[:200])
            return False

    def save_workflow(self, spec: dict) -> dict:
        """Save a workflow via POST /api/v1/workflow/save."""
        logger.info("💾 Saving workflow: %s", spec.get("name", spec.get("id", "unknown")))
        resp = self.session.post(
            f"{self.base_url}/api/v1/workflow/save",
            headers=self._headers(),
            json={"spec": spec},
        )
        if resp.status_code in (200, 201):
            data = resp.json()
            logger.info("  ✅ Workflow saved (id: %s)", data.get("id", "?"))
            return data
        else:
            logger.error("  ❌ Save failed: %d %s", resp.status_code, resp.text[:500])
            return {}

    def list_workflows(self, limit: int = 20) -> list:
        """List workflows via GET /api/v1/workflow/list."""
        resp = self.session.get(
            f"{self.base_url}/api/v1/workflow/list",
            headers=self._headers(),
            params={"limit": limit},
        )
        if resp.status_code == 200:
            data = resp.json()
            return data.get("workflows", data.get("items", []))
        return []

    def execute_workflow(self, spec: dict, input_text: str) -> list:
        """Execute a workflow and collect SSE events."""
        logger.info("🚀 Executing workflow: %s", spec.get("name", "?"))
        logger.info("   Input length: %d chars", len(input_text))

        events = []
        start_time = time.time()

        try:
            resp = self.session.post(
                f"{self.base_url}/api/v1/workflow/execute",
                headers={**self._headers(), "Accept": "text/event-stream"},
                json={"spec": spec, "inputText": input_text},
                stream=True,
                timeout=300,  # 5 minute timeout
            )

            if resp.status_code != 200:
                logger.error("  ❌ Execute failed: %d %s", resp.status_code, resp.text[:500])
                return events

            logger.info("  📡 Streaming SSE events...")
            current_event_type = None
            current_data = ""

            for line in resp.iter_lines(decode_unicode=True):
                if line is None:
                    continue
                line = line.strip() if isinstance(line, str) else line.decode("utf-8").strip()

                if line.startswith("event:"):
                    current_event_type = line[6:].strip()
                elif line.startswith("data:"):
                    current_data = line[5:].strip()
                elif line == "" and current_event_type:
                    # End of SSE event
                    elapsed = round(time.time() - start_time, 1)
                    event = {
                        "type": current_event_type,
                        "data": current_data,
                        "elapsed_s": elapsed,
                    }
                    events.append(event)

                    # Log key events
                    if current_event_type == "execution_start":
                        logger.info("  ▶ [%ss] Run started", elapsed)
                    elif current_event_type == "node_complete":
                        try:
                            node_data = json.loads(current_data)
                            logger.info("  ✓ [%ss] Agent completed: %s", elapsed, node_data.get("label", "?"))
                        except json.JSONDecodeError:
                            logger.info("  ✓ [%ss] Step completed", elapsed)
                    elif current_event_type == "text":
                        # Token delta — just count, don't log each one
                        pass
                    elif current_event_type == "execution_complete":
                        logger.info("  🏁 [%ss] Execution complete!", elapsed)
                    elif current_event_type == "execution_error":
                        logger.error("  ❌ [%ss] Execution error: %s", elapsed, current_data[:200])

                    current_event_type = None
                    current_data = ""

        except requests.exceptions.Timeout:
            logger.error("  ⏰ Request timed out after 300s")
        except requests.exceptions.ConnectionError as e:
            logger.error("  🔌 Connection error: %s", str(e)[:200])
        except Exception as e:
            logger.error("  ❌ Unexpected error: %s", str(e)[:200])

        total_time = round(time.time() - start_time, 1)
        logger.info("  📊 Total events: %d, Total time: %ss", len(events), total_time)
        return events


def save_results(profile_name: str, events: list, workflow_spec: dict, client_profile: dict):
    """Save execution results to disk."""
    RESULTS_DIR.mkdir(parents=True, exist_ok=True)

    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    result_file = RESULTS_DIR / f"run_{profile_name}_{timestamp}.json"

    # Extract text content from token deltas
    full_text = ""
    for event in events:
        if event["type"] == "text":
            try:
                data = json.loads(event["data"])
                full_text += data.get("delta", "")
            except (json.JSONDecodeError, TypeError):
                full_text += event["data"]

    # Extract step completions
    steps = []
    for event in events:
        if event["type"] in ("node_complete", "execution_start", "execution_complete"):
            try:
                steps.append({"type": event["type"], "data": json.loads(event["data"]), "elapsed_s": event["elapsed_s"]})
            except (json.JSONDecodeError, TypeError):
                steps.append({"type": event["type"], "data": event["data"], "elapsed_s": event["elapsed_s"]})

    result = {
        "profile_name": profile_name,
        "timestamp": timestamp,
        "workflow_id": workflow_spec.get("id"),
        "workflow_name": workflow_spec.get("name"),
        "client_id": client_profile.get("clientId"),
        "total_events": len(events),
        "total_time_s": events[-1]["elapsed_s"] if events else 0,
        "steps": steps,
        "generated_text": full_text,
        "raw_events": events,
    }

    with open(result_file, "w", encoding="utf-8") as f:
        json.dump(result, f, indent=2, ensure_ascii=False)

    logger.info("💾 Results saved to: %s", result_file)
    return result_file


def main():
    parser = argparse.ArgumentParser(description="E2E Workflow Test — ABA Goal Generation")
    parser.add_argument(
        "--profile",
        default="early-learner",
        choices=["early-learner", "school-age-social", "adolescent-adaptive",
                 "preschooler-behavior", "young-adult-vocational"],
        help="Client profile to use (default: early-learner)",
    )
    parser.add_argument("--base-url", default=BASE_URL, help="API base URL")
    parser.add_argument("--skip-login", action="store_true", help="Skip authentication")
    args = parser.parse_args()

    logger.info("=" * 70)
    logger.info("🧪 Synaptiq E2E Workflow Test — ABA Goal Generation")
    logger.info("=" * 70)
    logger.info("  API:     %s", args.base_url)
    logger.info("  Profile: %s", args.profile)
    logger.info("")

    # 1. Load workflow spec and client profile
    logger.info("📂 Loading workflow spec and client profile...")
    workflow_spec = load_json("workflows/spectrayan-health/aba-goal-generation.json")
    client_profile = load_json(f"workflows/spectrayan-health/client-profiles/{args.profile}.json")
    logger.info("  ✅ Loaded workflow: %s", workflow_spec["name"])
    logger.info("  ✅ Loaded profile: %s", client_profile["profileName"])
    logger.info("")

    # 2. Create API client and authenticate
    api = SynaptiqClient(args.base_url, TENANT_ID)

    if not args.skip_login:
        if not api.login():
            logger.error("❌ Authentication failed. Is the backend running?")
            sys.exit(1)
        logger.info("")

    # 3. Sanitize spec for REST API (strip internal-only fields) and save
    api_spec = sanitize_spec_for_api(workflow_spec)
    saved = api.save_workflow(api_spec)
    if saved:
        # Use the saved workflow's MongoDB ID for execution
        saved_id = saved.get("id")
        if saved_id:
            api_spec["id"] = saved_id
            logger.info("  📌 Will execute with saved workflow ID: %s", saved_id)
    else:
        logger.warning("⚠️  Workflow save may have failed — continuing with inline spec execution")
    logger.info("")

    # 4. List workflows to verify
    workflows = api.list_workflows()
    logger.info("📋 Workflows in system: %d", len(workflows))
    for w in workflows:
        logger.info("   - %s (id: %s)", w.get("name", w.get("spec", {}).get("name", "?")), w.get("id", "?"))
    logger.info("")

    # 5. Execute workflow with client profile
    input_text = json.dumps(client_profile, indent=2)
    events = api.execute_workflow(api_spec, input_text)
    logger.info("")

    # 6. Save results
    if events:
        result_file = save_results(args.profile, events, workflow_spec, client_profile)
        logger.info("")

        # 7. Summary
        text_events = [e for e in events if e["type"] == "text"]
        step_events = [e for e in events if e["type"] == "node_complete"]
        error_events = [e for e in events if e["type"] == "execution_error"]

        logger.info("=" * 70)
        logger.info("📊 Execution Summary")
        logger.info("=" * 70)
        logger.info("  Profile:         %s", client_profile["profileName"])
        logger.info("  Total events:    %d", len(events))
        logger.info("  Text deltas:     %d", len(text_events))
        logger.info("  Steps completed: %d", len(step_events))
        logger.info("  Errors:          %d", len(error_events))
        if events:
            logger.info("  Total time:      %ss", events[-1]["elapsed_s"])
        logger.info("  Results file:    %s", result_file)

        if error_events:
            logger.warning("")
            logger.warning("⚠️  Errors detected during execution:")
            for e in error_events:
                logger.warning("   %s", e["data"][:200])

        logger.info("")
        logger.info("✅ E2E test complete!")
    else:
        logger.error("❌ No events received — execution may have failed")
        logger.error("   Check backend logs: tail -f /tmp/synaptiq-api.log")
        sys.exit(1)


if __name__ == "__main__":
    main()
