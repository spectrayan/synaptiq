"""Embed all catalog items for a tenant — one-time bootstrap script.

Fixes missing seed data (schema + item status) and generates embeddings.

Usage:
    cd apps/backend/api
    uv run python -m synaptiq_api.scripts.embed_items [tenant_id]
"""
import asyncio
import sys
import logging
from datetime import datetime, timezone

logging.basicConfig(level=logging.INFO, format="%(levelname)s: %(message)s")
logger = logging.getLogger(__name__)


async def main(tenant_id: str = "acme-demo"):
    from synaptiq_api.core.mongodb import connect_db, disconnect_db, get_db
    from synaptiq_api.services.embedding_service import generate_embedding

    await connect_db()
    db = get_db()

    # ── Step 1: Ensure catalog_schemas collection has the active schema ──
    existing_schema = await db["catalog_schemas"].find_one(
        {"tenant_id": tenant_id, "is_active": True},
    )
    if not existing_schema:
        # Pull schema from tenant doc (seed puts it there)
        tenant = await db["tenants"].find_one({"tenant_id": tenant_id})
        if not tenant:
            logger.error("Tenant %s not found!", tenant_id)
            return

        raw_fields = tenant.get("schema", {}).get("fields", [])
        if not raw_fields:
            logger.error("No schema fields found in tenant config!")
            return

        # Convert seed schema format → canonical SchemaField format
        schema_fields = []
        for f in raw_fields:
            field_type = f.get("type", "text")
            # Map seed types to SchemaField types
            type_map = {"string": "text", "number": "number", "boolean": "boolean"}
            schema_fields.append({
                "field_id": f["name"],
                "label": f.get("label", f["name"]),
                "type": type_map.get(field_type, field_type),
                "required": f.get("required", False),
                "searchable": f.get("searchable", False),
                "filterable": f.get("filterable", False),
                "sortable": f.get("sortable", False),
                "visibility": "public",
                "unit": f.get("unit", ""),
            })

        schema_doc = {
            "tenant_id": tenant_id,
            "schema_id": tenant.get("schema", {}).get("schema_id", f"{tenant_id}-v1"),
            "version": 1,
            "is_active": True,
            "fields": schema_fields,
            "created_at": datetime.now(timezone.utc),
            "updated_at": datetime.now(timezone.utc),
        }
        await db["catalog_schemas"].insert_one(schema_doc)
        logger.info("✅ Created catalog_schemas entry with %d fields", len(schema_fields))
    else:
        logger.info("Schema already exists in catalog_schemas collection")

    # ── Step 2: Ensure all items have status = "active" ──
    result = await db["catalog_items"].update_many(
        {"tenant_id": tenant_id, "status": {"$exists": False}},
        {"$set": {"status": "active"}},
    )
    if result.modified_count > 0:
        logger.info("✅ Added status='active' to %d items", result.modified_count)

    # ── Step 3: Quick embedding connectivity test ──
    logger.info("Testing embedding connectivity...")
    test_vec = await generate_embedding("test connectivity check")
    logger.info("✅ Embedding works! Dimensions: %d", len(test_vec))

    # ── Step 4: Embed each item manually (to avoid schema mismatch issues) ──
    schema = await db["catalog_schemas"].find_one(
        {"tenant_id": tenant_id, "is_active": True},
    )
    fields = schema["fields"]
    searchable_fields = [f["field_id"] for f in fields if f.get("searchable")]
    logger.info("Searchable fields: %s", searchable_fields)

    cursor = db["catalog_items"].find(
        {"tenant_id": tenant_id, "status": "active"},
    )
    processed = 0
    succeeded = 0

    async for item in cursor:
        processed += 1
        data = item.get("data", {})

        # Build embeddable text from searchable fields
        parts = []
        for f in fields:
            if not f.get("searchable"):
                continue
            val = data.get(f["field_id"])
            if val:
                parts.append(f"{f['label']}: {val}")

        # Also include non-searchable but useful context
        for key in ["category", "brand", "price"]:
            if key not in searchable_fields and data.get(key):
                parts.append(f"{key}: {data[key]}")

        text = " | ".join(parts)
        if not text:
            logger.warning("No embeddable text for %s, skipping", data.get("name"))
            continue

        try:
            embedding = await generate_embedding(text)
            await db["catalog_items"].update_one(
                {"_id": item["_id"]},
                {
                    "$set": {
                        "embedding": embedding,
                        "embedding_model": "text-embedding-004",
                        "embedded_at": datetime.now(timezone.utc),
                    },
                },
            )
            succeeded += 1
            logger.info("  ✅ Embedded: %s (%d dims)", data.get("name"), len(embedding))
        except Exception as e:
            logger.error("  ❌ Failed: %s — %s", data.get("name"), e)

    logger.info(
        "🎉 Embedding complete: %d processed, %d succeeded",
        processed, succeeded,
    )

    await disconnect_db()


if __name__ == "__main__":
    tid = sys.argv[1] if len(sys.argv) > 1 else "acme-demo"
    asyncio.run(main(tid))
