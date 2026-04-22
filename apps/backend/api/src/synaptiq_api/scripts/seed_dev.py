"""Seed script — bootstraps the development database with demo tenant and sample data.

Usage:
    python -m synaptiq_api.scripts.seed_dev

    Or via Docker:
    docker exec synaptiq-api python -m synaptiq_api.scripts.seed_dev
"""
import asyncio
import logging
from datetime import datetime, timezone

from synaptiq_api.core.config import settings
from synaptiq_api.core.mongodb import connect_db, get_db

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


DEMO_TENANT = {
    "tenant_id": "demo-tenant",
    "name": "Demo Store",
    "slug": "demo-tenant",
    "status": "active",
    "owner_email": "admin@demo-tenant.com",
    "created_at": datetime.now(timezone.utc).isoformat(),
    "config": {
        "llm_provider": {
            "provider": "platform_managed",
            "model_id": "",
        },
        "branding": {
            "primary_color": "#b39ddb",
            "logo_url": "",
            "company_name": "Demo Store",
        },
    },
    "limits": {
        "monthly_messages": 10000,
        "max_sessions_per_day": 100,
        "max_catalog_items": 5000,
    },
    "schema": {
        "schema_id": "demo-electronics-v1",
        "fields": [
            {"name": "name", "type": "string", "label": "Product Name", "required": True, "searchable": True},
            {"name": "description", "type": "string", "label": "Description", "searchable": True},
            {"name": "price", "type": "number", "label": "Price", "unit": "USD"},
            {"name": "category", "type": "string", "label": "Category", "filterable": True},
            {"name": "brand", "type": "string", "label": "Brand", "filterable": True},
            {"name": "rating", "type": "number", "label": "Rating", "min": 0, "max": 5},
            {"name": "in_stock", "type": "boolean", "label": "In Stock", "filterable": True},
            {"name": "image_url", "type": "string", "label": "Image URL"},
        ],
    },
}


DEMO_CATALOG_ITEMS = [
    {
        "tenant_id": "demo-tenant",
        "item_id": "sku-101",
        "data": {
            "name": "Smart Speaker Pro",
            "description": "Premium smart speaker with 360° sound, voice assistant, and multi-room support.",
            "price": 79.99,
            "category": "Electronics",
            "brand": "SonicAI",
            "rating": 4.6,
            "in_stock": True,
            "image_url": "",
        },
        "created_at": datetime.now(timezone.utc).isoformat(),
    },
    {
        "tenant_id": "demo-tenant",
        "item_id": "sku-102",
        "data": {
            "name": "Wireless Charger Pad",
            "description": "Qi-certified 15W fast wireless charging pad with LED indicator and anti-slip design.",
            "price": 34.99,
            "category": "Electronics",
            "brand": "ChargeTech",
            "rating": 4.3,
            "in_stock": True,
            "image_url": "",
        },
        "created_at": datetime.now(timezone.utc).isoformat(),
    },
    {
        "tenant_id": "demo-tenant",
        "item_id": "sku-103",
        "data": {
            "name": "USB-C Hub 7-in-1",
            "description": "Multiport adapter with HDMI 4K, USB 3.0, SD card reader, and 100W PD passthrough.",
            "price": 49.99,
            "category": "Electronics",
            "brand": "ConnectPro",
            "rating": 4.7,
            "in_stock": True,
            "image_url": "",
        },
        "created_at": datetime.now(timezone.utc).isoformat(),
    },
    {
        "tenant_id": "demo-tenant",
        "item_id": "sku-104",
        "data": {
            "name": "Noise Cancelling Earbuds",
            "description": "True wireless ANC earbuds with 8h battery, transparency mode, and IPX5 water resistance.",
            "price": 129.99,
            "category": "Electronics",
            "brand": "SonicAI",
            "rating": 4.5,
            "in_stock": True,
            "image_url": "",
        },
        "created_at": datetime.now(timezone.utc).isoformat(),
    },
    {
        "tenant_id": "demo-tenant",
        "item_id": "sku-105",
        "data": {
            "name": "Mechanical Keyboard",
            "description": "RGB mechanical keyboard with hot-swappable Cherry MX switches and USB-C connectivity.",
            "price": 89.99,
            "category": "Electronics",
            "brand": "KeyCraft",
            "rating": 4.8,
            "in_stock": True,
            "image_url": "",
        },
        "created_at": datetime.now(timezone.utc).isoformat(),
    },
    {
        "tenant_id": "demo-tenant",
        "item_id": "sku-106",
        "data": {
            "name": "Portable Monitor 15.6\"",
            "description": "Full HD IPS portable monitor with USB-C and mini HDMI. Perfect for remote work.",
            "price": 219.99,
            "category": "Electronics",
            "brand": "ViewMax",
            "rating": 4.4,
            "in_stock": False,
            "image_url": "",
        },
        "created_at": datetime.now(timezone.utc).isoformat(),
    },
    {
        "tenant_id": "demo-tenant",
        "item_id": "sku-107",
        "data": {
            "name": "Ergonomic Mouse",
            "description": "Vertical ergonomic wireless mouse with adjustable DPI, silent clicks, and Bluetooth 5.0.",
            "price": 39.99,
            "category": "Electronics",
            "brand": "ComfortTech",
            "rating": 4.2,
            "in_stock": True,
            "image_url": "",
        },
        "created_at": datetime.now(timezone.utc).isoformat(),
    },
    {
        "tenant_id": "demo-tenant",
        "item_id": "sku-108",
        "data": {
            "name": "Running Shoes UltraLight",
            "description": "Lightweight running shoes with responsive cushioning and breathable mesh upper.",
            "price": 119.99,
            "category": "Clothing",
            "brand": "StridePro",
            "rating": 4.6,
            "in_stock": True,
            "image_url": "",
        },
        "created_at": datetime.now(timezone.utc).isoformat(),
    },
    {
        "tenant_id": "demo-tenant",
        "item_id": "sku-109",
        "data": {
            "name": "Standing Desk Converter",
            "description": "Height adjustable sit-stand desk converter with dual monitor support and keyboard tray.",
            "price": 299.99,
            "category": "Home & Garden",
            "brand": "ErgoRise",
            "rating": 4.1,
            "in_stock": True,
            "image_url": "",
        },
        "created_at": datetime.now(timezone.utc).isoformat(),
    },
    {
        "tenant_id": "demo-tenant",
        "item_id": "sku-110",
        "data": {
            "name": "Smart LED Bulb (4-pack)",
            "description": "WiFi smart LED bulbs with 16M colors, schedules, and voice assistant integration.",
            "price": 29.99,
            "category": "Home & Garden",
            "brand": "LumiSmart",
            "rating": 4.3,
            "in_stock": True,
            "image_url": "",
        },
        "created_at": datetime.now(timezone.utc).isoformat(),
    },
]


async def seed() -> None:
    """Seed the development database."""
    await connect_db()
    db = get_db()

    # ── Tenant ────────────────────────────────────────────────────────
    existing = await db.tenants.find_one({"tenant_id": DEMO_TENANT["tenant_id"]})
    if existing:
        logger.info("Demo tenant already exists — updating...")
        await db.tenants.replace_one(
            {"tenant_id": DEMO_TENANT["tenant_id"]},
            DEMO_TENANT,
        )
    else:
        await db.tenants.insert_one(DEMO_TENANT)
    logger.info("✅ Tenant '%s' seeded", DEMO_TENANT["tenant_id"])

    # ── Catalog items ─────────────────────────────────────────────────
    for item in DEMO_CATALOG_ITEMS:
        await db.catalog_items.update_one(
            {"tenant_id": item["tenant_id"], "item_id": item["item_id"]},
            {"$set": item},
            upsert=True,
        )
    logger.info("✅ Seeded %d catalog items", len(DEMO_CATALOG_ITEMS))

    # ── Indexes ───────────────────────────────────────────────────────
    await db.tenants.create_index("tenant_id", unique=True)
    await db.catalog_items.create_index([("tenant_id", 1), ("item_id", 1)], unique=True)
    await db.catalog_items.create_index([("tenant_id", 1), ("data.category", 1)])
    await db.sessions.create_index([("tenant_id", 1), ("session_id", 1)], unique=True)
    await db.usage_ledger.create_index([("tenant_id", 1), ("timestamp", -1)])
    logger.info("✅ Database indexes created")

    logger.info("🎉 Seed complete — database ready for development")


if __name__ == "__main__":
    asyncio.run(seed())
