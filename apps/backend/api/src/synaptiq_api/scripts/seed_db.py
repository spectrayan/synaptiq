"""MongoDB seed script — creates 1 demo tenant, schema, and 20 catalog items.

Usage:
    python -m synaptiq_api.scripts.seed_db

Requires MongoDB to be running (via docker-compose).
"""
import asyncio
import logging
from datetime import datetime

from motor.motor_asyncio import AsyncIOMotorClient

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

MONGODB_URI = "mongodb://localhost:27017"
DB_NAME = "synaptiq"


# ---------------------------------------------------------------------------
# Demo Tenant
# ---------------------------------------------------------------------------
DEMO_TENANT = {
    "tenant_id": "acme-demo",
    "name": "Acme Electronics",
    "slug": "acme",
    "catalog_label": "Products",
    "status": "active",
    "access_mode": "public",
    "limits": {
        "max_catalog_items": 10000,
        "max_monthly_tokens": 500000,
        "max_users": 50,
        "max_requests_per_minute": 60,
        "seat_cap": 100,
        "token_exceed_action": "warn_admin",
        "seat_exceed_action": "warn_admin",
    },
    "ai_persona": {
        "display_name": "Aria",
        "tone": "friendly",
        "custom_instruction": "You are an electronics shopping assistant. Be knowledgeable about tech specs and recommend products based on user needs.",
        "welcome_message": "Hi! I'm Aria, your electronics shopping assistant. I can help you find, compare, and explore products. What are you looking for?",
        "starter_prompts": [
            "Show me laptops under $1000",
            "What's your best-selling headphone?",
            "Compare the latest tablets",
            "I need a gift for a tech enthusiast",
        ],
    },
    "ai_guardrails": {
        "out_of_scope_message": "I'm here to help you find the perfect electronics product. What are you looking for?",
        "recommendation_mode": True,
        "language": "en",
    },
    "llm_provider": {
        "provider": "platform_managed",
        "model_id": "",
        "byok_encrypted_key": "",
        "is_byok": False,
    },
    "branding": {
        "logo_url": "",
        "primary_color": "#6366F1",
        "secondary_color": "#8B5CF6",
        "background_style": "dark",
        "heading_font": "Inter",
        "body_font": "Inter",
        "ui_font": "Inter",
        "favicon_url": "",
        "page_title": "Acme Electronics - Smart Shopping",
        "show_platform_branding": True,
    },
    "components": {
        "item_card": True,
        "item_grid": True,
        "item_detail": True,
        "comparison_table": True,
        "filter_summary": True,
        "result_count": True,
        "empty_state": True,
        "action_confirm": True,
        "info_banner": True,
    },
    "actions": {
        "actions": [
            {"action_id": "save_item", "enabled": True, "label": "Save to Wishlist"},
            {"action_id": "share_item", "enabled": True, "label": "Share"},
            {"action_id": "contact_enquiry", "enabled": True, "label": "Contact Us"},
            {"action_id": "view_external", "enabled": True, "label": "View on Store"},
            {"action_id": "show_more", "enabled": True, "label": "More Details"},
        ],
        "enquiry_webhook_url": "",
        "enquiry_email": "sales@acme-demo.com",
    },
    "personalization": {
        "allow_theme_switch": True,
        "allow_font_switch": False,
        "allow_bubble_style": True,
    },
    "admins": [],
    "created_at": datetime.utcnow(),
    "updated_at": datetime.utcnow(),
}


# ---------------------------------------------------------------------------
# Demo Schema
# ---------------------------------------------------------------------------
DEMO_SCHEMA = {
    "tenant_id": "acme-demo",
    "name": "Electronics Catalog",
    "version": 1,
    "is_active": True,
    "fields": [
        {
            "field_id": "name",
            "label": "Product Name",
            "type": "text",
            "required": True,
            "searchable": True,
            "displayable": True,
            "filterable": False,
            "sortable": True,
            "unit": "",
            "visibility": "public",
            "designator": "primary_label",
            "deprecated": False,
            "display_order": 0,
            "enum_values": [],
        },
        {
            "field_id": "price",
            "label": "Price",
            "type": "currency",
            "required": True,
            "searchable": True,
            "displayable": True,
            "filterable": True,
            "sortable": True,
            "unit": "USD",
            "visibility": "public",
            "designator": "primary_price",
            "deprecated": False,
            "display_order": 1,
            "enum_values": [],
        },
        {
            "field_id": "category",
            "label": "Category",
            "type": "enum",
            "required": True,
            "searchable": True,
            "displayable": True,
            "filterable": True,
            "sortable": True,
            "unit": "",
            "visibility": "public",
            "designator": "none",
            "deprecated": False,
            "display_order": 2,
            "enum_values": ["Laptops", "Headphones", "Tablets", "Phones", "Accessories"],
        },
        {
            "field_id": "brand",
            "label": "Brand",
            "type": "text",
            "required": True,
            "searchable": True,
            "displayable": True,
            "filterable": True,
            "sortable": True,
            "unit": "",
            "visibility": "public",
            "designator": "none",
            "deprecated": False,
            "display_order": 3,
            "enum_values": [],
        },
        {
            "field_id": "description",
            "label": "Description",
            "type": "rich_text",
            "required": False,
            "searchable": True,
            "displayable": True,
            "filterable": False,
            "sortable": False,
            "unit": "",
            "visibility": "public",
            "designator": "none",
            "deprecated": False,
            "display_order": 4,
            "enum_values": [],
        },
        {
            "field_id": "image",
            "label": "Image",
            "type": "image_url",
            "required": False,
            "searchable": False,
            "displayable": True,
            "filterable": False,
            "sortable": False,
            "unit": "",
            "visibility": "public",
            "designator": "primary_image",
            "deprecated": False,
            "display_order": 5,
            "enum_values": [],
        },
        {
            "field_id": "rating",
            "label": "Rating",
            "type": "number",
            "required": False,
            "searchable": False,
            "displayable": True,
            "filterable": True,
            "sortable": True,
            "unit": "",
            "visibility": "public",
            "designator": "none",
            "deprecated": False,
            "display_order": 6,
            "enum_values": [],
        },
        {
            "field_id": "in_stock",
            "label": "In Stock",
            "type": "boolean",
            "required": False,
            "searchable": False,
            "displayable": True,
            "filterable": True,
            "sortable": False,
            "unit": "",
            "visibility": "public",
            "designator": "none",
            "deprecated": False,
            "display_order": 7,
            "enum_values": [],
        },
        {
            "field_id": "cost_price",
            "label": "Cost Price",
            "type": "currency",
            "required": False,
            "searchable": False,
            "displayable": False,
            "filterable": False,
            "sortable": False,
            "unit": "USD",
            "visibility": "admin_only",
            "designator": "none",
            "deprecated": False,
            "display_order": 8,
            "enum_values": [],
        },
    ],
    "created_at": datetime.utcnow(),
    "updated_at": datetime.utcnow(),
}


# ---------------------------------------------------------------------------
# Demo Catalog Items (20 items)
# ---------------------------------------------------------------------------
DEMO_ITEMS = [
    {"name": "ProBook Ultra 15", "price": 899.99, "category": "Laptops", "brand": "TechVault", "description": "15.6\" FHD display, Intel i7-13700H, 16GB RAM, 512GB SSD. Perfect for productivity and light gaming.", "rating": 4.5, "in_stock": True, "cost_price": 620},
    {"name": "ProBook Air 13", "price": 699.99, "category": "Laptops", "brand": "TechVault", "description": "Ultra-thin 13.3\" laptop with M2 chip, 8GB RAM, 256GB SSD. All-day battery life.", "rating": 4.7, "in_stock": True, "cost_price": 480},
    {"name": "ZenBook Creator 16", "price": 1499.99, "category": "Laptops", "brand": "Zenith", "description": "16\" 4K OLED display, RTX 4060, 32GB RAM. Built for creative professionals.", "rating": 4.8, "in_stock": True, "cost_price": 1050},
    {"name": "SoundMax Pro ANC", "price": 299.99, "category": "Headphones", "brand": "AudioPeak", "description": "Premium over-ear headphones with adaptive ANC, 40hr battery, and Hi-Res audio certification.", "rating": 4.6, "in_stock": True, "cost_price": 145},
    {"name": "SoundMax Buds Ultra", "price": 179.99, "category": "Headphones", "brand": "AudioPeak", "description": "True wireless earbuds with spatial audio, 8hr battery, and IPX5 water resistance.", "rating": 4.3, "in_stock": True, "cost_price": 78},
    {"name": "FlexPad Pro 11", "price": 549.99, "category": "Tablets", "brand": "Zenith", "description": "11\" Liquid Retina display, A16 chip, supports stylus and keyboard. Perfect for note-taking.", "rating": 4.4, "in_stock": True, "cost_price": 380},
    {"name": "FlexPad Max 12.9", "price": 999.99, "category": "Tablets", "brand": "Zenith", "description": "12.9\" mini-LED XDR display, M2 chip, Thunderbolt port. The ultimate tablet for pros.", "rating": 4.9, "in_stock": False, "cost_price": 720},
    {"name": "NovaPulse X1", "price": 799.99, "category": "Phones", "brand": "NovaTech", "description": "6.7\" AMOLED 120Hz, Snapdragon 8 Gen 3, 256GB, 50MP triple camera. Flagship performance.", "rating": 4.5, "in_stock": True, "cost_price": 480},
    {"name": "NovaPulse Lite", "price": 399.99, "category": "Phones", "brand": "NovaTech", "description": "6.1\" OLED display, 128GB storage, 48MP camera. Great value mid-range phone.", "rating": 4.2, "in_stock": True, "cost_price": 210},
    {"name": "NovaPulse Mini", "price": 599.99, "category": "Phones", "brand": "NovaTech", "description": "5.4\" compact design, flagship specs in a pocket-friendly form factor.", "rating": 4.3, "in_stock": True, "cost_price": 350},
    {"name": "ChargeMaster 65W GaN", "price": 49.99, "category": "Accessories", "brand": "PowerCore", "description": "Ultra-compact 65W GaN charger with 3 ports (2x USB-C, 1x USB-A). Charges laptop and phone simultaneously.", "rating": 4.7, "in_stock": True, "cost_price": 18},
    {"name": "FlexStand Pro", "price": 79.99, "category": "Accessories", "brand": "ErgoPro", "description": "Adjustable aluminum laptop stand with cable management. Supports up to 17\" laptops.", "rating": 4.4, "in_stock": True, "cost_price": 28},
    {"name": "SoundMax Studio", "price": 249.99, "category": "Headphones", "brand": "AudioPeak", "description": "Studio reference headphones with flat frequency response. Ideal for music production.", "rating": 4.5, "in_stock": True, "cost_price": 120},
    {"name": "KeyFlow Mechanical 75%", "price": 129.99, "category": "Accessories", "brand": "TechVault", "description": "Hot-swappable mechanical keyboard with RGB, gasket mount, and PBT keycaps.", "rating": 4.6, "in_stock": True, "cost_price": 52},
    {"name": "ProBook Gaming 16", "price": 1299.99, "category": "Laptops", "brand": "TechVault", "description": "16\" QHD 240Hz, RTX 4070, 32GB RAM, 1TB SSD. Built for competitive gaming.", "rating": 4.7, "in_stock": True, "cost_price": 890},
    {"name": "FlexPad Reader", "price": 279.99, "category": "Tablets", "brand": "Zenith", "description": "E-ink 10.3\" tablet for reading and note-taking. Paper-like feel with weeks of battery.", "rating": 4.1, "in_stock": True, "cost_price": 150},
    {"name": "NovaPulse Pro 5G", "price": 1099.99, "category": "Phones", "brand": "NovaTech", "description": "6.8\" QHD+, titanium frame, 200MP camera, S Pen included. The ultimate pro phone.", "rating": 4.8, "in_stock": False, "cost_price": 680},
    {"name": "WirelessPad Qi2", "price": 39.99, "category": "Accessories", "brand": "PowerCore", "description": "15W MagSafe-compatible wireless charger with alignment magnets and LED indicator.", "rating": 4.3, "in_stock": True, "cost_price": 12},
    {"name": "CloudStor 2TB SSD", "price": 149.99, "category": "Accessories", "brand": "DataVault", "description": "Portable NVMe SSD with 2000MB/s read speed, USB-C, and IP67 water resistance.", "rating": 4.6, "in_stock": True, "cost_price": 68},
    {"name": "SoundMax Kids", "price": 49.99, "category": "Headphones", "brand": "AudioPeak", "description": "Volume-limited (85dB) wireless headphones for children. 24hr battery, foldable design.", "rating": 4.4, "in_stock": True, "cost_price": 15},
]


async def seed() -> None:
    """Seed the database with demo data."""
    client = AsyncIOMotorClient(MONGODB_URI)
    db = client[DB_NAME]

    # --- Clean existing demo data ---
    await db.tenants.delete_many({"tenant_id": "acme-demo"})
    await db.catalog_schemas.delete_many({"tenant_id": "acme-demo"})
    await db.catalog_items.delete_many({"tenant_id": "acme-demo"})
    logger.info("Cleaned existing demo data")

    # --- Insert tenant ---
    await db.tenants.insert_one(DEMO_TENANT)
    logger.info("✓ Inserted demo tenant: acme-demo")

    # --- Insert schema ---
    await db.catalog_schemas.insert_one(DEMO_SCHEMA)
    logger.info("✓ Inserted demo schema: Electronics Catalog")

    # --- Insert items ---
    now = datetime.utcnow()
    items = [
        {
            "tenant_id": "acme-demo",
            "status": "active",
            "data": item_data,
            "embedding": None,
            "embedding_model": "",
            "embedded_at": None,
            "created_at": now,
            "updated_at": now,
        }
        for item_data in DEMO_ITEMS
    ]
    result = await db.catalog_items.insert_many(items)
    logger.info("✓ Inserted %d demo catalog items", len(result.inserted_ids))

    # --- Create indexes (T1.9) ---
    await db.tenants.create_index("tenant_id", unique=True)
    await db.tenants.create_index("slug", unique=True)

    await db.catalog_schemas.create_index([("tenant_id", 1), ("is_active", 1)])

    await db.catalog_items.create_index("tenant_id")
    await db.catalog_items.create_index([("tenant_id", 1), ("status", 1)])

    await db.sessions.create_index("session_id", unique=True)
    await db.sessions.create_index([("tenant_id", 1), ("session_id", 1)])
    await db.sessions.create_index("expires_at", expireAfterSeconds=0)

    await db.saved_items.create_index([("tenant_id", 1), ("session_id", 1)])
    await db.saved_items.create_index([("tenant_id", 1), ("item_id", 1)])

    await db.action_logs.create_index([("tenant_id", 1), ("created_at", -1)])
    await db.action_logs.create_index([("tenant_id", 1), ("action_id", 1)])

    await db.usage_ledger.create_index([("tenant_id", 1), ("event_type", 1), ("created_at", -1)])
    await db.usage_ledger.create_index([("tenant_id", 1), ("created_at", -1)])

    logger.info("✓ Created MongoDB indexes for all collections")

    client.close()
    logger.info("🎉 Seed complete!")


if __name__ == "__main__":
    asyncio.run(seed())
