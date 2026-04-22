import json
import logging
from typing import Any
from langchain_core.tools import tool
from langchain_core.runnables import RunnableConfig

from synaptiq_api.services.search_service import catalog_search as do_catalog_search
from synaptiq_api.services.schema_registry import SchemaRegistry
from synaptiq_api.services.embedding_service import generate_embedding

logger = logging.getLogger(__name__)

@tool
async def search_catalog(query: str, config: RunnableConfig) -> str:
    """
    Search the standard product catalog using semantic vector search.
    Use this tool when the user queries about products, items, or general catalog inventory.
    """
    tenant_id = config["configurable"].get("tenant_id")
    if not tenant_id:
        return "Error: tenant_id not found in configuration."

    logger.info("Agent computing embedding for catalog search: %s", query)
    embedding = await generate_embedding(query)
    if not embedding:
        return "Error: could not generate embedding for search query."

    logger.info("Agent executing catalog search for tenant %s", tenant_id)
    results = await do_catalog_search(
        tenant_id=tenant_id,
        query_embedding=embedding,
        top_k=8,
        min_score=0.3
    )

    if not results:
        return "No relevant catalog items found."

    # Format the results cleanly
    items_text = []
    for i, item in enumerate(results, 1):
        data = item.get("data", {})
        score = item.get("score", 0)
        fields = " | ".join(f"{k}: {v}" for k, v in data.items() if v)
        items_text.append(f"[{i}] (score: {score:.2f}) {fields}")

    return "Relevant Catalog Items:\n" + "\n".join(items_text)

@tool
async def query_collection(collection_name: str, mongodb_pipeline: list[dict], config: RunnableConfig) -> str:
    """
    Execute a dynamic MongoDB aggregation pipeline against a specific data collection.
    Use this tool to fetch metrics, summarize tasks, group orders, or retrieve any non-catalog data.
    The response will be the raw JSON query results.

    Args:
        collection_name: The name of the collection to query (e.g. 'sales_metrics', 'tasks', 'orders', 'support_tickets')
        mongodb_pipeline: A list of dicts representing a MongoDB aggregation pipeline. (e.g., [{"$match": {"status": "open"}}, {"$limit": 5}])
    """
    tenant_id = config["configurable"].get("tenant_id")
    if not tenant_id:
        return "Error: tenant_id not found in configuration."

    logger.info("Agent querying collection %s for tenant %s", collection_name, tenant_id)
    try:
        # Note: SchemaRegistry automatically prepends a $match for tenant_id.
        result = await SchemaRegistry.query_data(
            tenant_id=tenant_id,
            collection_name=collection_name,
            pipeline=mongodb_pipeline
        )
        data = result.get("results", [])
        return json.dumps(data, default=str)
    except Exception as e:
        logger.error("Error executing query against %s: %s", collection_name, str(e))
        return f"Error executing query: {str(e)}"
