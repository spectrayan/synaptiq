"""Daily analytics aggregation job (T12.1).

Pre-aggregates metrics from raw collections (usage_ledger, sessions,
search_logs, action_logs) into the `analytics_daily` collection.

Can be triggered via:
  - CLI:  python -m synaptiq_api.services.aggregation_job
  - API:  POST /analytics/aggregate (platform admin only)
  - Cron: Cloud Scheduler → Cloud Run job

The aggregated data powers the GET /analytics/summary endpoint,
which prefers pre-aggregated data over live aggregation.
"""
import asyncio
import logging
from datetime import date, datetime, timedelta, timezone

from synaptiq_api.core.mongodb import get_db

logger = logging.getLogger(__name__)

ANALYTICS_DAILY_COLLECTION = "analytics_daily"


async def run_daily_aggregation(
    target_date: date | None = None,
) -> dict:
    """
    Run daily aggregation for all active tenants.

    Args:
        target_date: The date to aggregate (default: yesterday UTC).

    Returns:
        Summary dict: {tenants_processed, succeeded, failed}
    """
    db = get_db()

    if target_date is None:
        target_date = (datetime.now(timezone.utc) - timedelta(days=1)).date()

    from_dt = datetime(target_date.year, target_date.month, target_date.day,
                       tzinfo=timezone.utc)
    to_dt = from_dt + timedelta(days=1)
    date_str = target_date.isoformat()

    # Get all active tenants
    tenants = await db["tenants"].find(
        {"status": {"$in": ["active", "trial"]}},
        {"tenant_id": 1},
    ).to_list(10000)

    processed = 0
    succeeded = 0
    failed = 0

    for tenant_doc in tenants:
        tenant_id = tenant_doc["tenant_id"]
        processed += 1

        try:
            metrics = await _aggregate_tenant_day(db, tenant_id, from_dt, to_dt)

            # Upsert into analytics_daily
            await db[ANALYTICS_DAILY_COLLECTION].update_one(
                {"tenant_id": tenant_id, "date": date_str},
                {"$set": {
                    "tenant_id": tenant_id,
                    "date": date_str,
                    "metrics": metrics,
                    "aggregated_at": datetime.now(timezone.utc),
                }},
                upsert=True,
            )
            succeeded += 1

        except Exception:
            logger.exception("Aggregation failed for tenant %s on %s", tenant_id, date_str)
            failed += 1

    logger.info(
        "Daily aggregation for %s: %d processed, %d succeeded, %d failed",
        date_str, processed, succeeded, failed,
    )

    return {
        "date": date_str,
        "tenants_processed": processed,
        "succeeded": succeeded,
        "failed": failed,
    }


async def _aggregate_tenant_day(
    db, tenant_id: str, from_dt: datetime, to_dt: datetime,
) -> dict:
    """Aggregate all metrics for one tenant for one day."""

    # ── Conversations ─────────────────────────────────────────────────
    conversations = await db["sessions"].count_documents({
        "tenant_id": tenant_id,
        "created_at": {"$gte": from_dt, "$lt": to_dt},
    })

    # ── Messages + tokens ─────────────────────────────────────────────
    msg_pipeline = [
        {
            "$match": {
                "tenant_id": tenant_id,
                "event_type": "token_consumption",
                "created_at": {"$gte": from_dt, "$lt": to_dt},
            },
        },
        {
            "$group": {
                "_id": None,
                "messages": {"$sum": 1},
                "tokens_input": {"$sum": "$tokens_input"},
                "tokens_output": {"$sum": "$tokens_output"},
                "cost": {"$sum": "$estimated_cost_usd"},
                "unique_sessions": {"$addToSet": "$session_id"},
            },
        },
    ]
    msg_result = await db["usage_ledger"].aggregate(msg_pipeline).to_list(1)
    msg = msg_result[0] if msg_result else {}

    # ── Actions ───────────────────────────────────────────────────────
    action_pipeline = [
        {
            "$match": {
                "tenant_id": tenant_id,
                "created_at": {"$gte": from_dt, "$lt": to_dt},
            },
        },
        {"$group": {"_id": "$action", "count": {"$sum": 1}}},
    ]
    action_results = await db["action_logs"].aggregate(action_pipeline).to_list(50)
    action_rates = {r["_id"]: r["count"] for r in action_results if r["_id"]}

    # ── Search metrics ────────────────────────────────────────────────
    search_metrics = await _aggregate_search_metrics(db, tenant_id, from_dt, to_dt)

    return {
        "conversations": conversations,
        "messages": msg.get("messages", 0),
        "tokens_input": msg.get("tokens_input", 0),
        "tokens_output": msg.get("tokens_output", 0),
        "estimated_cost_usd": round(msg.get("cost", 0.0), 4),
        "unique_sessions": len(msg.get("unique_sessions", [])),
        "actions": sum(action_rates.values()),
        "action_rates": action_rates,
        **search_metrics,
    }


async def _aggregate_search_metrics(
    db, tenant_id: str, from_dt: datetime, to_dt: datetime,
) -> dict:
    """Aggregate search-specific metrics from search_logs."""

    base_match = {
        "tenant_id": tenant_id,
        "created_at": {"$gte": from_dt, "$lt": to_dt},
    }

    # ── Top queried items ─────────────────────────────────────────────
    top_items_pipeline = [
        {"$match": base_match},
        {"$unwind": "$top_item_ids"},
        {"$group": {"_id": "$top_item_ids", "count": {"$sum": 1}}},
        {"$sort": {"count": -1}},
        {"$limit": 10},
        # Lookup item names
        {
            "$lookup": {
                "from": "catalog_items",
                "let": {"item_id": {"$toObjectId": "$_id"}},
                "pipeline": [
                    {"$match": {"$expr": {"$eq": ["$_id", "$$item_id"]}}},
                    {"$project": {"data.name": 1}},
                ],
                "as": "item",
            },
        },
        {"$unwind": {"path": "$item", "preserveNullAndEmptyArrays": True}},
    ]

    try:
        top_items_raw = await db["search_logs"].aggregate(top_items_pipeline).to_list(10)
        top_queried_items = [
            {
                "item_id": str(r["_id"]),
                "name": (r.get("item", {}).get("data", {}) or {}).get("name", ""),
                "query_count": r["count"],
            }
            for r in top_items_raw
        ]
    except Exception:
        top_queried_items = []

    # ── Top intents (most common search queries) ──────────────────────
    intents_pipeline = [
        {"$match": base_match},
        {"$group": {"_id": "$query", "count": {"$sum": 1}}},
        {"$sort": {"count": -1}},
        {"$limit": 10},
    ]

    try:
        intents_raw = await db["search_logs"].aggregate(intents_pipeline).to_list(10)
        top_intents = [
            {"intent": r["_id"], "count": r["count"]}
            for r in intents_raw if r["_id"]
        ]
    except Exception:
        top_intents = []

    # ── Zero-result queries ───────────────────────────────────────────
    zero_pipeline = [
        {"$match": {**base_match, "is_zero_result": True}},
        {"$group": {"_id": "$query", "count": {"$sum": 1}}},
        {"$sort": {"count": -1}},
        {"$limit": 20},
    ]

    try:
        zero_raw = await db["search_logs"].aggregate(zero_pipeline).to_list(20)
        zero_result_queries = [r["_id"] for r in zero_raw if r["_id"]]
    except Exception:
        zero_result_queries = []

    return {
        "top_queried_items": top_queried_items,
        "top_intents": top_intents,
        "zero_result_queries": zero_result_queries,
    }


# ---------------------------------------------------------------------------
# CLI entry point: python -m synaptiq_api.services.aggregation_job
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    import sys

    async def _main():
        from synaptiq_api.core.mongodb import init_db
        await init_db()

        target = None
        if len(sys.argv) > 1:
            target = date.fromisoformat(sys.argv[1])

        result = await run_daily_aggregation(target)
        print(f"Aggregation complete: {result}")

    asyncio.run(_main())
