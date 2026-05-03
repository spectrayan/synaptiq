"""
Seed default users for built-in auth.

Creates a default admin user with must_change_password=true.
Skips if the user already exists (idempotent).
"""
import asyncio
import logging
import os
from datetime import datetime, timezone

from motor.motor_asyncio import AsyncIOMotorClient
import bcrypt

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

MONGODB_URI = os.environ.get("MONGODB_URI", "mongodb://localhost:27017/?directConnection=true")
DB_NAME = "synaptiq"


def _hash_pw(plain: str) -> str:
    return bcrypt.hashpw(plain.encode("utf-8"), bcrypt.gensalt()).decode("utf-8")

DEFAULT_USERS = [
    {
        "email": "admin@synaptiq.dev",
        "password": "admin",
        "display_name": "Admin",
        "role": "platform_admin",
        "tenant_id": "demo-tenant",
        "must_change_password": True,
        "email_verified": True,
        "disabled": False,
    },
]


async def seed():
    """Seed default users into the users collection."""
    logger.info("👤 Seeding default users...")

    client = AsyncIOMotorClient(MONGODB_URI)
    db = client[DB_NAME]
    collection = db["users"]

    # Ensure unique index on email
    await collection.create_index("email", unique=True)

    for user_def in DEFAULT_USERS:
        existing = await collection.find_one({"email": user_def["email"]})
        if existing:
            logger.info("  ⏭ User '%s' already exists — skipping", user_def["email"])
            continue

        now = datetime.now(timezone.utc)
        doc = {
            "email": user_def["email"],
            "password_hash": _hash_pw(user_def["password"]),
            "display_name": user_def["display_name"],
            "role": user_def["role"],
            "tenant_id": user_def["tenant_id"],
            "must_change_password": user_def["must_change_password"],
            "email_verified": user_def["email_verified"],
            "disabled": user_def["disabled"],
            "created_at": now,
            "updated_at": now,
        }
        await collection.insert_one(doc)
        logger.info("  ✅ Created user '%s' (role=%s, must_change_password=%s)",
                     user_def["email"], user_def["role"], user_def["must_change_password"])

    client.close()
    logger.info("👤 User seed complete!")


if __name__ == "__main__":
    asyncio.run(seed())
