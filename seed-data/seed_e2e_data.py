"""
Synaptiq E2E Seed Script
========================
Populates MongoDB with realistic test data for end-to-end testing of all
platform components (charts, dashboards, kanban, timeline, etc.).

Usage:
    python -m synaptiq_api.scripts.seed_e2e_data

Requires: MongoDB running at the configured URI (default: mongodb://localhost:27017)
"""
import asyncio
import logging
import os
from datetime import datetime, timedelta, timezone
from motor.motor_asyncio import AsyncIOMotorClient

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

MONGODB_URI = os.environ.get("MONGODB_URI", "mongodb://localhost:27017/?directConnection=true")
DB_NAME = "synaptiq"
TENANT_ID = "demo-tenant"


async def seed():
    client = AsyncIOMotorClient(MONGODB_URI)
    db = client[DB_NAME]

    logger.info("🌱 Seeding E2E test data for tenant '%s'...", TENANT_ID)

    # ─── 0. Ensure tenant has AI persona config ────────────────────────
    await db["tenants"].update_one(
        {"tenant_id": TENANT_ID},
        {"$set": {
            "ai_persona": {
                "display_name": "Aria",
                "tone": "friendly",
                "custom_instruction": "You are an AI-powered catalog and analytics assistant. Help users explore products, view sales metrics, and manage their data.",
                "welcome_message": "Hi! 👋 I'm **Aria**, your AI-powered assistant. I can search your catalog, show analytics dashboards, and help you manage everything — all through this chat.",
                "starter_prompts": [
                    "Show me sales metrics",
                    "Search electronics",
                    "Compare the latest products",
                    "Show open support tickets",
                ],
            },
        }},
        upsert=False,
    )
    logger.info("  ✅ Updated tenant ai_persona config")

    # ─── 1. Products Collection ─────────────────────────────────────────
    products = db["products"]
    await products.delete_many({"tenant_id": TENANT_ID})
    product_docs = [
        {"tenant_id": TENANT_ID, "name": "Smart Speaker Pro", "category": "Electronics", "price": 79.99,
         "stock": 5, "rating": 4.7, "units_sold": 342, "revenue": 27356, "margin": 34,
         "description": "AI-powered smart speaker with premium audio", "created_at": datetime.now(timezone.utc)},
        {"tenant_id": TENANT_ID, "name": "Wireless Charger X", "category": "Electronics", "price": 34.99,
         "stock": 120, "rating": 4.3, "units_sold": 287, "revenue": 10032, "margin": 42,
         "description": "Fast wireless charging pad with LED indicator", "created_at": datetime.now(timezone.utc)},
        {"tenant_id": TENANT_ID, "name": "Premium Headphones", "category": "Audio", "price": 149.99,
         "stock": 45, "rating": 4.8, "units_sold": 198, "revenue": 29700, "margin": 38,
         "description": "Over-ear noise cancelling headphones", "created_at": datetime.now(timezone.utc)},
        {"tenant_id": TENANT_ID, "name": "USB-C Hub Ultra", "category": "Accessories", "price": 24.99,
         "stock": 200, "rating": 4.5, "units_sold": 456, "revenue": 11400, "margin": 52,
         "description": "7-in-1 USB-C hub with HDMI and ethernet", "created_at": datetime.now(timezone.utc)},
        {"tenant_id": TENANT_ID, "name": "Organic Cotton Tee", "category": "Clothing", "price": 24.99,
         "stock": 350, "rating": 4.6, "units_sold": 823, "revenue": 20575, "margin": 61,
         "description": "Sustainably sourced organic cotton t-shirt", "created_at": datetime.now(timezone.utc)},
        {"tenant_id": TENANT_ID, "name": "Bluetooth Keyboard", "category": "Electronics", "price": 59.99,
         "stock": 78, "rating": 4.4, "units_sold": 156, "revenue": 9358, "margin": 45,
         "description": "Slim wireless mechanical keyboard", "created_at": datetime.now(timezone.utc)},
        {"tenant_id": TENANT_ID, "name": "Yoga Mat Premium", "category": "Fitness", "price": 39.99,
         "stock": 95, "rating": 4.9, "units_sold": 412, "revenue": 16476, "margin": 55,
         "description": "Non-slip eco-friendly yoga mat", "created_at": datetime.now(timezone.utc)},
        {"tenant_id": TENANT_ID, "name": "LED Desk Lamp", "category": "Home", "price": 44.99,
         "stock": 67, "rating": 4.2, "units_sold": 234, "revenue": 10528, "margin": 48,
         "description": "Adjustable LED lamp with 5 color temperatures", "created_at": datetime.now(timezone.utc)},
    ]
    await products.insert_many(product_docs)
    logger.info("  ✅ Inserted %d products", len(product_docs))

    # ─── 2. Orders Collection ───────────────────────────────────────────
    orders = db["orders"]
    await orders.delete_many({"tenant_id": TENANT_ID})
    now = datetime.now(timezone.utc)
    order_docs = []
    statuses = ["pending", "processing", "shipped", "delivered", "returned"]
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

    # ─── 3. Sales Metrics (daily aggregates) ────────────────────────────
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

    # ─── 3b. Monthly Sales Summary (for bar/pie charts) ──────────────
    monthly_sales = db["monthly_sales"]
    await monthly_sales.delete_many({"tenant_id": TENANT_ID})
    months = [
        {"month": "Jan 2026", "revenue": 42500, "orders": 385, "profit": 14875, "growth": 0},
        {"month": "Feb 2026", "revenue": 48200, "orders": 412, "profit": 17352, "growth": 13.4},
        {"month": "Mar 2026", "revenue": 55800, "orders": 478, "profit": 20604, "growth": 15.8},
        {"month": "Apr 2026", "revenue": 61200, "orders": 524, "profit": 23256, "growth": 9.7},
    ]
    monthly_docs = [{"tenant_id": TENANT_ID, **m, "year": 2026} for m in months]
    await monthly_sales.insert_many(monthly_docs)
    logger.info("  ✅ Inserted %d monthly sales summaries", len(monthly_docs))

    # ─── 3c. Sales by Category (for pie/donut charts) ────────────────
    category_sales = db["category_sales"]
    await category_sales.delete_many({"tenant_id": TENANT_ID})
    cat_docs = [
        {"tenant_id": TENANT_ID, "category": "Electronics", "revenue": 78600, "units_sold": 1243, "pct_total": 37.8, "avg_price": 63.23, "top_product": "Smart Speaker Pro"},
        {"tenant_id": TENANT_ID, "category": "Audio", "revenue": 44850, "units_sold": 612, "pct_total": 21.6, "avg_price": 73.28, "top_product": "Premium Headphones"},
        {"tenant_id": TENANT_ID, "category": "Clothing", "revenue": 35420, "units_sold": 1417, "pct_total": 17.1, "avg_price": 25.00, "top_product": "Organic Cotton Tee"},
        {"tenant_id": TENANT_ID, "category": "Accessories", "revenue": 24800, "units_sold": 992, "pct_total": 11.9, "avg_price": 25.00, "top_product": "USB-C Hub Ultra"},
        {"tenant_id": TENANT_ID, "category": "Fitness", "revenue": 16476, "units_sold": 412, "pct_total": 7.9, "avg_price": 39.99, "top_product": "Yoga Mat Premium"},
        {"tenant_id": TENANT_ID, "category": "Home", "revenue": 7654, "units_sold": 170, "pct_total": 3.7, "avg_price": 45.02, "top_product": "LED Desk Lamp"},
    ]
    await category_sales.insert_many(cat_docs)
    logger.info("  ✅ Inserted %d category sales breakdowns", len(cat_docs))

    # ─── 3d. Regional Sales (for geographic dashboards) ──────────────
    regional_sales = db["regional_sales"]
    await regional_sales.delete_many({"tenant_id": TENANT_ID})
    region_docs = [
        {"tenant_id": TENANT_ID, "region": "North America", "revenue": 98500, "orders": 1842, "customers": 1205, "avg_order_value": 53.47},
        {"tenant_id": TENANT_ID, "region": "Europe", "revenue": 62300, "orders": 1156, "customers": 890, "avg_order_value": 53.89},
        {"tenant_id": TENANT_ID, "region": "Asia Pacific", "revenue": 31200, "orders": 623, "customers": 478, "avg_order_value": 50.08},
        {"tenant_id": TENANT_ID, "region": "Latin America", "revenue": 12800, "orders": 287, "customers": 215, "avg_order_value": 44.60},
        {"tenant_id": TENANT_ID, "region": "Middle East & Africa", "revenue": 3000, "orders": 91, "customers": 68, "avg_order_value": 32.97},
    ]
    await regional_sales.insert_many(region_docs)
    logger.info("  ✅ Inserted %d regional sales records", len(region_docs))

    # ─── 4. Tasks / Project Board ───────────────────────────────────────
    tasks = db["tasks"]
    await tasks.delete_many({"tenant_id": TENANT_ID})
    task_docs = [
        {"tenant_id": TENANT_ID, "title": "Define pricing tiers", "description": "Research competitor pricing", "status": "backlog", "priority": "medium", "assignee": "Sarah L.", "tags": ["pricing", "strategy"], "created_at": now - timedelta(days=14)},
        {"tenant_id": TENANT_ID, "title": "Create product photos", "description": "Schedule studio shoot for all 12 SKUs", "status": "backlog", "priority": "low", "assignee": "Mike R.", "tags": ["marketing"], "created_at": now - timedelta(days=12)},
        {"tenant_id": TENANT_ID, "title": "Build landing page", "description": "Responsive landing page with hero, features, and pricing", "status": "in_progress", "priority": "high", "assignee": "Alex K.", "tags": ["frontend", "urgent"], "due_date": (now + timedelta(days=4)).strftime("%Y-%m-%d"), "created_at": now - timedelta(days=7)},
        {"tenant_id": TENANT_ID, "title": "API integration", "description": "Connect payment gateway and inventory sync", "status": "in_progress", "priority": "urgent", "assignee": "Jordan M.", "tags": ["backend"], "due_date": (now + timedelta(days=2)).strftime("%Y-%m-%d"), "created_at": now - timedelta(days=10)},
        {"tenant_id": TENANT_ID, "title": "Write product descriptions", "description": "SEO-optimized descriptions for all products", "status": "review", "priority": "medium", "assignee": "Emily W.", "tags": ["content"], "created_at": now - timedelta(days=5)},
        {"tenant_id": TENANT_ID, "title": "Set up analytics tracking", "description": "GA4 + custom events", "status": "done", "priority": "high", "assignee": "Alex K.", "tags": ["analytics"], "created_at": now - timedelta(days=20)},
        {"tenant_id": TENANT_ID, "title": "Domain + SSL configured", "description": "DNS, SSL cert, CDN setup", "status": "done", "priority": "high", "assignee": "Jordan M.", "tags": ["devops"], "created_at": now - timedelta(days=25)},
        {"tenant_id": TENANT_ID, "title": "Email templates", "description": "Order confirmation, shipping, welcome emails", "status": "in_progress", "priority": "medium", "assignee": "Emily W.", "tags": ["email", "marketing"], "created_at": now - timedelta(days=3)},
    ]
    await tasks.insert_many(task_docs)
    logger.info("  ✅ Inserted %d tasks", len(task_docs))

    # ─── 5. Audit / Event Log ───────────────────────────────────────────
    events = db["events"]
    await events.delete_many({"tenant_id": TENANT_ID})
    event_docs = [
        {"tenant_id": TENANT_ID, "event_type": "order_placed", "title": "Order placed", "description": "Customer placed order for 3 items totaling $142.97", "actor": "Customer", "entity_type": "order", "entity_id": "ORD-2847", "timestamp": now - timedelta(hours=8)},
        {"tenant_id": TENANT_ID, "event_type": "payment_confirmed", "title": "Payment confirmed", "description": "Visa ending in 4242 — authorized and captured", "actor": "Payment Gateway", "entity_type": "order", "entity_id": "ORD-2847", "timestamp": now - timedelta(hours=7, minutes=59)},
        {"tenant_id": TENANT_ID, "event_type": "inventory_reserved", "title": "Inventory reserved", "description": "All 3 items reserved from warehouse A", "actor": "System", "entity_type": "order", "entity_id": "ORD-2847", "timestamp": now - timedelta(hours=7, minutes=15)},
        {"tenant_id": TENANT_ID, "event_type": "shipped", "title": "Shipped via FedEx", "description": "Tracking: FX-7829341 — Est. delivery Apr 24", "actor": "Warehouse Team", "entity_type": "order", "entity_id": "ORD-2847", "timestamp": now - timedelta(hours=5, minutes=20)},
        {"tenant_id": TENANT_ID, "event_type": "low_stock_alert", "title": "Low stock alert", "description": "Smart Speaker Pro down to 5 units — reorder triggered", "actor": "Inventory System", "entity_type": "product", "entity_id": "smart-speaker-pro", "timestamp": now - timedelta(hours=3, minutes=50)},
        {"tenant_id": TENANT_ID, "event_type": "support_note", "title": "Customer note", "description": "Customer requested gift wrapping via support chat", "actor": "Support Agent", "entity_type": "order", "entity_id": "ORD-2847", "timestamp": now - timedelta(hours=2, minutes=15)},
    ]
    await events.insert_many(event_docs)
    logger.info("  ✅ Inserted %d events", len(event_docs))

    # ─── 6. Support Tickets ─────────────────────────────────────────────
    tickets = db["support_tickets"]
    await tickets.delete_many({"tenant_id": TENANT_ID})
    ticket_docs = [
        {"tenant_id": TENANT_ID, "ticket_id": "TKT-101", "subject": "Order not received", "status": "open", "priority": "high", "customer_email": "john@example.com", "created_at": now - timedelta(days=1)},
        {"tenant_id": TENANT_ID, "ticket_id": "TKT-102", "subject": "Wrong item shipped", "status": "in_progress", "priority": "urgent", "customer_email": "jane@example.com", "created_at": now - timedelta(days=2)},
        {"tenant_id": TENANT_ID, "ticket_id": "TKT-103", "subject": "Refund request", "status": "resolved", "priority": "medium", "customer_email": "bob@example.com", "created_at": now - timedelta(days=5)},
    ]
    await tickets.insert_many(ticket_docs)
    logger.info("  ✅ Inserted %d support tickets", len(ticket_docs))

    # ─── 7. Schema Registry Entries ─────────────────────────────────────
    schema_registry = db["schema_registry"]
    await schema_registry.delete_many({"tenant_id": TENANT_ID})
    schemas = [
        {"tenant_id": TENANT_ID, "collection_name": "products", "display_name": "Products",
         "description": "Product catalog with pricing, stock, and sales data",
         "fields": {"name": "string", "category": "string", "price": "number", "stock": "number",
                    "rating": "number", "units_sold": "number", "revenue": "number", "margin": "number",
                    "description": "string", "created_at": "date"},
         "registered_at": now},
        {"tenant_id": TENANT_ID, "collection_name": "orders", "display_name": "Orders",
         "description": "Customer orders with items, totals, and status tracking",
         "fields": {"order_id": "string", "customer_name": "string", "customer_email": "string",
                    "items": "array", "total": "number", "status": "string", "created_at": "date", "shipped_at": "date"},
         "registered_at": now},
        {"tenant_id": TENANT_ID, "collection_name": "sales_metrics", "display_name": "Sales Metrics",
         "description": "Daily aggregated sales data including revenue, orders, sessions, and conversion rates",
         "fields": {"date": "string", "revenue": "number", "orders": "number", "sessions": "number",
                    "conversion_rate": "number", "avg_order_value": "number", "new_customers": "number",
                    "returning_customers": "number"},
         "registered_at": now},
        {"tenant_id": TENANT_ID, "collection_name": "tasks", "display_name": "Tasks",
         "description": "Project tasks with status, priority, assignees, and tags",
         "fields": {"title": "string", "description": "string", "status": "string", "priority": "string",
                    "assignee": "string", "tags": "array", "due_date": "string", "created_at": "date"},
         "registered_at": now},
        {"tenant_id": TENANT_ID, "collection_name": "events", "display_name": "Events",
         "description": "Audit log / event timeline for orders and system events",
         "fields": {"event_type": "string", "title": "string", "description": "string",
                    "actor": "string", "entity_type": "string", "entity_id": "string", "timestamp": "date"},
         "registered_at": now},
        {"tenant_id": TENANT_ID, "collection_name": "support_tickets", "display_name": "Support Tickets",
         "description": "Customer support tickets with status and priority",
         "fields": {"ticket_id": "string", "subject": "string", "status": "string",
                    "priority": "string", "customer_email": "string", "created_at": "date"},
         "registered_at": now},
        {"tenant_id": TENANT_ID, "collection_name": "monthly_sales", "display_name": "Monthly Sales",
         "description": "Monthly aggregated revenue, orders, profit, and growth percentage",
         "fields": {"month": "string", "year": "number", "revenue": "number", "orders": "number",
                    "profit": "number", "growth": "number"},
         "registered_at": now},
        {"tenant_id": TENANT_ID, "collection_name": "category_sales", "display_name": "Sales by Category",
         "description": "Revenue and units sold breakdown by product category with percentage of total",
         "fields": {"category": "string", "revenue": "number", "units_sold": "number",
                    "pct_total": "number", "avg_price": "number", "top_product": "string"},
         "registered_at": now},
        {"tenant_id": TENANT_ID, "collection_name": "regional_sales", "display_name": "Regional Sales",
         "description": "Revenue, orders, and customer counts by geographic region",
         "fields": {"region": "string", "revenue": "number", "orders": "number",
                    "customers": "number", "avg_order_value": "number"},
         "registered_at": now},
    ]
    await schema_registry.insert_many(schemas)
    logger.info("  ✅ Registered %d collection schemas", len(schemas))

    # ─── 8. Tenant Config ───────────────────────────────────────────────
    tenants = db["tenants"]
    await tenants.update_one(
        {"tenant_id": TENANT_ID},
        {"$set": {
            "tenant_id": TENANT_ID,
            "name": "Demo Store",
            "domain": "demo.synaptiq.app",
            "plan": "pro",
            "ai_config": {
                "provider": "gemini",
                "enabled_components": [
                    "item_card", "item_grid", "item_detail", "comparison_table",
                    "filter_summary", "result_count", "empty_state", "action_confirm",
                    "info_banner", "data_table", "form_input",
                    "kpi_card", "chart", "stat_grid", "kanban", "timeline",
                    "metric_table", "progress_tracker", "launchpad", "view",
                ],
            },
            "updated_at": now,
        }},
        upsert=True,
    )
    logger.info("  ✅ Tenant config upserted")

    logger.info("🎉 Seed complete! %d collections populated for tenant '%s'",
                7, TENANT_ID)

    client.close()


if __name__ == "__main__":
    asyncio.run(seed())
