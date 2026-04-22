import asyncio
from motor.motor_asyncio import AsyncIOMotorClient

async def check():
    client = AsyncIOMotorClient("mongodb://localhost:27017/?directConnection=true")
    doc = await client["synaptiq"]["tenants"].find_one(
        {"tenant_id": "demo-tenant"}, {"_id": 0, "ai_persona": 1}
    )
    # Avoid emoji encoding issues on Windows
    if doc and "ai_persona" in doc:
        p = doc["ai_persona"]
        print(f"display_name: {p.get('display_name')}")
        print(f"starter_prompts: {p.get('starter_prompts')}")
        print(f"welcome_message length: {len(p.get('welcome_message', ''))}")
    else:
        print("NO ai_persona found!")

asyncio.run(check())
