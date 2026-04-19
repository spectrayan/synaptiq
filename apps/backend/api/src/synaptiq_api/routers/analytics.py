"""Analytics router — Phase 12: Analytics & Usage.

Provides aggregated metrics for tenant admins (T12.1–T12.5)
and platform-wide rollups for L0 admins (T12.6).
"""
import logging
from datetime import datetime, timedelta, timezone

from fastapi import APIRouter, Depends, Query
from pydantic import BaseModel, Field

from synaptiq_api.core.dependencies import require_platform_admin, require_tenant_admin
from synaptiq_api.core.mongodb import get_db

logger = logging.getLogger(__name__)
router = APIRouter()


# ---------------------------------------------------------------------------
# Response models
# ---------------------------------------------------------------------------

class DailyMetric(BaseModel):
    """Single day's aggregated metric."""
    date: str  # YYYY-MM-DD
    value: int | float = 0


class TopItem(BaseModel):
    """Most-queried item."""
    item_id: str
    name: str = ""
    query_count: int = 0


class TopIntent(BaseModel):
    """Most common user intent."""
    intent: str
    count: int = 0


class AnalyticsSummary(BaseModel):
    """Aggregated analytics for a date range — T12.2."""
    total_conversations: int = 0
    total_messages: int = 0
    total_tokens_input: int = 0
    total_tokens_output: int = 0
    total_actions: int = 0
    unique_users: int = 0
    avg_messages_per_session: float = 0.0
    daily_conversations: list[DailyMetric] = Field(default_factory=list)
    daily_messages: list[DailyMetric] = Field(default_factory=list)
    top_queried_items: list[TopItem] = Field(default_factory=list)
    top_intents: list[TopIntent] = Field(default_factory=list)
    zero_result_queries: list[str] = Field(default_factory=list)
    action_rates: dict[str, int] = Field(default_factory=dict)


class TokenUsageSummary(BaseModel):
    """Token usage vs plan limit — T12.4."""
    total_tokens_input: int = 0
    total_tokens_output: int = 0
    total_tokens: int = 0
    estimated_cost_usd: float = 0.0
    # Plan limits (from tenant config)
    plan_token_limit: int = 0  # 0 = unlimited
    usage_percent: float = 0.0


class BillingReport(BaseModel):
    """Billing breakdown — T12.5."""
    seat_count: int = 0
    total_tokens: int = 0
    estimated_cost_usd: float = 0.0
    by_provider: dict[str, dict] = Field(default_factory=dict)


class PlatformRollup(BaseModel):
    """Platform admin (L0) rollup — T12.6."""
    total_tenants: int = 0
    active_tenants: int = 0
    total_conversations: int = 0
    total_messages: int = 0
    total_tokens: int = 0
    total_estimated_cost_usd: float = 0.0
    byok_tenants: int = 0
    platform_managed_tenants: int = 0
    per_tenant: list[dict] = Field(default_factory=list)


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _parse_date(date_str: str | None, default_days_ago: int = 30) -> datetime:
    """Parse a date string (YYYY-MM-DD) or return a default date."""
    if date_str:
        try:
            return datetime.strptime(date_str, "%Y-%m-%d").replace(tzinfo=timezone.utc)
        except ValueError:
            pass
    return datetime.now(timezone.utc) - timedelta(days=default_days_ago)


# ---------------------------------------------------------------------------
# T12.1 — Daily aggregation (called by endpoint, could be scheduled)
# ---------------------------------------------------------------------------

async def _aggregate_conversations(
    tenant_id: str,
    from_dt: datetime,
    to_dt: datetime,
) -> dict:
    """Aggregate conversation metrics from sessions and usage_ledger."""
    db = get_db()

    # Conversations count
    conversation_count = await db.sessions.count_documents({
        "tenant_id": tenant_id,
        "created_at": {"$gte": from_dt, "$lte": to_dt},
    })

    # Messages count from usage_ledger (token_consumption events = 1 per turn)
    message_pipeline = [
        {
            "$match": {
                "tenant_id": tenant_id,
                "event_type": "token_consumption",
                "created_at": {"$gte": from_dt, "$lte": to_dt},
            },
        },
        {
            "$group": {
                "_id": None,
                "total_messages": {"$sum": 1},
                "total_tokens_input": {"$sum": "$tokens_input"},
                "total_tokens_output": {"$sum": "$tokens_output"},
                "total_cost": {"$sum": "$estimated_cost_usd"},
                "unique_sessions": {"$addToSet": "$session_id"},
                "unique_users": {"$addToSet": "$user_uid"},
            },
        },
    ]
    msg_result = await db.usage_ledger.aggregate(message_pipeline).to_list(1)
    msg_data = msg_result[0] if msg_result else {}

    total_messages = msg_data.get("total_messages", 0)
    unique_sessions = len(msg_data.get("unique_sessions", []))
    unique_users = len([u for u in msg_data.get("unique_users", []) if u])

    # Daily breakdown
    daily_pipeline = [
        {
            "$match": {
                "tenant_id": tenant_id,
                "created_at": {"$gte": from_dt, "$lte": to_dt},
            },
        },
        {
            "$group": {
                "_id": {"$dateToString": {"format": "%Y-%m-%d", "date": "$created_at"}},
                "count": {"$sum": 1},
            },
        },
        {"$sort": {"_id": 1}},
    ]

    daily_convos = await db.sessions.aggregate(daily_pipeline).to_list(100)
    daily_msgs_pipeline = [
        {
            "$match": {
                "tenant_id": tenant_id,
                "event_type": "token_consumption",
                "created_at": {"$gte": from_dt, "$lte": to_dt},
            },
        },
        {
            "$group": {
                "_id": {"$dateToString": {"format": "%Y-%m-%d", "date": "$created_at"}},
                "count": {"$sum": 1},
            },
        },
        {"$sort": {"_id": 1}},
    ]
    daily_msgs = await db.usage_ledger.aggregate(daily_msgs_pipeline).to_list(100)

    # Action rates
    action_pipeline = [
        {
            "$match": {
                "tenant_id": tenant_id,
                "created_at": {"$gte": from_dt, "$lte": to_dt},
            },
        },
        {
            "$group": {
                "_id": "$action",
                "count": {"$sum": 1},
            },
        },
    ]
    action_results = await db.action_logs.aggregate(action_pipeline).to_list(50)
    action_rates = {r["_id"]: r["count"] for r in action_results if r["_id"]}

    total_actions = sum(action_rates.values())

    return {
        "total_conversations": conversation_count,
        "total_messages": total_messages,
        "total_tokens_input": msg_data.get("total_tokens_input", 0),
        "total_tokens_output": msg_data.get("total_tokens_output", 0),
        "total_actions": total_actions,
        "unique_users": unique_users,
        "avg_messages_per_session": round(total_messages / max(unique_sessions, 1), 1),
        "daily_conversations": [
            DailyMetric(date=d["_id"], value=d["count"]) for d in daily_convos
        ],
        "daily_messages": [
            DailyMetric(date=d["_id"], value=d["count"]) for d in daily_msgs
        ],
        "top_queried_items": [],  # TODO: implement search log tracking
        "top_intents": [],
        "zero_result_queries": [],
        "action_rates": action_rates,
    }


# ---------------------------------------------------------------------------
# T12.2 — Analytics Summary Endpoint
# ---------------------------------------------------------------------------

@router.get(
    "/summary",
    response_model=AnalyticsSummary,
    dependencies=[Depends(require_tenant_admin)],
)
async def get_analytics_summary(
    user: dict = Depends(require_tenant_admin),
    from_date: str | None = Query(None, alias="from", description="Start date YYYY-MM-DD"),
    to_date: str | None = Query(None, alias="to", description="End date YYYY-MM-DD"),
):
    """Return aggregated analytics for the tenant within a date range."""
    tenant_id = user.get("tenant_id", "")
    from_dt = _parse_date(from_date, default_days_ago=30)
    to_dt = _parse_date(to_date, default_days_ago=0)
    if to_dt < from_dt:
        to_dt = datetime.now(timezone.utc)

    data = await _aggregate_conversations(tenant_id, from_dt, to_dt)
    return AnalyticsSummary(**data)


# ---------------------------------------------------------------------------
# T12.4 — Token Usage vs Plan Limit
# ---------------------------------------------------------------------------

@router.get(
    "/tokens",
    response_model=TokenUsageSummary,
    dependencies=[Depends(require_tenant_admin)],
)
async def get_token_usage(
    user: dict = Depends(require_tenant_admin),
    from_date: str | None = Query(None, alias="from"),
    to_date: str | None = Query(None, alias="to"),
):
    """Return token usage breakdown with plan limit comparison."""
    tenant_id = user.get("tenant_id", "")
    from_dt = _parse_date(from_date, default_days_ago=30)
    to_dt = _parse_date(to_date, default_days_ago=0)
    if to_dt < from_dt:
        to_dt = datetime.now(timezone.utc)

    db = get_db()

    pipeline = [
        {
            "$match": {
                "tenant_id": tenant_id,
                "event_type": "token_consumption",
                "created_at": {"$gte": from_dt, "$lte": to_dt},
            },
        },
        {
            "$group": {
                "_id": None,
                "total_input": {"$sum": "$tokens_input"},
                "total_output": {"$sum": "$tokens_output"},
                "total_cost": {"$sum": "$estimated_cost_usd"},
            },
        },
    ]

    result = await db.usage_ledger.aggregate(pipeline).to_list(1)
    data = result[0] if result else {}

    total_input = data.get("total_input", 0)
    total_output = data.get("total_output", 0)
    total_tokens = total_input + total_output
    cost = round(data.get("total_cost", 0.0), 4)

    # Get plan limit from tenant doc
    tenant = await db.tenants.find_one({"tenant_id": tenant_id}, {"limits": 1})
    plan_limit = (tenant or {}).get("limits", {}).get("monthly_token_limit", 0)
    usage_pct = round((total_tokens / max(plan_limit, 1)) * 100, 1) if plan_limit else 0.0

    return TokenUsageSummary(
        total_tokens_input=total_input,
        total_tokens_output=total_output,
        total_tokens=total_tokens,
        estimated_cost_usd=cost,
        plan_token_limit=plan_limit,
        usage_percent=usage_pct,
    )


# ---------------------------------------------------------------------------
# T12.5 — Billing Report
# ---------------------------------------------------------------------------

@router.get(
    "/billing",
    response_model=BillingReport,
    dependencies=[Depends(require_tenant_admin)],
)
async def get_billing_report(
    user: dict = Depends(require_tenant_admin),
    from_date: str | None = Query(None, alias="from"),
    to_date: str | None = Query(None, alias="to"),
):
    """Return billing breakdown: seats, tokens, cost by provider."""
    tenant_id = user.get("tenant_id", "")
    from_dt = _parse_date(from_date, default_days_ago=30)
    to_dt = _parse_date(to_date, default_days_ago=0)
    if to_dt < from_dt:
        to_dt = datetime.now(timezone.utc)

    db = get_db()

    # Seat count = unique users with seat_start events
    seat_pipeline = [
        {
            "$match": {
                "tenant_id": tenant_id,
                "event_type": "seat_start",
                "created_at": {"$gte": from_dt, "$lte": to_dt},
            },
        },
        {
            "$group": {
                "_id": "$user_uid",
            },
        },
        {"$count": "count"},
    ]
    seat_result = await db.usage_ledger.aggregate(seat_pipeline).to_list(1)
    seat_count = seat_result[0]["count"] if seat_result else 0

    # Token breakdown by provider
    provider_pipeline = [
        {
            "$match": {
                "tenant_id": tenant_id,
                "event_type": "token_consumption",
                "created_at": {"$gte": from_dt, "$lte": to_dt},
            },
        },
        {
            "$group": {
                "_id": "$provider",
                "tokens_input": {"$sum": "$tokens_input"},
                "tokens_output": {"$sum": "$tokens_output"},
                "cost": {"$sum": "$estimated_cost_usd"},
                "requests": {"$sum": 1},
            },
        },
    ]
    provider_results = await db.usage_ledger.aggregate(provider_pipeline).to_list(20)

    by_provider = {}
    total_tokens = 0
    total_cost = 0.0
    for p in provider_results:
        name = p["_id"] or "unknown"
        tokens = p["tokens_input"] + p["tokens_output"]
        total_tokens += tokens
        total_cost += p["cost"]
        by_provider[name] = {
            "tokens_input": p["tokens_input"],
            "tokens_output": p["tokens_output"],
            "total_tokens": tokens,
            "estimated_cost_usd": round(p["cost"], 4),
            "requests": p["requests"],
        }

    return BillingReport(
        seat_count=seat_count,
        total_tokens=total_tokens,
        estimated_cost_usd=round(total_cost, 4),
        by_provider=by_provider,
    )


# ---------------------------------------------------------------------------
# T12.6 — Platform Admin Rollup
# ---------------------------------------------------------------------------

@router.get(
    "/platform",
    response_model=PlatformRollup,
    dependencies=[Depends(require_platform_admin)],
)
async def get_platform_rollup(
    from_date: str | None = Query(None, alias="from"),
    to_date: str | None = Query(None, alias="to"),
):
    """Platform admin (L0) rollup: all-tenant usage, revenue vs LLM cost, BYOK split."""
    from_dt = _parse_date(from_date, default_days_ago=30)
    to_dt = _parse_date(to_date, default_days_ago=0)
    if to_dt < from_dt:
        to_dt = datetime.now(timezone.utc)

    db = get_db()

    # All tenants
    total_tenants = await db.tenants.count_documents({})
    active_tenants = await db.tenants.count_documents({"status": "active"})

    # Global token stats
    global_pipeline = [
        {
            "$match": {
                "event_type": "token_consumption",
                "created_at": {"$gte": from_dt, "$lte": to_dt},
            },
        },
        {
            "$group": {
                "_id": "$tenant_id",
                "conversations": {"$addToSet": "$session_id"},
                "messages": {"$sum": 1},
                "tokens": {"$sum": {"$add": ["$tokens_input", "$tokens_output"]}},
                "cost": {"$sum": "$estimated_cost_usd"},
                "providers": {"$addToSet": "$provider"},
            },
        },
    ]
    tenant_stats = await db.usage_ledger.aggregate(global_pipeline).to_list(1000)

    total_conversations = 0
    total_messages = 0
    total_tokens = 0
    total_cost = 0.0
    byok_count = 0
    platform_managed_count = 0
    per_tenant = []

    for ts in tenant_stats:
        convos = len(ts.get("conversations", []))
        total_conversations += convos
        total_messages += ts["messages"]
        total_tokens += ts["tokens"]
        total_cost += ts["cost"]

        providers = ts.get("providers", [])
        is_byok = any(p in providers for p in ["openai", "anthropic", "gemini"])
        if is_byok:
            byok_count += 1
        else:
            platform_managed_count += 1

        per_tenant.append({
            "tenant_id": ts["_id"],
            "conversations": convos,
            "messages": ts["messages"],
            "tokens": ts["tokens"],
            "estimated_cost_usd": round(ts["cost"], 4),
            "is_byok": is_byok,
        })

    return PlatformRollup(
        total_tenants=total_tenants,
        active_tenants=active_tenants,
        total_conversations=total_conversations,
        total_messages=total_messages,
        total_tokens=total_tokens,
        total_estimated_cost_usd=round(total_cost, 4),
        byok_tenants=byok_count,
        platform_managed_tenants=platform_managed_count,
        per_tenant=per_tenant,
    )
