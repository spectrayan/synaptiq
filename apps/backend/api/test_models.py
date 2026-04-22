import asyncio
import os
from langchain_google_vertexai import ChatVertexAI

async def main():
    models = ["gemini-1.5-flash", "gemini-1.5-pro", "gemini-1.5-flash-001", "gemini-1.5-pro-001", "gemini-1.0-pro"]
    project = os.environ.get("VERTEXAI_PROJECT", "elevate-x-2d43c")
    location = os.environ.get("VERTEXAI_LOCATION", "us-central1")
    
    for model_name in models:
        print(f"Testing {model_name}...")
        try:
            llm = ChatVertexAI(model_name=model_name, project=project, location=location)
            res = await llm.ainvoke("Hello")
            print(f"Success with {model_name}: {res.content}")
            return
        except Exception as e:
            print(f"Failed {model_name}: {e}")

if __name__ == "__main__":
    asyncio.run(main())
