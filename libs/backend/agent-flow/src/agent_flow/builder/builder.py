from __future__ import annotations

import json
from pathlib import Path
from typing import Any, Mapping, Tuple

from jsonschema import Draft7Validator

from agent_flow.builder.adapters.langgraph_builder import LangGraphBuilder
from agent_flow.builder.models.settings import FlowSettings, FlowType
from agent_flow.coordinator.coordinator import CoordinatorRunner

_SCHEMA_PATH = Path(__file__).parent / "models" / "FlowSpec.json"


def _load_graph_schema() -> dict:
    with open(_SCHEMA_PATH, "r", encoding="utf-8") as f:
        return json.load(f)


def validate_graph_spec(graph_spec: Mapping[str, Any]) -> None:
    schema = _load_graph_schema()
    validator = Draft7Validator(schema)
    errors = sorted(validator.iter_errors(graph_spec), key=lambda e: e.path)
    if errors:
        msg = "; ".join([f"{list(e.path)}: {e.message}" for e in errors])
        raise ValueError(f"Graph spec does not conform to FlowSpec.json schema: {msg}")


def parse_spec_json(spec_json: str | Mapping[str, Any]) -> FlowSettings:
    """Parse FlowSettings from JSON string or mapping using Pydantic."""
    if isinstance(spec_json, str):
        spec = FlowSettings.model_validate_json(spec_json)
    else:
        spec = FlowSettings.model_validate(spec_json)
    return spec


def build_workflow(spec_json: str | Mapping[str, Any], settings_json: str | Mapping[str, Any] | None = None) -> Tuple[Any, FlowSettings, Any]:
    """Build the workflow graph and return (graph_or_runner, settings, ctx).

    Only supports building from a FlowSpec-style graph spec and separate settings.
    - graph spec must have 'nodes' and 'entry_point' and is validated against models/FlowSpec.json.
    - settings must be provided either via `settings_json` or under the key 'settings' in the graph spec mapping.
    """
    # Normalize graph spec to a mapping
    graph_spec: Mapping[str, Any]
    if isinstance(spec_json, str):
        graph_spec = json.loads(spec_json)
    else:
        graph_spec = spec_json

    if not isinstance(graph_spec, Mapping) or not ("nodes" in graph_spec and "entry_point" in graph_spec):
        raise ValueError("Expected a FlowSpec-like graph spec with 'nodes' and 'entry_point'. Legacy settings-only JSON is no longer supported.")

    # Allow embedded settings in the same JSON under 'settings'
    embedded_settings = graph_spec.get("settings") if isinstance(graph_spec, Mapping) else None
    settings_source = settings_json or embedded_settings
    if settings_source is None:
        raise ValueError("No settings provided. Pass settings_json or include a 'settings' object in the spec.")

    # Validate graph spec and parse settings
    validate_graph_spec(graph_spec)
    settings = parse_spec_json(settings_source)

    builder = LangGraphBuilder(settings)
    graph, ctx = builder.build(graph_spec, settings)

    # Handle dynamic/hybrid via settings.flowType if needed
    if getattr(settings, "flowType", FlowType.static) in (FlowType.dynamic, FlowType.hybrid):
        runner = CoordinatorRunner(base_spec=settings)
        ctx = {"mode": "coordinator"}
        return runner, settings, ctx

    return graph, settings, ctx
