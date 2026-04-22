"""Quick fix: directly update the demo-tenant's ai_persona in MongoDB."""
import asyncio
from motor.motor_asyncio import AsyncIOMotorClient

async def fix():
    client = AsyncIOMotorClient("mongodb://localhost:27017/?directConnection=true")
    result = await client["synaptiq"]["tenants"].update_one(
        {"tenant_id": "demo-tenant"},
        {"$set": {
            "ai_persona": {
                "display_name": "Aria",
                "tone": "friendly",
                "custom_instruction": "You are an AI-powered assistant. Help users explore data, view analytics, and manage their workspace.",
                "welcome_message": "Hi! 👋 I'm **Aria**, your AI-powered assistant. I can search, analyse, and visualise your data — all through this chat.",
                "starter_prompts": [
                    "Show me sales metrics",
                    "Search electronics",
                    "Compare the latest products",
                    "Show open support tickets",
                ],
            },
        }},
    )
    print(f"Matched: {result.matched_count}, Modified: {result.modified_count}")
    
    # Verify
    doc = await client["synaptiq"]["tenants"].find_one(
        {"tenant_id": "demo-tenant"}, {"_id": 0, "ai_persona": 1}
    )
    print(f"Verified: {doc}")

asyncio.run(fix())
