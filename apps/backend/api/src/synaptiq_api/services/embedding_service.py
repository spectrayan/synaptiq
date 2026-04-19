"""Embedding service — generate & store vector embeddings for catalog items (T5.1–T5.3).

Supports pluggable embedding models:
  - Google Gemini `text-embedding-004` (default, free tier)
  - OpenAI `text-embedding-3-small` (BYOK fallback)

Embeddings are stored alongside items in MongoDB for Atlas Vector Search.
"""
import logging
from datetime import datetime
from typing import Any

from bson import ObjectId

from synaptiq_api.core.mongodb import get_db
from synaptiq_api.models.catalog import FieldType, SchemaField

logger = logging.getLogger(__name__)

ITEMS_COLLECTION = "catalog_items"
SCHEMAS_COLLECTION = "catalog_schemas"

# T5.1 — Default embedding model
DEFAULT_MODEL = "text-embedding-004"
EMBEDDING_DIMENSIONS = 768  # Gemini text-embedding-004 output dims


# ---------------------------------------------------------------------------
# Text extraction — build embeddable text from item data + schema
# ---------------------------------------------------------------------------

def build_embeddable_text(data: dict[str, Any], fields: list[SchemaField]) -> str:
    """
    Concatenate searchable field values into a single string for embedding.

    Only fields marked searchable=True are included. Enum values and labels
    are included for semantic richness.
    """
    parts: list[str] = []

    for field in fields:
        if not field.searchable:
            continue

        value = data.get(field.field_id)
        if value is None or value == "":
            continue

        if field.type in (FieldType.text, FieldType.rich_text):
            parts.append(f"{field.label}: {value}")
        elif field.type == FieldType.enum:
            parts.append(f"{field.label}: {value}")
        elif field.type == FieldType.multi_enum and isinstance(value, list):
            parts.append(f"{field.label}: {', '.join(str(v) for v in value)}")
        elif field.type in (FieldType.number, FieldType.currency):
            unit = f" {field.unit}" if field.unit else ""
            parts.append(f"{field.label}: {value}{unit}")
        else:
            parts.append(f"{field.label}: {value}")

    return " | ".join(parts)


# ---------------------------------------------------------------------------
# Embedding generation
# ---------------------------------------------------------------------------

async def generate_embedding(text: str, model: str = DEFAULT_MODEL) -> list[float]:
    """
    Generate a vector embedding for the given text.

    Uses the google-genai SDK for Gemini embeddings (same auth path as LLM provider)
    and langchain for OpenAI embeddings.
    """
    if not text.strip():
        return []

    try:
        if "text-embedding" in model and "openai" not in model.lower():
            # Gemini embedding via google-genai SDK
            from google import genai
            from synaptiq_api.core.config import settings

            client = genai.Client(
                vertexai=True,
                project=settings.vertexai_project,
                location=settings.vertexai_location,
            )
            result = await client.aio.models.embed_content(
                model=model,
                contents=text,
            )
            return list(result.embeddings[0].values)
        else:
            # OpenAI embedding
            from langchain_openai import OpenAIEmbeddings
            embedder = OpenAIEmbeddings(model=model)
            vectors = await embedder.aembed_documents([text])
            return vectors[0] if vectors else []

    except Exception as e:
        logger.error("Embedding generation failed (model=%s): %s", model, e)
        raise


# ---------------------------------------------------------------------------
# Embed a single item (T5.2)
# ---------------------------------------------------------------------------

async def embed_item(tenant_id: str, item_id: str) -> bool:
    """
    Generate and store embedding for a single catalog item.

    Returns True if successful.
    """
    db = get_db()

    item = await db[ITEMS_COLLECTION].find_one({
        "_id": ObjectId(item_id),
        "tenant_id": tenant_id,
    })
    if not item:
        return False

    # Fetch schema for searchable field info
    schema = await db[SCHEMAS_COLLECTION].find_one(
        {"tenant_id": tenant_id, "is_active": True},
    )
    if not schema:
        logger.warning("No active schema for tenant %s, skipping embedding", tenant_id)
        return False

    fields = [SchemaField(**f) for f in schema["fields"]]
    text = build_embeddable_text(item.get("data", {}), fields)

    if not text:
        logger.info("No embeddable text for item %s, skipping", item_id)
        return False

    embedding = await generate_embedding(text)

    await db[ITEMS_COLLECTION].update_one(
        {"_id": ObjectId(item_id)},
        {
            "$set": {
                "embedding": embedding,
                "embedding_model": DEFAULT_MODEL,
                "embedded_at": datetime.utcnow(),
            },
        },
    )

    logger.info("Embedded item %s (%d dims)", item_id, len(embedding))
    return True


# ---------------------------------------------------------------------------
# Re-embed all items for a tenant (T5.3)
# ---------------------------------------------------------------------------

async def reembed_tenant_items(tenant_id: str) -> dict:
    """
    Re-embed all active items for a tenant. Called when schema searchable
    fields change. Runs as a background task.

    Returns {processed, succeeded, failed}.
    """
    db = get_db()

    schema = await db[SCHEMAS_COLLECTION].find_one(
        {"tenant_id": tenant_id, "is_active": True},
    )
    if not schema:
        return {"processed": 0, "succeeded": 0, "failed": 0}

    fields = [SchemaField(**f) for f in schema["fields"]]

    cursor = db[ITEMS_COLLECTION].find(
        {"tenant_id": tenant_id, "status": {"$ne": "archived"}},
        {"data": 1},
    )

    processed = 0
    succeeded = 0
    failed = 0

    async for item in cursor:
        processed += 1
        text = build_embeddable_text(item.get("data", {}), fields)

        if not text:
            continue

        try:
            embedding = await generate_embedding(text)
            await db[ITEMS_COLLECTION].update_one(
                {"_id": item["_id"]},
                {
                    "$set": {
                        "embedding": embedding,
                        "embedding_model": DEFAULT_MODEL,
                        "embedded_at": datetime.utcnow(),
                    },
                },
            )
            succeeded += 1
        except Exception:
            logger.exception("Failed to embed item %s", item["_id"])
            failed += 1

    logger.info(
        "Re-embedded tenant %s: %d processed, %d succeeded, %d failed",
        tenant_id, processed, succeeded, failed,
    )
    return {"processed": processed, "succeeded": succeeded, "failed": failed}
