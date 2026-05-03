from __future__ import annotations

import json
from typing import Any, Dict

from langchain_core.tools import tool

from agent_flow.builder.builder import build_workflow, parse_spec_json, validate_graph_spec


@tool
def validate_and_build_flow(spec_json: str) -> str:
    """Validate a workflow JSON (settings or graph) and attempt to build/compile it.

    Returns a brief status message. Useful as a tool inside LangGraph to check
    or prepare downstream execution artifacts.
    """
    # Detect whether this is a graph-spec or legacy settings
    parts: list[str] = []
    try:
        data = json.loads(spec_json)
    except Exception:
        data = None

    graph_or_runner = None
    flow_id = None
    flow_type = None
    entry = None

    if isinstance(data, dict) and ("nodes" in data or "entry_point" in data):
        # Validate graph spec and require embedded settings
        validate_graph_spec(data)
        settings = data.get("settings")
        if not settings:
            raise ValueError("Graph spec provided without embedded 'settings'")
        graph_or_runner, settings_model, _ctx = build_workflow(data, settings_json=settings)
        flow_id = settings_model.id
        flow_type = settings_model.flowType
        entry = data.get("entry_point") or data.get("entrypoint")
    else:
        # Legacy settings path
        spec = parse_spec_json(spec_json)
        graph_or_runner, _spec, _ctx = build_workflow(spec.model_dump())
        flow_id = spec.id
        flow_type = spec.flowType
        entry = spec.entrypoint

    # Provide a compact summary to the caller (as plain text)
    parts.append(f"flow_id={flow_id}")
    parts.append(f"flow_type={flow_type}")
    parts.append(f"entrypoint={entry}")
    if hasattr(graph_or_runner, "invoke"):
        parts.append("compiled=graph")
    else:
        parts.append("compiled=coordinator")
    return ", ".join(parts)


@tool
def build_workflow_tool(specification: str) -> str:
    """Build an executable workflow from a FlowSettings specification.
    
    Args:
        specification: JSON string containing FlowSettings specification
        
    Returns:
        JSON string with build result and status
    """
    try:
        # Parse the specification
        spec_dict = json.loads(specification)
        
        # Create a proper FlowSpec format for building
        flow_spec = {
            "entry_point": spec_dict.get("entrypoint"),
            "nodes": {},
            "edges": spec_dict.get("edges", []),
            "settings": spec_dict
        }
        
        # Create nodes from agents
        for agent in spec_dict.get("agents", []):
            agent_id = agent["id"]
            flow_spec["nodes"][agent_id] = {
                "type": "agent",
                "agent": agent_id
            }
        
        # Build the workflow
        graph_or_runner, settings_model, ctx = build_workflow(flow_spec)
        
        return json.dumps({
            "status": "success",
            "flow_id": settings_model.id,
            "flow_type": str(settings_model.flowType),
            "entrypoint": settings_model.entrypoint,
            "agent_count": len(settings_model.agents),
            "has_tools": any(len(agent.tools) > 0 for agent in settings_model.agents),
            "executable": hasattr(graph_or_runner, "invoke") or hasattr(graph_or_runner, "stream"),
            "message": "Workflow built successfully"
        })
        
    except Exception as e:
        return json.dumps({
            "status": "error",
            "error": f"Failed to build workflow: {e}"
        })
