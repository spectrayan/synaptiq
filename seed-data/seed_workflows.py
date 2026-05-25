"""
Synaptiq Seed: Workflows
=========================
Seeds workflow definitions (FlowSettings specs) into MongoDB.

Currently seeds the Spectrayan Health ABA multi-agent goal generation workflow.

Usage:
    python seed-data/seed_workflows.py
"""
import asyncio
import json
import logging
import os
from datetime import datetime, timezone
from pathlib import Path

from motor.motor_asyncio import AsyncIOMotorClient

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

MONGODB_URI = os.environ.get("MONGODB_URI", "mongodb://localhost:27017/?directConnection=true")
DB_NAME = "synaptiq"
TENANT_ID = "demo-tenant"
APP_ID = "demo"

DATA_DIR = Path(__file__).parent / "data"


def load_json(relative_path: str):
    """Load a JSON file from the data directory."""
    filepath = DATA_DIR / relative_path
    with open(filepath, "r", encoding="utf-8") as f:
        return json.load(f)


async def seed_workflows():
    """Seed workflow definitions into the workflows collection."""
    client = AsyncIOMotorClient(MONGODB_URI)
    db = client[DB_NAME]

    logger.info("🔄 Seeding workflows for tenant '%s'...", TENANT_ID)

    now = datetime.now(timezone.utc)
    workflows_coll = db["workflows"]

    # ─── ABA Goal Generation Workflow ────────────────────────────────
    aba_spec = load_json("workflows/spectrayan-health/aba-goal-generation.json")

    await workflows_coll.update_one(
        {"tenantId": TENANT_ID, "spec.id": "aba-goal-generation"},
        {"$set": {
            "tenantId": TENANT_ID,
            "appId": APP_ID,
            "spec": aba_spec,
            "shareToken": None,
            "isPublic": False,
            "name": aba_spec["name"],
            "description": "Multi-agent workflow for generating comprehensive 12-month ABA treatment plans. "
                           "A supervisor agent orchestrates 4 specialist agents (ABA, Speech Therapy, "
                           "Occupational Therapy, CBT) to create evidence-based goals.",
            "tags": ["spectrayan-health", "aba", "multi-agent", "goal-generation"],
            "createdAt": now,
            "updatedAt": now,
        }},
        upsert=True,
    )
    logger.info("  ✅ Upserted workflow: %s", aba_spec["name"])

    # ─── Seed Client Profiles as Test Fixtures ───────────────────────
    profiles_dir = DATA_DIR / "workflows" / "spectrayan-health" / "client-profiles"
    profiles_coll = db["workflow_test_fixtures"]
    await profiles_coll.delete_many({"tenant_id": TENANT_ID, "workflow_id": "aba-goal-generation"})

    profile_files = sorted(profiles_dir.glob("*.json"))
    profile_docs = []
    for pf in profile_files:
        with open(pf, "r", encoding="utf-8") as f:
            profile = json.load(f)
        profile_docs.append({
            "tenant_id": TENANT_ID,
            "workflow_id": "aba-goal-generation",
            "fixture_name": profile.get("profileName", pf.stem),
            "client_id": profile.get("clientId", pf.stem),
            "input_data": profile,
            "created_at": now,
        })

    if profile_docs:
        await profiles_coll.insert_many(profile_docs)
        logger.info("  ✅ Inserted %d client profile test fixtures", len(profile_docs))

    logger.info("🎉 Workflow seeding complete!")
    client.close()


if __name__ == "__main__":
    asyncio.run(seed_workflows())
