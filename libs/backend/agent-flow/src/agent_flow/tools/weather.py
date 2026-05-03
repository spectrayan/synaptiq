from __future__ import annotations

from langchain_core.tools import tool


@tool
def search(query: str) -> str:
    """Simulates a web search. Use it get information on weather"""
    q = (query or "").lower()
    if "sf" in q or "san francisco" in q:
        return "It's 60 degrees and foggy."
    return "It's 90 degrees and sunny."
