from __future__ import annotations

import importlib
from dataclasses import dataclass
from typing import Any, Callable, Iterable, List

from langchain_core.tools import BaseTool, tool

from ..models.settings import ToolSettings, ToolType


@dataclass
class ToolBuildContext:
    deterministic: bool = False
    seed: int | None = None


class ToolFactory:
    """Factory to construct tools from ToolSettings.

    Supports:
    - python: load function from import path and wrap as LC tool
    - langchain: load LC tool via import path in "import_path"
    - mcp: delegated to optional MCP adapter (stubbed if unavailable)
    """

    @staticmethod
    def create_many(settings: Iterable[ToolSettings], ctx: ToolBuildContext | None = None) -> List[BaseTool]:
        ctx = ctx or ToolBuildContext()
        tools: List[BaseTool] = []
        for spec in settings:
            tools.append(ToolFactory.create(spec, ctx))
        return tools

    @staticmethod
    def create(spec: ToolSettings, ctx: ToolBuildContext) -> BaseTool:
        if spec.type == ToolType.python:
            if not spec.import_path:
                raise ValueError(f"python tool '{spec.id}' requires import_path")
            fn = _load_object(spec.import_path)
            if isinstance(fn, BaseTool):
                return fn
            return _as_langchain_tool(fn, name=spec.name or spec.id, **(spec.params or {}))

        if spec.type == ToolType.langchain:
            if not spec.import_path:
                raise ValueError(f"langchain tool '{spec.id}' requires import_path")
            obj = _load_object(spec.import_path)
            if isinstance(obj, BaseTool):
                return obj
            # Some LC tools expose .from_... factory methods; allow call if callable
            if callable(obj):
                res = obj(**(spec.params or {}))
                if isinstance(res, BaseTool):
                    return res
            raise TypeError(
                f"Import at {spec.import_path} did not yield a BaseTool; got {type(obj)}"
            )

        if spec.type == ToolType.mcp:
            try:
                from ..mcp.mcp_adapter import MCPToolAdapter
            except Exception as e:  # pragma: no cover - optional dependency
                raise RuntimeError("MCP support not available: " + str(e))
            return MCPToolAdapter.create_tool(
                server_id=spec.mcp_server,
                tool_name=spec.mcp_tool,
                name=spec.name or spec.id,
                params=spec.params or {},
                deterministic=ctx.deterministic,
            )

        raise ValueError(f"Unsupported tool type: {spec.type}")


def _load_object(import_path: str) -> Any:
    """Load an object from a string import path like 'pkg.mod:attr'."""
    if ":" not in import_path:
        raise ValueError("import_path must be 'module.sub:attr'")
    module_name, attr = import_path.split(":", 1)
    module = importlib.import_module(module_name)
    obj = getattr(module, attr)
    return obj


def _as_langchain_tool(fn: Callable[..., Any], name: str, **kwargs: Any) -> BaseTool:
    # If the function already has a LC tool wrapper, return it
    if hasattr(fn, "lc_namespace") or hasattr(fn, "bind_tools"):
        # heuristic; fall back to wrapping otherwise
        pass

    # Use the tool decorator without the name parameter
    decorated = tool(fn)
    # Set the name after decoration
    decorated.name = name
    
    if kwargs:
        # Some tool frameworks accept config through attributes
        try:
            for k, v in kwargs.items():
                setattr(decorated, k, v)
        except Exception:
            # Ignore config assignment if unsupported
            pass
    return decorated
