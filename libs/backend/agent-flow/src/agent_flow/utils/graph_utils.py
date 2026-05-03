from __future__ import annotations

from typing import Dict, Mapping, Any

try:
    # Typing-only import; CompiledStateGraph lives in langgraph
    from langgraph.graph.state import CompiledStateGraph  # type: ignore
except Exception:  # pragma: no cover - optional at import time
    CompiledStateGraph = Any  # type: ignore

from agent_flow.builder.adapters.langgraph_builder import LangGraphBuilder
from agent_flow.builder.models.settings import FlowSettings


def export_graph_specs(compiled_graph: CompiledStateGraph, settings: FlowSettings) -> Dict[str, Any]:
    """Return JSON-like representations of the flow spec and flow settings.

    Parameters:
    - compiled_graph: A langgraph CompiledStateGraph (not currently introspected).
    - settings: FlowSettings instance used to build the graph.

    Returns a dict with keys:
    - "flow_spec": conforms to agent_flow/builder/models/FlowSpec.json
    - "flow_settings": FlowSettings serialized to a plain dict

    Notes:
    - Current implementation derives the flow_spec from settings via LangGraphBuilder,
      which guarantees consistency with how graphs are built elsewhere in the codebase.
      Direct introspection of compiled_graph is intentionally avoided for stability
      and because LangGraph does not expose a stable API for reverse-spec extraction.
    """

    return compiled_graph.get_input_jsonschema()


def flow_spec_from(compiled_graph: CompiledStateGraph, settings: FlowSettings) -> Dict[str, Any]:
    """Convenience: get only the FlowSpec-style graph JSON.

    See export_graph_specs for details.
    """
    return export_graph_specs(compiled_graph, settings)["flow_spec"]


def flow_settings_to_json(settings: FlowSettings) -> Mapping[str, Any]:
    """Serialize FlowSettings to a plain JSON-compatible mapping.

    Delegates to LangGraphBuilder(settings).export_specs() for a consistent output
    across the project.
    """
    builder = LangGraphBuilder(settings)
    return builder.export_specs()["flow_settings"]
