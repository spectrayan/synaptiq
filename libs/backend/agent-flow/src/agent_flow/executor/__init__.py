"""Execution engine for agentic flows using LangChain and LangGraph.

Public API:
- parse_spec_json: parse a JSON string/object into FlowSettings
- FlowExecutor: lifecycle API (start, stream, cancel, result)
"""
from agent_flow.builder.models.settings import FlowSettings
from .engine import FlowExecutor

__all__ = [
    "FlowSettings",
    "FlowExecutor",
]
