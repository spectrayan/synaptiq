import httpx
import asyncio

async def main():
    async with httpx.AsyncClient() as client:
        print("Sending request...")
        async with client.stream(
            "POST", 
            "http://localhost:8000/api/v1/chat/message",
            headers={"X-Tenant-ID": "demo-tenant"},
            json={"session_id": "test-session-123", "message": "Show me sales metrics"},
            timeout=None
        ) as response:
            print(f"Status: {response.status_code}")
            async for chunk in response.aiter_text():
                print(chunk, end="", flush=True)

if __name__ == "__main__":
    asyncio.run(main())
