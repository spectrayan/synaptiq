from __future__ import annotations

from enum import Enum
from typing import Any, Dict, List

from pydantic import BaseModel, Field, field_validator


class Provider(str, Enum):
    vertexai = "vertexai"
    openai = "openai"
    anthropic = "anthropic"


class LLMSettings(BaseModel):
    provider: Provider = Field(..., description="LLM provider identifier")
    model: str = Field(..., description="Model name or id")
    temperature: float = 0.0
    max_tokens: int | None = None
    streaming: bool = True
    # Extra provider-specific kwargs (api_key, location, endpoint, etc.)
    params: Dict[str, Any] = Field(default_factory=dict)


class ToolType(str, Enum):
    python = "python"  # load by import path
    mcp = "mcp"  # Model Context Protocol bridged tool
    langchain = "langchain"  # load a registered LC tool by name


class ToolSettings(BaseModel):
    id: str
    type: ToolType
    name: str | None = None
    # For python tools, e.g. "my_pkg.tools.weather:search"
    import_path: str | None = None
    # For MCP tools, server id and tool name
    mcp_server: str | None = None
    mcp_tool: str | None = None
    # Free-form params passed to factories
    params: Dict[str, Any] = Field(default_factory=dict)


class AgentSettings(BaseModel):
    id: str
    name: str | None = None
    system_prompt: str | None = None
    instructions: str | None = None
    llm: LLMSettings
    tools: List[ToolSettings] = Field(default_factory=list)


class EdgeCondition(str, Enum):
    always = "always"
    on_tool_calls = "on_tool_calls"  # if last model message requests tool(s)
    never = "never"


class EdgeSpec(BaseModel):
    source: str
    target: str
    condition: EdgeCondition = EdgeCondition.always


class MCPTransport(str, Enum):
    stdio = "stdio"
    http = "http"
    ws = "ws"


class MCPServerConfig(BaseModel):
    id: str
    transport: MCPTransport = MCPTransport.stdio
    command: str | None = None
    args: List[str] = Field(default_factory=list)
    env: Dict[str, str] = Field(default_factory=dict)
    url: str | None = None


class ResourceLimits(BaseModel):
    # Overall
    total_timeout_ms: int | None = None
    # Per-step timeout
    step_timeout_ms: int | None = None
    max_tokens: int | None = None
    rate_limit_rps: float | None = None


class ExecutionPolicy(BaseModel):
    deterministic: bool = False
    seed: int | None = None
    resources: ResourceLimits = Field(default_factory=ResourceLimits)


class FlowType(str, Enum):
    static = "static"
    dynamic = "dynamic"
    hybrid = "hybrid"


class FlowSettings(BaseModel):
    """Workflow settings/configuration used to instantiate agents/tools/MCPs.

    Note: Historically this lived in `spec.py` as `FlowSpec`. It has been renamed
    to clarify that it represents runtime configuration, not the graph shape.
    """

    id: str
    name: str | None = None
    entrypoint: str
    agents: List[AgentSettings]
    edges: List[EdgeSpec] = Field(default_factory=list)
    mcp_servers: List[MCPServerConfig] = Field(default_factory=list)
    policy: ExecutionPolicy = Field(default_factory=ExecutionPolicy)
    flowType: FlowType = FlowType.static

    @field_validator("agents")
    @classmethod
    def ids_unique(cls, v: List[AgentSettings]) -> List[AgentSettings]:
        ids = [a.id for a in v]
        if len(ids) != len(set(ids)):
            raise ValueError("Agent ids must be unique")
        return v

    def agent_index(self) -> Dict[str, AgentSettings]:
        return {a.id: a for a in self.agents}


# Backward compatibility: re-export legacy names so existing imports continue to work
# without immediate refactors. This allows a staged migration.
FlowSpec = FlowSettings
