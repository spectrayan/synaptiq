import asyncio
from motor.motor_asyncio import AsyncIOMotorClient

async def main():
    client = AsyncIOMotorClient('mongodb://localhost:27017')
    await client.synaptiq.tenants.update_one({'tenant_id': 'demo-tenant'}, {'$set': {'config.llm_provider.model_id': 'gemini-1.5-pro'}})
    print('Done')

if __name__ == "__main__":
    asyncio.run(main())
