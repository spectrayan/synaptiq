"""
Synaptiq E2E Seed Script
========================
Populates MongoDB with realistic test data for end-to-end testing of all
platform components (charts, dashboards, kanban, timeline, etc.).

All static data is loaded from JSON files under seed-data/data/.

Usage:
    python -m synaptiq_api.scripts.seed_e2e_data

Requires: MongoDB running at the configured URI (default: mongodb://localhost:27017)
"""
import asyncio
import json
import logging
import os
from datetime import datetime, timedelta, timezone
from pathlib import Path
from motor.motor_asyncio import AsyncIOMotorClient

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

MONGODB_URI = os.environ.get("MONGODB_URI", "mongodb://localhost:27017/?directConnection=true")
DB_NAME = "synaptiq"
TENANT_ID = "demo-tenant"

# Resolve data directory relative to this file
DATA_DIR = Path(__file__).parent / "data"


def load_json(relative_path: str):
    """Load a JSON file from the data directory."""
    filepath = DATA_DIR / relative_path
    with open(filepath, "r", encoding="utf-8") as f:
        return json.load(f)


async def seed():
    client = AsyncIOMotorClient(MONGODB_URI)
    db = client[DB_NAME]

    logger.info("🌱 Seeding E2E test data for tenant '%s'...", TENANT_ID)

    now = datetime.now(timezone.utc)

    # ─── 0. Ensure tenant has AI persona config ────────────────────────
    tenant_config = load_json("tenant.json")
    await db["tenants"].update_one(
        {"tenant_id": TENANT_ID},
        {"$set": {
            "ai_persona": tenant_config["ai_persona"],
        }},
        upsert=False,
    )
    logger.info("  ✅ Updated tenant ai_persona config")

    # ─── 1. Products Collection ─────────────────────────────────────────
    products_data = load_json("products.json")
    products = db["products"]
    await products.delete_many({"tenant_id": TENANT_ID})
    product_docs = [
        {"tenant_id": TENANT_ID, **p, "created_at": now}
        for p in products_data
    ]
    await products.insert_many(product_docs)
    logger.info("  ✅ Inserted %d products", len(product_docs))

    # ─── 2. Orders Collection (dynamically generated) ───────────────────
    orders = db["orders"]
    await orders.delete_many({"tenant_id": TENANT_ID})
    statuses = ["pending", "processing", "shipped", "delivered", "returned"]
    order_docs = []
    for i in range(30):
        order_docs.append({
            "tenant_id": TENANT_ID,
            "order_id": f"ORD-{2800 + i}",
            "customer_name": f"Customer {i + 1}",
            "customer_email": f"customer{i + 1}@example.com",
            "items": [
                {"product_name": product_docs[i % len(product_docs)]["name"],
                 "quantity": (i % 3) + 1,
                 "price": product_docs[i % len(product_docs)]["price"]},
            ],
            "total": round(product_docs[i % len(product_docs)]["price"] * ((i % 3) + 1), 2),
            "status": statuses[i % len(statuses)],
            "created_at": now - timedelta(days=i, hours=i % 6),
            "shipped_at": (now - timedelta(days=max(0, i - 2))) if statuses[i % len(statuses)] in ("shipped", "delivered") else None,
        })
    await orders.insert_many(order_docs)
    logger.info("  ✅ Inserted %d orders", len(order_docs))

    # ─── 3. Sales Metrics (dynamically generated daily aggregates) ──────
    sales_metrics = db["sales_metrics"]
    await sales_metrics.delete_many({"tenant_id": TENANT_ID})
    metrics_docs = []
    for i in range(90):
        day = now - timedelta(days=89 - i)
        base_revenue = 3000 + (i * 30) + ((i % 7) * 200)
        metrics_docs.append({
            "tenant_id": TENANT_ID,
            "date": day.strftime("%Y-%m-%d"),
            "revenue": round(base_revenue + (i % 5) * 150, 2),
            "orders": 40 + (i % 15),
            "sessions": 800 + (i * 5) + (i % 10) * 50,
            "conversion_rate": round(3.5 + (i % 20) * 0.1, 2),
            "avg_order_value": round(base_revenue / (40 + (i % 15)), 2),
            "new_customers": 15 + (i % 8),
            "returning_customers": 25 + (i % 12),
        })
    await sales_metrics.insert_many(metrics_docs)
    logger.info("  ✅ Inserted %d daily sales metrics", len(metrics_docs))

    # ─── 3b. Monthly Sales Summary ──────────────────────────────────────
    monthly_sales_data = load_json("sales/monthly.json")
    monthly_sales = db["monthly_sales"]
    await monthly_sales.delete_many({"tenant_id": TENANT_ID})
    monthly_docs = [{"tenant_id": TENANT_ID, **m, "year": 2026} for m in monthly_sales_data]
    await monthly_sales.insert_many(monthly_docs)
    logger.info("  ✅ Inserted %d monthly sales summaries", len(monthly_docs))

    # ─── 3c. Sales by Category ──────────────────────────────────────────
    category_sales_data = load_json("sales/category.json")
    category_sales = db["category_sales"]
    await category_sales.delete_many({"tenant_id": TENANT_ID})
    cat_docs = [{"tenant_id": TENANT_ID, **c} for c in category_sales_data]
    await category_sales.insert_many(cat_docs)
    logger.info("  ✅ Inserted %d category sales breakdowns", len(cat_docs))

    # ─── 3d. Regional Sales ─────────────────────────────────────────────
    regional_sales_data = load_json("sales/regional.json")
    regional_sales = db["regional_sales"]
    await regional_sales.delete_many({"tenant_id": TENANT_ID})
    region_docs = [{"tenant_id": TENANT_ID, **r} for r in regional_sales_data]
    await regional_sales.insert_many(region_docs)
    logger.info("  ✅ Inserted %d regional sales records", len(region_docs))

    # ─── 4. Tasks / Project Board ───────────────────────────────────────
    tasks_data = load_json("tasks.json")
    tasks = db["tasks"]
    await tasks.delete_many({"tenant_id": TENANT_ID})
    task_docs = []
    for t in tasks_data:
        doc = {
            "tenant_id": TENANT_ID,
            "title": t["title"],
            "description": t["description"],
            "status": t["status"],
            "priority": t["priority"],
            "assignee": t["assignee"],
            "tags": t["tags"],
            "created_at": now - timedelta(days=t["created_days_ago"]),
        }
        if "due_days_from_now" in t:
            doc["due_date"] = (now + timedelta(days=t["due_days_from_now"])).strftime("%Y-%m-%d")
        task_docs.append(doc)
    await tasks.insert_many(task_docs)
    logger.info("  ✅ Inserted %d tasks", len(task_docs))

    # ─── 5. Audit / Event Log ───────────────────────────────────────────
    events_data = load_json("events.json")
    events = db["events"]
    await events.delete_many({"tenant_id": TENANT_ID})
    event_docs = [
        {
            "tenant_id": TENANT_ID,
            "event_type": e["event_type"],
            "title": e["title"],
            "description": e["description"],
            "actor": e["actor"],
            "entity_type": e["entity_type"],
            "entity_id": e["entity_id"],
            "timestamp": now - timedelta(hours=e["hours_ago"]),
        }
        for e in events_data
    ]
    await events.insert_many(event_docs)
    logger.info("  ✅ Inserted %d events", len(event_docs))

    # ─── 6. Support Tickets ─────────────────────────────────────────────
    tickets_data = load_json("support_tickets.json")
    tickets = db["support_tickets"]
    await tickets.delete_many({"tenant_id": TENANT_ID})
    ticket_docs = [
        {
            "tenant_id": TENANT_ID,
            "ticket_id": t["ticket_id"],
            "subject": t["subject"],
            "status": t["status"],
            "priority": t["priority"],
            "customer_email": t["customer_email"],
            "created_at": now - timedelta(days=t["days_ago"]),
        }
        for t in tickets_data
    ]
    await tickets.insert_many(ticket_docs)
    logger.info("  ✅ Inserted %d support tickets", len(ticket_docs))

    # ─── 7. Tenant Config ───────────────────────────────────────────────
    tenants = db["tenants"]
    await tenants.update_one(
        {"tenant_id": TENANT_ID},
        {"$set": {
            "tenant_id": TENANT_ID,
            "name": tenant_config["name"],
            "domain": tenant_config["domain"],
            "plan": tenant_config["plan"],
            "ai_config": tenant_config["ai_config"],
            "updated_at": now,
        }},
        upsert=True,
    )
    logger.info("  ✅ Tenant config upserted")

    # ─── 8. Applications ────────────────────────────────────────────────
    app_config = load_json("application.json")
    applications = db["applications"]
    await applications.update_one(
        {"appId": app_config["appId"]},
        {"$set": {**app_config, "createdAt": now}},
        upsert=True,
    )
    logger.info("  ✅ Application config upserted")

    logger.info("🎉 Seed complete! %d collections populated for tenant '%s'",
                8, TENANT_ID)

    client.close()


if __name__ == "__main__":
    asyncio.run(seed())
