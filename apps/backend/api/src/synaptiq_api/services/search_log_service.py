"""Search log service — lightweight write-behind logger for search events (T12.1).

Captures every search performed in the chat pipeline for analytics:
  - Most queried items (by item_id appearance in results)
  - Most common search intents (by query text)
  - Zero-result queries (result_count == 0)

All writes are fire-and-forget to avoid impacting chat latency.
"""
import logging
from datetime import datetime

from synaptiq_api.core.mongodb import get_db

logger = logging.getLogger(__name__)

SEARCH_LOGS_COLLECTION = "search_logs"


async def log_search(
    tenant_id: str,
    session_id: str,
    query: str,
    result_count: int,
    top_item_ids: list[str] | None = None,
    latency_ms: int = 0,
) -> None:
    """
    Log a search event for analytics aggregation.

    This is called as a fire-and-forget task from chat_service after
    each vector/keyword search. Failures are silently swallowed.

    Args:
        tenant_id: Tenant scope.
        session_id: Session that triggered the search.
        query: The user's search query text.
        result_count: Number of results returned.
        top_item_ids: IDs of the top results (up to 5).
        latency_ms: Time taken for the search in milliseconds.
    """
    try:
        db = get_db()

        doc = {
            "tenant_id": tenant_id,
            "session_id": session_id,
            "query": query.strip().lower()[:500],  # Normalize for grouping
            "query_raw": query.strip()[:500],       # Original case for display
            "result_count": result_count,
            "is_zero_result": result_count == 0,
            "top_item_ids": top_item_ids or [],
            "latency_ms": latency_ms,
            "created_at": datetime.utcnow(),
        }

        await db[SEARCH_LOGS_COLLECTION].insert_one(doc)

    except Exception as e:
        # Non-critical — never let search logging break the chat pipeline
        logger.debug("Search log write failed (non-critical): %s", e)
