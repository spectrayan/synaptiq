"""
Synaptiq Seed: Schema Registry
===============================
Registers all data collections (business + observability) in the schema registry
so the LLM's query_collection tool can discover and query them.

All schema definitions are loaded from JSON files under seed-data/data/schemas/.

Usage:
    python seed-data/seed_schema_registry.py
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

DATA_DIR = Path(__file__).parent / "data"


def load_json(relative_path: str):
    """Load a JSON file from the data directory."""
    filepath = DATA_DIR / relative_path
    with open(filepath, "r", encoding="utf-8") as f:
        return json.load(f)


async def seed_schema_registry():
    """Register all collection schemas."""
    client = AsyncIOMotorClient(MONGODB_URI)
    db = client[DB_NAME]

    logger.info("📋 Seeding schema registry for tenant '%s'...", TENANT_ID)

    # Load schemas from JSON files
    business_schemas = load_json("schemas/business.json")
    observability_schemas = load_json("schemas/observability.json")
    all_schemas = business_schemas + observability_schemas

    coll = db["schema_registry"]
    await coll.delete_many({"tenant_id": TENANT_ID})

    now = datetime.now(timezone.utc)
    docs = [
        {"tenant_id": TENANT_ID, "registered_at": now, **schema}
        for schema in all_schemas
    ]

    await coll.insert_many(docs)
    logger.info("  ✅ Registered %d collection schemas", len(docs))

    client.close()


if __name__ == "__main__":
    asyncio.run(seed_schema_registry())
