from __future__ import annotations

from abc import ABC, abstractmethod
from typing import Any, List, Optional

from agent_flow.builder.models.settings import AgentSettings, EdgeCondition, EdgeSpec, FlowSettings, FlowType


class FlowGenerator(ABC):
    """Abstract base class for generating or updating FlowSettings.

    Single responsibility: produce FlowSettings from user input and context.
    """

    @abstractmethod
    def generate(self, *, user_input: dict[str, Any], base_spec: Optional[FlowSettings] = None, transcript: Optional[List[Any]] = None) -> FlowSettings:
        raise NotImplementedError

    def update(self, *, current_spec: FlowSettings, transcript: List[Any]) -> FlowSettings | None:
        """Optionally update FlowSettings based on the evolving transcript.
        Returns a new spec if an update is needed, otherwise None.
        Default implementation: no updates.
        """
        return None


class StaticFlowGenerator(FlowGenerator):
    def generate(self, *, user_input: dict[str, Any], base_spec: Optional[FlowSettings] = None, transcript: Optional[List[Any]] = None) -> FlowSettings:
        if base_spec is None:
            raise ValueError("StaticFlowGenerator requires a base_spec")
        return base_spec


class DynamicFlowGenerator(FlowGenerator):
    """Dynamic generator that uses an LLM-driven generator workflow to produce Flow Settings.

    It builds a static "generator" workflow (provided via base_spec or bundled default JSON)
    and invokes it with the user's input. The generator agent must output a valid Flow Settings JSON,
    which we parse and return. The returned spec defaults to flowType=static to avoid nested
    coordinators.
    """

    def _to_initial_state(self, input: Any) -> dict[str, Any]:
        # Mirror the executor's convenience conversion
        try:
            from langchain_core.messages import HumanMessage
        except Exception:
            HumanMessage = None  # type: ignore
        if input is None:
            return {"messages": []}
        if isinstance(input, str):
            return {"messages": [HumanMessage(content=input)]} if HumanMessage else {"messages": [input]}
        if isinstance(input, dict):
            if "messages" in input:
                return dict(input)
            return {"messages": [HumanMessage(content=str(input))]} if HumanMessage else {"messages": [str(input)]}
        try:
            from collections.abc import Iterable as _Iterable
            if isinstance(input, _Iterable):
                return {"messages": list(input)}
        except Exception:
            pass
        return {"messages": [HumanMessage(content=str(input))]} if HumanMessage else {"messages": [str(input)]}

    def _extract_messages(self, event_or_state: Any) -> List[Any]:
        try:
            if isinstance(event_or_state, dict) and "messages" in event_or_state:
                msgs = event_or_state.get("messages", [])
                if isinstance(msgs, list):
                    return msgs
                return [msgs]
        except Exception:
            pass
        return []

    def _load_default_generator_spec_json(self) -> str:
        import os
        path = os.path.join(os.path.dirname(__file__), "generator_settings.json")
        with open(path, "r", encoding="utf-8") as f:
            return f.read()

    def generate(self, *, user_input: dict[str, Any], base_spec: Optional[FlowSettings] = None, transcript: Optional[List[Any]] = None) -> FlowSettings:
        from agent_flow.builder.builder import build_workflow, parse_spec_json
        import json

        # Determine the generator spec (static)
        if base_spec is None:
            # Fall back to bundled default JSON
            gen_spec_json = self._load_default_generator_spec_json()
            generator_spec = parse_spec_json(gen_spec_json)
        else:
            # Treat provided base_spec as the generator spec
            generator_spec = base_spec

        # Build generator workflow (should be static)
        graph, _spec, _ctx = build_workflow(generator_spec.model_dump())

        # Prepare input state and run
        input_state = self._to_initial_state(user_input.get("prompt") if isinstance(user_input, dict) else user_input)
        out = graph.invoke(input_state)
        msgs = self._extract_messages(out)
        if not msgs:
            raise ValueError("Generator workflow produced no messages; cannot derive Flow Settings")
        # Assume the last message content is the Flow Settings JSON
        last = msgs[-1]
        content = getattr(last, "content", last)
        if isinstance(content, list):
            # Some models return content as list of parts; join text parts
            text_parts = [p.get("text", "") for p in content if isinstance(p, dict) and "text" in p]
            content = "\n".join([t for t in text_parts if t])
        if not isinstance(content, str):
            content = str(content)

        # Strip common code fences if present
        content_str = content.strip()
        if content_str.startswith("```"):
            # remove first line and trailing ```
            lines = content_str.splitlines()
            if len(lines) >= 2:
                # drop the first fence line and possible last fence
                if lines[-1].strip().startswith("```"):
                    lines = lines[1:-1]
                else:
                    lines = lines[1:]
                content_str = "\n".join(lines)

        # Parse JSON into Flow Settings
        try:
            # Try json first to validate/normalize; then use pydantic for full validation
            _ = json.loads(content_str)
        except json.JSONDecodeError as e:
            raise ValueError(f"Generator did not return valid JSON Flow Settings: {e}\nContent:\n{content_str[:1000]}")

        spec = parse_spec_json(content_str)
        # Ensure flowType is static by default (avoid nested coordinators)
        if getattr(spec, "flowType", None) is None:
            from agent_flow.builder.models.settings import FlowType as _FlowType
            spec.flowType = _FlowType.static
        return spec

    def update(self, *, current_spec: FlowSettings, transcript: List[Any]) -> FlowSettings | None:
        # Runtime updates are out of scope for the generator itself; coordinator may implement policies
        return None


class HybridFlowGenerator(DynamicFlowGenerator):
    """Start from a static spec, but allow dynamic adjustments.

    Current minimal implementation: same as dynamic for initial generation; no runtime updates.
    """

    def generate(self, *, user_input: dict[str, Any], base_spec: Optional[FlowSettings] = None, transcript: Optional[List[Any]] = None) -> FlowSettings:
        if base_spec is None:
            raise ValueError("HybridFlowGenerator requires a base_spec")
        # Start with base spec unchanged (hybrid approach)
        spec = FlowSettings(
            id=base_spec.id,
            name=base_spec.name or base_spec.id,
            entrypoint=base_spec.entrypoint,
            agents=list(base_spec.agents),
            edges=list(base_spec.edges),
            mcp_servers=list(base_spec.mcp_servers),
            policy=base_spec.policy,
            flowType=FlowType.hybrid,
        )
        return spec


def get_generator(flow_type: FlowType) -> type[FlowGenerator]:
    if flow_type == FlowType.static:
        return StaticFlowGenerator
    if flow_type == FlowType.dynamic:
        return DynamicFlowGenerator
    if flow_type == FlowType.hybrid:
        return HybridFlowGenerator
    # Fallback to static
    return StaticFlowGenerator
