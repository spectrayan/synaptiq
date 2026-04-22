"""
E2E Test Setup Script
- Updates ai_persona in MongoDB
- Flushes Redis tenant cache
- Creates admin + normal test users in Firebase Auth emulator
"""
import asyncio
import os
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

os.environ["FIREBASE_AUTH_EMULATOR_HOST"] = "127.0.0.1:9099"


async def setup():
    # 1. Fix MongoDB ai_persona
    from motor.motor_asyncio import AsyncIOMotorClient
    client = AsyncIOMotorClient("mongodb://localhost:27017/?directConnection=true")
    result = await client["synaptiq"]["tenants"].update_one(
        {"tenant_id": "demo-tenant"},
        {"$set": {
            "ai_persona": {
                "display_name": "Aria",
                "tone": "friendly",
                "custom_instruction": "You are an AI-powered assistant.",
                "welcome_message": "Hi! I'm Aria, your AI-powered assistant. I can search, analyse, and visualise your data.",
                "starter_prompts": [
                    "Show me sales metrics",
                    "Search electronics",
                    "Compare the latest products",
                    "Show open support tickets",
                ],
            },
        }},
    )
    logger.info("MongoDB: matched=%d, modified=%d", result.matched_count, result.modified_count)

    # 2. Flush Redis tenant cache
    try:
        import redis.asyncio as aioredis
        r = aioredis.from_url("redis://localhost:6379")
        deleted = await r.delete("tenant:demo-tenant")
        logger.info("Redis: deleted %d cache keys", deleted)
        await r.aclose()
    except Exception as e:
        logger.warning("Redis flush failed (may not be running): %s", e)

    # 3. Verify the API returns correct data
    import urllib.request
    import json
    req = urllib.request.Request(
        "http://localhost:8000/api/v1/config/branding/public",
        headers={"X-Tenant-ID": "demo-tenant"},
    )
    with urllib.request.urlopen(req) as resp:
        data = json.loads(resp.read())
        persona = data.get("ai_persona", {})
        logger.info("API verify - display_name: %s", persona.get("display_name"))
        logger.info("API verify - starter_prompts: %s", persona.get("starter_prompts"))
        logger.info("API verify - welcome_message present: %s", bool(persona.get("welcome_message")))

    # 4. Create Firebase Auth emulator users
    import firebase_admin
    from firebase_admin import auth

    try:
        firebase_admin.get_app()
    except ValueError:
        firebase_admin.initialize_app(options={"projectId": "synaptiq-platform"})

    # Clear existing users
    try:
        import urllib.request
        req = urllib.request.Request(
            "http://127.0.0.1:9099/emulator/v1/projects/synaptiq-platform/accounts",
            method="DELETE",
        )
        urllib.request.urlopen(req)
        logger.info("Firebase: cleared all emulator accounts")
    except Exception as e:
        logger.warning("Firebase: clear failed: %s", e)

    # Create admin user
    try:
        u1 = auth.create_user(email="admin@synaptiq.com", password="admin123456", display_name="Admin")
        auth.set_custom_user_claims(u1.uid, {"role": "tenant_admin", "tenant_id": "demo-tenant"})
        logger.info("Firebase: admin user created (uid=%s)", u1.uid)
    except Exception as e:
        logger.warning("Firebase: admin creation failed: %s", e)

    # Create normal user
    try:
        u2 = auth.create_user(email="user@synaptiq.com", password="user123456", display_name="Alex")
        auth.set_custom_user_claims(u2.uid, {"role": "member", "tenant_id": "demo-tenant"})
        logger.info("Firebase: normal user created (uid=%s)", u2.uid)
    except Exception as e:
        logger.warning("Firebase: normal user creation failed: %s", e)

    logger.info("Setup complete!")


asyncio.run(setup())
