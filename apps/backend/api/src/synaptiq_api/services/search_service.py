"""Catalog search — Atlas Vector Search + metadata filtering (T5.4).

Provides semantic search over embedded catalog items using MongoDB Atlas
Vector Search with optional metadata pre-filtering.
"""
import logging
from typing import Any

from synaptiq_api.core.mongodb import get_db

logger = logging.getLogger(__name__)

ITEMS_COLLECTION = "catalog_items"
VECTOR_INDEX_NAME = "catalog_vector_index"


async def catalog_search(
    tenant_id: str,
    query_embedding: list[float],
    filters: dict[str, Any] | None = None,
    top_k: int = 10,
    min_score: float = 0.0,
) -> list[dict]:
    """
    Semantic vector search over catalog items (REQ-NF4).

    Uses MongoDB Atlas $vectorSearch aggregation stage with optional
    metadata pre-filtering on tenant_id and item data fields.

    Args:
        tenant_id: Scope search to this tenant.
        query_embedding: The query vector (same dimensionality as stored embeddings).
        filters: Optional metadata filters (e.g. {"data.category": "shoes"}).
        top_k: Maximum number of results.
        min_score: Minimum similarity score threshold.

    Returns:
        List of matching items with a `score` field appended.
    """
    db = get_db()

    # Build the pre-filter — always scoped to tenant + active items
    pre_filter: dict[str, Any] = {
        "tenant_id": tenant_id,
        "status": "active",
    }
    if filters:
        for key, value in filters.items():
            # Prefix data fields
            if not key.startswith("data.") and key not in ("status", "tenant_id"):
                pre_filter[f"data.{key}"] = value
            else:
                pre_filter[key] = value

    pipeline: list[dict] = [
        {
            "$vectorSearch": {
                "index": VECTOR_INDEX_NAME,
                "path": "embedding",
                "queryVector": query_embedding,
                "numCandidates": top_k * 10,
                "limit": top_k,
                "filter": pre_filter,
            },
        },
        {
            "$addFields": {
                "score": {"$meta": "vectorSearchScore"},
            },
        },
        # Remove embedding from response to save bandwidth
        {
            "$unset": ["embedding"],
        },
    ]

    # Apply minimum score filter
    if min_score > 0:
        pipeline.append({
            "$match": {"score": {"$gte": min_score}},
        })

    results: list[dict] = []
    async for doc in db[ITEMS_COLLECTION].aggregate(pipeline):
        doc["_id"] = str(doc["_id"])
        results.append(doc)

    logger.info(
        "Vector search for tenant %s: %d results (top_k=%d)",
        tenant_id, len(results), top_k,
    )
    return results


async def keyword_search(
    tenant_id: str,
    query: str,
    top_k: int = 10,
) -> list[dict]:
    """
    Keyword fallback search — used when the LLM circuit breaker is open (REQ-NF9).

    Uses MongoDB $text search on indexed data fields.
    """
    db = get_db()

    results: list[dict] = []
    cursor = db[ITEMS_COLLECTION].find(
        {
            "tenant_id": tenant_id,
            "status": "active",
            "$text": {"$search": query},
        },
        {"embedding": 0, "score": {"$meta": "textScore"}},
    ).sort(
        [("score", {"$meta": "textScore"})]
    ).limit(top_k)

    async for doc in cursor:
        doc["_id"] = str(doc["_id"])
        results.append(doc)

    return results
