import asyncio
from motor.motor_asyncio import AsyncIOMotorClient

async def check():
    client = AsyncIOMotorClient("mongodb://localhost:27017/?directConnection=true")
    doc = await client["synaptiq"]["tenants"].find_one(
        {"tenant_id": "demo-tenant"}, {"_id": 0, "ai_persona": 1}
    )
    print("ai_persona:", doc)

asyncio.run(check())
