from __future__ import annotations
"""Workflow Service — generates and executes AI agent workflows from natural language."""
import logging

import json
import time
import uuid
from typing import Any, AsyncIterator

from pydantic import BaseModel, Field

from synaptiq_api.core.mongodb import get_database


# ── Models ────────────────────────────────────────────────────────────────────

class AgentNodeSpec(BaseModel):
    """Specification for a single agent node in the workflow."""
    id: str
    type: str = "agent"  # agent | tool | function | conditional | parallel
    label: str
    description: str = ""
    system_prompt: str = ""
    llm: dict[str, str] | None = None
    tools: list[str] = Field(default_factory=list)
    position: dict[str, float] | None = None


class EdgeSpec(BaseModel):
    """Edge connecting two nodes in the workflow."""
    source: str = Field(alias="from", default="")
    target: str = Field(alias="to", default="")
    condition: str = "always"
    label: str = ""

    model_config = {"populate_by_name": True}


class WorkflowSpec(BaseModel):
    """Complete workflow specification — the frontend contract."""
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    name: str = ""
    description: str = ""
    flow_type: str = "static"  # static | dynamic | hybrid
    agents: list[AgentNodeSpec] = Field(default_factory=list)
    edges: list[EdgeSpec] = Field(default_factory=list)
    conditional_edges: list[dict[str, Any]] = Field(default_factory=list)
    entry_point: str = ""
    metadata: dict[str, Any] = Field(default_factory=dict)
    created_at: float = Field(default_factory=time.time)


class SSEEvent(BaseModel):
    """Server-Sent Event payload."""
    event: str  # status | spec | text | error | done
    data: Any


# ── Starter Templates ────────────────────────────────────────────────────────

WORKFLOW_TEMPLATES: list[dict[str, Any]] = [
    {
        "id": "research-writer",
        "name": "Research & Write",
        "description": "Research a topic using web search, then write a polished article.",
        "flow_type": "static",
        "agents": [
            {
                "id": "researcher",
                "type": "agent",
                "label": "Researcher",
                "description": "Searches the web and gathers information on a topic",
                "system_prompt": "You are a thorough researcher. Search for comprehensive information on the given topic. Provide detailed findings with sources.",
                "llm": {"provider": "google", "model": "gemini-2.5-flash"},
                "tools": ["web_search", "url_reader"],
            },
            {
                "id": "writer",
                "type": "agent",
                "label": "Writer",
                "description": "Writes polished content based on research findings",
                "system_prompt": "You are a skilled writer. Using the research provided, write a clear, engaging, and well-structured article.",
                "llm": {"provider": "google", "model": "gemini-2.5-flash"},
                "tools": [],
            },
        ],
        "edges": [
            {"from": "researcher", "to": "writer", "condition": "always", "label": "research complete"},
            {"from": "writer", "to": "END", "condition": "always", "label": "article ready"},
        ],
        "entry_point": "researcher",
    },
    {
        "id": "feedback-analyzer",
        "name": "Feedback Analyzer",
        "description": "Categorize customer feedback, analyze sentiment, and generate an actionable report.",
        "flow_type": "static",
        "agents": [
            {
                "id": "categorizer",
                "type": "agent",
                "label": "Categorizer",
                "description": "Categorizes feedback into themes (bugs, features, praise, complaints)",
                "system_prompt": "You are a feedback categorization expert. Analyze each piece of feedback and assign it to one or more categories: bug, feature_request, praise, complaint, question, other.",
                "llm": {"provider": "google", "model": "gemini-2.5-flash"},
                "tools": [],
            },
            {
                "id": "sentiment_analyzer",
                "type": "agent",
                "label": "Sentiment Analyzer",
                "description": "Analyzes sentiment and urgency of categorized feedback",
                "system_prompt": "You are a sentiment analysis expert. For each piece of feedback, determine the sentiment (positive, neutral, negative) and urgency level (low, medium, high, critical).",
                "llm": {"provider": "google", "model": "gemini-2.5-flash"},
                "tools": [],
            },
            {
                "id": "report_generator",
                "type": "agent",
                "label": "Report Generator",
                "description": "Generates an actionable summary report",
                "system_prompt": "You are a business analyst. Using the categorized and sentiment-analyzed feedback, generate a concise executive report with key insights, trends, and recommended actions.",
                "llm": {"provider": "google", "model": "gemini-2.5-flash"},
                "tools": [],
            },
        ],
        "edges": [
            {"from": "categorizer", "to": "sentiment_analyzer", "condition": "always", "label": "categorized"},
            {"from": "sentiment_analyzer", "to": "report_generator", "condition": "always", "label": "analyzed"},
            {"from": "report_generator", "to": "END", "condition": "always", "label": "report ready"},
        ],
        "entry_point": "categorizer",
    },
    {
        "id": "support-triage",
        "name": "Support Ticket Triage",
        "description": "Multi-agent system to triage, route, and draft responses for support tickets.",
        "flow_type": "dynamic",
        "agents": [
            {
                "id": "triage_agent",
                "type": "agent",
                "label": "Triage Agent",
                "description": "Classifies ticket priority and routes to the right specialist",
                "system_prompt": "You are a support triage specialist. Classify the incoming ticket by priority (P0-P3) and category (billing, technical, account, general). Route to the appropriate specialist.",
                "llm": {"provider": "google", "model": "gemini-2.5-flash"},
                "tools": [],
            },
            {
                "id": "technical_agent",
                "type": "agent",
                "label": "Technical Support",
                "description": "Handles technical issues and drafts responses",
                "system_prompt": "You are a technical support specialist. Analyze the technical issue, suggest troubleshooting steps, and draft a helpful response.",
                "llm": {"provider": "google", "model": "gemini-2.5-flash"},
                "tools": ["knowledge_base_search"],
            },
            {
                "id": "billing_agent",
                "type": "agent",
                "label": "Billing Support",
                "description": "Handles billing and account inquiries",
                "system_prompt": "You are a billing support specialist. Address billing concerns, explain charges, and draft a clear response.",
                "llm": {"provider": "google", "model": "gemini-2.5-flash"},
                "tools": [],
            },
        ],
        "edges": [
            {"from": "technical_agent", "to": "END", "condition": "always", "label": "resolved"},
            {"from": "billing_agent", "to": "END", "condition": "always", "label": "resolved"},
        ],
        "conditional_edges": [
            {
                "from": "triage_agent",
                "condition_mapping": {
                    "technical": "technical_agent",
                    "billing": "billing_agent",
                },
                "default_target": "technical_agent",
            },
        ],
        "entry_point": "triage_agent",
    },
]


# ── Generation Prompt ─────────────────────────────────────────────────────────

WORKFLOW_GENERATION_SYSTEM_PROMPT = """You are an expert AI workflow architect. The user describes a task or process, and you generate a precise workflow specification in JSON format.

Your output MUST be a valid JSON object matching this structure:
{
  "name": "Workflow Name",
  "description": "Brief description of what the workflow does",
  "flow_type": "static",
  "agents": [
    {
      "id": "unique_snake_case_id",
      "type": "agent",
      "label": "Human Readable Name",
      "description": "What this agent does",
      "system_prompt": "Detailed instructions for this agent's behavior",
      "llm": {"provider": "google", "model": "gemini-2.5-flash"},
      "tools": []
    }
  ],
  "edges": [
    {"from": "source_id", "to": "target_id", "condition": "always", "label": "edge description"}
  ],
  "conditional_edges": [],
  "entry_point": "first_agent_id"
}

Rules:
1. Each agent must have a unique id (snake_case), a clear label, and a detailed system_prompt
2. Edges connect agents. Use "END" as the target for the final edge
3. For conditional routing, use conditional_edges with condition_mapping
4. Keep workflows focused: 2-5 agents is typical
5. Choose appropriate agent types: "agent" for LLM-based, "tool" for tool execution, "function" for pure logic
6. The entry_point must be the id of the first agent to execute
7. Always include at least one edge to "END"

Output ONLY the JSON object, no markdown fencing or extra text."""


# ── Prompt Regeneration System Prompt ────────────────────────────────────────

PROMPT_REGENERATION_SYSTEM_PROMPT = """You are an expert prompt engineer specializing in AI agent system prompts.

Your task is to IMPROVE a system prompt for an agent node in a workflow graph.

You will receive:
- The agent's label, description, and role in the workflow
- The current system prompt
- An optional user instruction for how to improve it
- Context about the overall workflow

Guidelines for improvement:
1. Be specific and actionable — avoid vague instructions
2. Include clear output format expectations
3. Add error handling / edge case instructions where appropriate
4. Use structured formatting (numbered steps, headers) for complex prompts
5. Keep the persona and tone appropriate for the agent's role
6. Preserve any domain-specific terminology from the original prompt
7. If the user asked for a specific change, focus on that

Output ONLY the improved system prompt text. No explanations, no markdown fencing, no JSON — just the raw prompt text."""


# ── Service ───────────────────────────────────────────────────────────────────

class WorkflowService:
    """Service for generating, storing, and executing AI agent workflows."""

    @staticmethod
    async def generate_workflow(prompt: str, tenant_id: str) -> AsyncIterator[SSEEvent]:
        """Generate a workflow specification from natural language via LLM.

        Yields SSE events: status → spec → text → done
        """
        # 1. Yield initial status
        yield SSEEvent(event="status", data="Analyzing your workflow requirements...")
        
        try:
            # 2. Call LLM to generate the workflow spec
            from synaptiq_api.services.llm_provider import get_langchain_model
            from synaptiq_api.core.mongodb import get_database

            db = await get_database()
            tenant_doc = await db["tenants"].find_one({"tenant_id": tenant_id}) or {}
            llm_config = tenant_doc.get("llm_provider", {})
            byok_key = tenant_doc.get("byok_key", "")

            llm = get_langchain_model(llm_config, byok_key)

            yield SSEEvent(event="status", data="Designing agent roles and connections...")

            messages = [
                {"role": "system", "content": WORKFLOW_GENERATION_SYSTEM_PROMPT},
                {"role": "user", "content": f"Design a workflow for: {prompt}"},
            ]

            response = await llm.ainvoke(messages)
            content = response.content if hasattr(response, "content") else str(response)

            # 3. Parse the JSON from the response
            yield SSEEvent(event="status", data="Building workflow graph...")

            spec_dict = _extract_json(content)
            if not spec_dict:
                yield SSEEvent(event="error", data="Failed to generate a valid workflow specification. Please try again with more detail.")
                return

            # Add generated fields
            spec_dict["id"] = str(uuid.uuid4())
            spec_dict.setdefault("flow_type", "static")
            spec_dict["metadata"] = {
                "generated_from": prompt,
                "generated_at": time.time(),
                "tenant_id": tenant_id,
            }

            # Validate with our model
            spec = WorkflowSpec(**spec_dict)

            # Auto-save the generated workflow to MongoDB
            try:
                await WorkflowService.save_workflow(
                    spec.model_dump(by_alias=True), tenant_id,
                )
                logging.getLogger(__name__).info(
                    "Auto-saved generated workflow '%s' (%s)", spec.name, spec.id,
                )
            except Exception as save_err:
                logging.getLogger(__name__).warning(
                    "Failed to auto-save workflow: %s", save_err,
                )

            # 4. Yield the workflow spec as a component
            yield SSEEvent(event="component", data={
                "type": "workflow",
                "spec": spec.model_dump(by_alias=True),
            })

            # 5. Yield summary text
            agent_count = len(spec.agents)
            edge_count = len(spec.edges) + len(spec.conditional_edges)
            agent_names = ", ".join(a.label for a in spec.agents)
            summary = (
                f"I've designed a **{spec.name}** workflow with "
                f"**{agent_count} agents** ({agent_names}) and "
                f"**{edge_count} connections**.\n\n"
                f"_{spec.description}_\n\n"
                f"Click **View Workflow** below to see the interactive graph, "
                f"or ask me to modify it."
            )
            yield SSEEvent(event="text", data=summary)
            yield SSEEvent(event="done", data=None)

        except Exception as e:
            yield SSEEvent(event="error", data=f"Workflow generation failed: {e!s}")

    @staticmethod
    async def get_templates() -> list[dict[str, Any]]:
        """Return starter workflow templates."""
        return WORKFLOW_TEMPLATES

    @staticmethod
    async def save_workflow(spec: dict[str, Any], tenant_id: str) -> str:
        """Persist a workflow specification to MongoDB."""
        db = await get_database()
        collection = db["workflows"]
        
        spec["tenant_id"] = tenant_id
        spec["updated_at"] = time.time()
        spec.setdefault("created_at", time.time())

        # Upsert by workflow id
        await collection.update_one(
            {"id": spec["id"], "tenant_id": tenant_id},
            {"$set": spec},
            upsert=True,
        )
        return spec["id"]

    @staticmethod
    async def get_workflow(workflow_id: str, tenant_id: str) -> dict[str, Any] | None:
        """Retrieve a saved workflow by ID."""
        db = await get_database()
        collection = db["workflows"]
        doc = await collection.find_one(
            {"id": workflow_id, "tenant_id": tenant_id},
            {"_id": 0},
        )
        return doc

    @staticmethod
    async def list_workflows(tenant_id: str, limit: int = 20) -> list[dict[str, Any]]:
        """List saved workflows for a tenant."""
        db = await get_database()
        collection = db["workflows"]
        cursor = collection.find(
            {"tenant_id": tenant_id},
            {"_id": 0, "id": 1, "name": 1, "description": 1, "flow_type": 1, "created_at": 1, "updated_at": 1},
        ).sort("updated_at", -1).limit(limit)
        return await cursor.to_list(length=limit)

    @staticmethod
    async def update_workflow(
        workflow_id: str, spec_updates: dict[str, Any], tenant_id: str,
    ) -> float:
        """Update an existing workflow specification (partial or full)."""
        logger = logging.getLogger(__name__)
        db = await get_database()
        now = time.time()
        spec_updates["updated_at"] = now
        spec_updates.pop("_id", None)  # Never write _id

        result = await db["workflows"].update_one(
            {"id": workflow_id, "tenant_id": tenant_id},
            {"$set": spec_updates},
        )
        if result.matched_count == 0:
            raise ValueError(f"Workflow {workflow_id} not found for tenant {tenant_id}")
        logger.info("[workflow_service] updated workflow %s (modified=%d)", workflow_id, result.modified_count)
        return now

    @staticmethod
    async def delete_workflow(workflow_id: str, tenant_id: str) -> None:
        """Delete a workflow and all associated execution runs."""
        logger = logging.getLogger(__name__)
        db = await get_database()
        result = await db["workflows"].delete_one({"id": workflow_id, "tenant_id": tenant_id})
        if result.deleted_count == 0:
            raise ValueError(f"Workflow {workflow_id} not found for tenant {tenant_id}")
        # Cascade-delete associated runs
        run_result = await db["workflow_runs"].delete_many({"workflow_id": workflow_id, "tenant_id": tenant_id})
        logger.info(
            "[workflow_service] deleted workflow %s and %d associated runs",
            workflow_id, run_result.deleted_count,
        )

    @staticmethod
    async def duplicate_workflow(workflow_id: str, tenant_id: str) -> str:
        """Duplicate an existing workflow with a new ID and name."""
        logger = logging.getLogger(__name__)
        db = await get_database()
        original = await db["workflows"].find_one(
            {"id": workflow_id, "tenant_id": tenant_id},
            {"_id": 0},
        )
        if not original:
            raise ValueError(f"Workflow {workflow_id} not found for tenant {tenant_id}")

        new_id = str(uuid.uuid4())
        now = time.time()
        clone = {**original, "id": new_id, "name": f"{original.get('name', 'Workflow')} (Copy)", "created_at": now, "updated_at": now}
        await db["workflows"].insert_one(clone)
        logger.info("[workflow_service] duplicated workflow %s → %s", workflow_id, new_id)
        return new_id

    @staticmethod
    async def list_workflow_runs(
        workflow_id: str, tenant_id: str, limit: int = 20,
    ) -> list[dict[str, Any]]:
        """List past execution runs for a workflow (most recent first)."""
        db = await get_database()
        cursor = db["workflow_runs"].find(
            {"workflow_id": workflow_id, "tenant_id": tenant_id},
            {
                "_id": 0,
                "run_id": 1, "status": 1, "dry_run": 1,
                "started_at": 1, "completed_at": 1,
                "total_duration_ms": 1,
                "workflow_name": 1,
            },
        ).sort("started_at", -1).limit(limit)
        return await cursor.to_list(length=limit)

    @staticmethod
    async def get_workflow_run(
        run_id: str, tenant_id: str,
    ) -> dict[str, Any] | None:
        """Retrieve a full execution run by run_id (includes all node outputs)."""
        db = await get_database()
        return await db["workflow_runs"].find_one(
            {"run_id": run_id, "tenant_id": tenant_id},
            {"_id": 0},
        )

    @staticmethod
    async def regenerate_prompt(
        *,
        node_id: str,
        node_label: str,
        node_description: str,
        current_prompt: str,
        instruction: str,
        workflow_context: dict[str, Any],
        tenant_id: str,
    ) -> str:
        """Use an LLM to improve a node's system prompt based on context."""
        db = await get_database()
        tenant_doc = await db["tenants"].find_one({"tenant_id": tenant_id}) or {}
        llm_config = tenant_doc.get("llm_provider", {})
        byok_key = tenant_doc.get("byok_key", "")

        llm = get_langchain_model(llm_config, byok_key)

        # Build context for the LLM
        workflow_name = workflow_context.get("name", "Unknown Workflow")
        workflow_desc = workflow_context.get("description", "")
        agents_summary = []
        for agent in workflow_context.get("agents", []):
            agents_summary.append(f"- {agent.get('label', agent.get('id', '?'))}: {agent.get('description', '')}")
        agents_str = "\n".join(agents_summary) if agents_summary else "No other agents."

        user_message = f"""## Agent to Improve
- **ID**: {node_id}
- **Label**: {node_label}
- **Description**: {node_description}

## Current System Prompt
{current_prompt or "(empty — generate from scratch)"}

## Workflow Context
- **Workflow**: {workflow_name}
- **Description**: {workflow_desc}
- **All agents in this workflow**:
{agents_str}

## User Instruction
{instruction or "Improve the prompt: make it clearer, more specific, and more effective."}

Now output ONLY the improved system prompt:"""

        messages = [
            {"role": "system", "content": PROMPT_REGENERATION_SYSTEM_PROMPT},
            {"role": "user", "content": user_message},
        ]

        response = await llm.ainvoke(messages)
        content = response.content if hasattr(response, "content") else str(response)

        # Strip any accidental markdown fencing
        import re
        content = re.sub(r"^```(?:text|markdown)?\s*\n?", "", content.strip())
        content = re.sub(r"\n?```\s*$", "", content.strip())

        logger.info(
            "[workflow] regenerate_prompt: improved prompt for node=%s (%d → %d chars)",
            node_id, len(current_prompt), len(content),
        )
        return content.strip()


def _extract_json(text: str) -> dict[str, Any] | None:
    """Extract a JSON object from LLM text output (handles markdown fencing)."""
    import re
    
    # Strip markdown code fences
    text = re.sub(r"```json\s*", "", text)
    text = re.sub(r"```\s*$", "", text.strip())
    
    # Try direct parse first
    try:
        return json.loads(text.strip())
    except json.JSONDecodeError:
        pass

    # Try to find JSON object boundaries
    start = text.find("{")
    end = text.rfind("}") + 1
    if start >= 0 and end > start:
        try:
            return json.loads(text[start:end])
        except json.JSONDecodeError:
            pass

    return None
