"""
Synaptiq Seed: Schema Registry
===============================
Registers all data collections (business + observability) in the schema registry
so the LLM's query_collection tool can discover and query them.

Usage:
    python seed-data/seed_schema_registry.py
"""
import asyncio
import logging
import os
from datetime import datetime, timezone

from motor.motor_asyncio import AsyncIOMotorClient

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

MONGODB_URI = os.environ.get("MONGODB_URI", "mongodb://localhost:27017/?directConnection=true")
DB_NAME = "synaptiq"
TENANT_ID = "demo-tenant"


SCHEMAS = [
    # ── Business Data ──────────────────────────────────────────────
    {
        "collection_name": "products",
        "display_name": "Products",
        "description": "Product catalog with pricing, stock levels, ratings, and sales data",
        "fields": {
            "name": "string", "category": "string", "price": "number", "stock": "number",
            "rating": "number", "units_sold": "number", "revenue": "number", "margin": "number",
            "description": "string", "created_at": "date",
        },
    },
    {
        "collection_name": "orders",
        "display_name": "Orders",
        "description": "Customer orders with items, totals, and status tracking (pending/processing/shipped/delivered/returned)",
        "fields": {
            "order_id": "string", "customer_name": "string", "customer_email": "string",
            "items": "array", "total": "number", "status": "string",
            "created_at": "date", "shipped_at": "date",
        },
    },
    {
        "collection_name": "sales_metrics",
        "display_name": "Sales Metrics",
        "description": "Daily aggregated sales: revenue, orders, sessions, conversion rates, new vs returning customers (90 days)",
        "fields": {
            "date": "string", "revenue": "number", "orders": "number", "sessions": "number",
            "conversion_rate": "number", "avg_order_value": "number",
            "new_customers": "number", "returning_customers": "number",
        },
    },
    {
        "collection_name": "monthly_sales",
        "display_name": "Monthly Sales",
        "description": "Monthly aggregated revenue, orders, profit, and growth percentage",
        "fields": {"month": "string", "year": "number", "revenue": "number", "orders": "number", "profit": "number", "growth": "number"},
    },
    {
        "collection_name": "category_sales",
        "display_name": "Sales by Category",
        "description": "Revenue and units sold breakdown by product category with percentage of total",
        "fields": {"category": "string", "revenue": "number", "units_sold": "number", "pct_total": "number", "avg_price": "number", "top_product": "string"},
    },
    {
        "collection_name": "regional_sales",
        "display_name": "Regional Sales",
        "description": "Revenue, orders, and customer counts by geographic region",
        "fields": {"region": "string", "revenue": "number", "orders": "number", "customers": "number", "avg_order_value": "number"},
    },
    {
        "collection_name": "tasks",
        "display_name": "Tasks",
        "description": "Project management tasks with kanban status (backlog/in_progress/review/done), priority, assignees, and tags",
        "fields": {"title": "string", "description": "string", "status": "string", "priority": "string", "assignee": "string", "tags": "array", "due_date": "string", "created_at": "date"},
    },
    {
        "collection_name": "events",
        "display_name": "Events",
        "description": "Audit log / event timeline for orders and system events",
        "fields": {"event_type": "string", "title": "string", "description": "string", "actor": "string", "entity_type": "string", "entity_id": "string", "timestamp": "date"},
    },
    {
        "collection_name": "support_tickets",
        "display_name": "Support Tickets",
        "description": "Customer support tickets with status (open/in_progress/resolved) and priority levels",
        "fields": {"ticket_id": "string", "subject": "string", "status": "string", "priority": "string", "customer_email": "string", "created_at": "date"},
    },
    # ── Observability Data ─────────────────────────────────────────
    {
        "collection_name": "api_metrics",
        "display_name": "API Metrics",
        "description": "5-minute interval API endpoint metrics: request counts, error rates, latency percentiles (P50/P95/P99), status code distribution, and bandwidth. Covers 7 days. Filter by endpoint and method.",
        "fields": {
            "timestamp": "date", "endpoint": "string", "method": "string",
            "requests": "number", "errors": "number", "error_rate": "number",
            "p50_latency_ms": "number", "p95_latency_ms": "number", "p99_latency_ms": "number",
            "avg_latency_ms": "number", "status_2xx": "number", "status_4xx": "number",
            "status_5xx": "number", "bytes_in": "number", "bytes_out": "number",
        },
    },
    {
        "collection_name": "infra_metrics",
        "display_name": "Infrastructure Metrics",
        "description": "1-minute interval infrastructure metrics per service: CPU%, memory, disk I/O, network I/O, connections, GC pauses, thread counts. Services: api-server, mongodb, redis, llm-proxy. Covers 24 hours.",
        "fields": {
            "timestamp": "date", "service": "string",
            "cpu_percent": "number", "memory_mb": "number", "memory_percent": "number",
            "disk_read_mb": "number", "disk_write_mb": "number",
            "network_in_mb": "number", "network_out_mb": "number",
            "active_connections": "number", "gc_pause_ms": "number", "thread_count": "number",
        },
    },
    {
        "collection_name": "llm_metrics",
        "display_name": "LLM Metrics",
        "description": "5-minute interval LLM usage: requests, token counts (in/out), time-to-first-token, generation latency, cache hit rates, tool calls, errors, and cost in USD. Covers 7 days.",
        "fields": {
            "timestamp": "date", "provider": "string", "model": "string",
            "requests": "number", "tokens_in": "number", "tokens_out": "number",
            "avg_ttft_ms": "number", "avg_generation_ms": "number",
            "cache_hits": "number", "cache_misses": "number",
            "tool_calls": "number", "errors": "number", "cost_usd": "number",
        },
    },
    {
        "collection_name": "error_logs",
        "display_name": "Error Logs",
        "description": "Application error and warning logs with error type, service, endpoint, resolution status. Types include LLMTimeoutError, RateLimitError, MongoConnectionError, ValidationError, etc.",
        "fields": {
            "timestamp": "date", "level": "string", "service": "string",
            "error_type": "string", "message": "string", "endpoint": "string",
            "session_id": "string", "resolved": "boolean", "count": "number",
        },
    },
    {
        "collection_name": "slo_metrics",
        "display_name": "SLO Metrics",
        "description": "Daily SLO compliance tracking: target vs actual, error budget remaining, violation counts. SLOs: API Availability (99.9%), Chat Latency P95 < 2s, LLM Error Rate < 5%, Search Latency P95 < 500ms. Covers 90 days.",
        "fields": {
            "date": "string", "slo_name": "string", "target": "number",
            "actual": "number", "budget_remaining_pct": "number",
            "error_budget_minutes": "number", "violations": "number",
        },
    },
    {
        "collection_name": "user_analytics",
        "display_name": "User Analytics",
        "description": "Hourly user engagement: active sessions, new sessions, messages sent, components rendered, actions triggered, session duration, bounce rate, and top intents. Covers 7 days.",
        "fields": {
            "timestamp": "date", "active_sessions": "number", "new_sessions": "number",
            "messages_sent": "number", "components_rendered": "number",
            "actions_triggered": "number", "avg_session_duration_min": "number",
            "bounce_rate": "number", "top_intents": "array",
        },
    },
]


async def seed_schema_registry():
    """Register all collection schemas."""
    client = AsyncIOMotorClient(MONGODB_URI)
    db = client[DB_NAME]

    logger.info("📋 Seeding schema registry for tenant '%s'...", TENANT_ID)

    coll = db["schema_registry"]
    await coll.delete_many({"tenant_id": TENANT_ID})

    now = datetime.now(timezone.utc)
    docs = [
        {"tenant_id": TENANT_ID, "registered_at": now, **schema}
        for schema in SCHEMAS
    ]

    await coll.insert_many(docs)
    logger.info("  ✅ Registered %d collection schemas", len(docs))

    client.close()


if __name__ == "__main__":
    asyncio.run(seed_schema_registry())
