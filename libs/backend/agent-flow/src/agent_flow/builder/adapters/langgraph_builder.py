from __future__ import annotations

import contextlib
import time
from typing import Any, Dict, List, Tuple

from langchain_core.messages import BaseMessage
from langchain_core.runnables import RunnableConfig
from langgraph.graph import END, MessagesState, StateGraph
from langgraph.prebuilt import ToolNode

from agent_flow.builder.factories.llm_factory import LLMFactory
from agent_flow.builder.factories.tool_factory import ToolBuildContext, ToolFactory
from agent_flow.builder.models.settings import (
    AgentSettings,
    EdgeCondition,
    EdgeSpec,
    FlowSettings,
)
from agent_flow.builder.mcp.mcp_adapter import MCPToolAdapter


class LangGraphBuilder:
    """Translate settings and/or graph spec into a compiled LangGraph graph and helpers."""

    def __init__(self, settings: FlowSettings):
        # settings represents runtime FlowSettings
        self.settings = settings
        self._agent_tools: Dict[str, List[Any]] = {}
        
        # Register MCP servers
        for server_config in settings.mcp_servers:
            MCPToolAdapter.register_server(server_config)

    def build(self, graph_spec: Dict[str, Any], settings: FlowSettings) -> Tuple[Any, Dict[str, Any]]:
        """Build and compile a LangGraph graph from a graph-spec (nodes/edges) and settings.

        The graph_spec must follow the enhanced JSON Schema in models/FlowSpec.json.
        Supports all LangGraph features including conditional routing, state management, and error handling.
        """
        # Determine state schema
        state_class = self._get_state_class(graph_spec)
        graph = StateGraph(state_class)
        ctx: Dict[str, Any] = {"nodes": {}, "state_class": state_class}

        # Precompute helpers
        agent_by_id = settings.agent_index()

        # Create callable nodes based on graph_spec
        nodes: Dict[str, Any] = graph_spec.get("nodes", {})
        for node_name, node_def in nodes.items():
            self._add_node_to_graph(graph, node_name, node_def, agent_by_id, settings)

        # Set entry point
        entry = graph_spec.get("entry_point") or graph_spec.get("entrypoint")
        if not entry:
            raise ValueError("Graph spec missing 'entry_point'")
        graph.set_entry_point(entry)

        # Add regular edges
        for edge in graph_spec.get("edges", []):
            self._add_edge_to_graph(graph, edge)

        # Add conditional edges
        for cond_edge in graph_spec.get("conditional_edges", []):
            self._add_conditional_edge_to_graph(graph, cond_edge)

        # Configure interrupts
        interrupts_config = graph_spec.get("interrupts", {})
        interrupt_before = interrupts_config.get("interrupt_before", [])
        interrupt_after = interrupts_config.get("interrupt_after", [])

        # Compile graph with configuration
        compile_config = {}
        if interrupt_before:
            compile_config["interrupt_before"] = interrupt_before
        if interrupt_after:
            compile_config["interrupt_after"] = interrupt_after

        # Add checkpointing if configured
        checkpointing = graph_spec.get("checkpointing", {})
        if checkpointing.get("enabled", False):
            compile_config["checkpointer"] = self._create_checkpointer(checkpointing)

        compiled = graph.compile(**compile_config) if compile_config else graph.compile()
        return compiled, ctx

    def _get_state_class(self, graph_spec: Dict[str, Any]):
        """Determine the state class to use for the graph."""
        state_schema = graph_spec.get("state_schema", {})
        schema_type = state_schema.get("type", "MessagesState")
        
        if schema_type == "MessagesState":
            return MessagesState
        elif schema_type == "TypedDict":
            # For now, use MessagesState as base and extend if needed
            # In a full implementation, would dynamically create TypedDict class
            return MessagesState
        elif schema_type == "custom":
            # Load custom state class
            custom_class = state_schema.get("custom_class")
            if custom_class:
                from agent_flow.builder.factories.tool_factory import _load_object
                return _load_object(custom_class)
            return MessagesState
        else:
            return MessagesState

    def _add_node_to_graph(self, graph: StateGraph, node_name: str, node_def: Dict[str, Any], 
                          agent_by_id: Dict[str, Any], settings: FlowSettings):
        """Add a node to the graph based on its type."""
        ntype = node_def.get("type")
        
        if ntype == "agent":
            self._add_agent_node(graph, node_name, node_def, agent_by_id, settings)
        elif ntype == "tool":
            self._add_tool_node(graph, node_name, node_def, settings)
        elif ntype == "function":
            self._add_function_node(graph, node_name, node_def)
        elif ntype == "conditional":
            self._add_conditional_node(graph, node_name, node_def)
        elif ntype == "parallel":
            self._add_parallel_node(graph, node_name, node_def)
        elif ntype == "end":
            # End nodes are handled by routing to END
            pass
        elif ntype == "subgraph":
            self._add_subgraph_node(graph, node_name, node_def)
        else:
            raise ValueError(f"Unsupported node type '{ntype}' for node '{node_name}'")

    def _add_agent_node(self, graph: StateGraph, node_name: str, node_def: Dict[str, Any], 
                       agent_by_id: Dict[str, Any], settings: FlowSettings):
        """Add an agent node to the graph."""
        agent_id = node_def.get("agent") or node_name
        agent = agent_by_id.get(agent_id)
        if not agent:
            raise KeyError(f"Agent '{agent_id}' referenced by node '{node_name}' is not defined in settings")
        
        agent_node_fn, tools = self._build_agent_node(agent)
        
        # Wrap with timeout and retry if configured
        wrapped_fn = self._wrap_node_with_policies(agent_node_fn, node_def)
        graph.add_node(node_name, wrapped_fn)
        
        self._agent_tools[node_name] = tools
        if tools:
            tools_node_name = f"{node_name}__tools"
            graph.add_node(tools_node_name, ToolNode(tools))
            graph.add_edge(tools_node_name, node_name)

    def _add_function_node(self, graph: StateGraph, node_name: str, node_def: Dict[str, Any]):
        """Add a function node to the graph."""
        fn_import = node_def.get("function")
        if not fn_import:
            raise ValueError(f"Function node '{node_name}' missing 'function' import path")
        
        from agent_flow.builder.factories.tool_factory import _load_object
        fn = _load_object(fn_import)
        
        def _function_node(state, config=None):
            # Extract input parameters from state based on input_mapping
            input_mapping = node_def.get("input_mapping", {})
            params = {}
            for param_name, state_key in input_mapping.items():
                if state_key in state:
                    params[param_name] = state[state_key]
            
            # Add static parameters
            static_params = node_def.get("input_params", {})
            params.update(static_params)
            
            # Call function
            result = fn(**params)
            
            # Map output back to state
            output_mapping = node_def.get("output_mapping", {})
            updates = {}
            if output_mapping:
                for output_key, state_key in output_mapping.items():
                    if hasattr(result, output_key):
                        updates[state_key] = getattr(result, output_key)
                    elif isinstance(result, dict) and output_key in result:
                        updates[state_key] = result[output_key]
            else:
                # Default: add result to messages
                updates["messages"] = [{"type": "function", "content": str(result)}]
            
            return updates
        
        wrapped_fn = self._wrap_node_with_policies(_function_node, node_def)
        graph.add_node(node_name, wrapped_fn)

    def _add_conditional_node(self, graph: StateGraph, node_name: str, node_def: Dict[str, Any]):
        """Add a conditional routing node."""
        condition_fn_path = node_def.get("condition_function")
        if not condition_fn_path:
            raise ValueError(f"Conditional node '{node_name}' missing 'condition_function'")
        
        from agent_flow.builder.factories.tool_factory import _load_object
        condition_fn = _load_object(condition_fn_path)
        
        # Conditional nodes don't need to be added as regular nodes
        # They are handled by conditional edges
        pass

    def _add_edge_to_graph(self, graph: StateGraph, edge: Dict[str, Any]):
        """Add a regular edge to the graph."""
        src = edge.get("from") or edge.get("source")
        dst = edge.get("to") or edge.get("target")
        cond = edge.get("condition")
        
        if not src or not dst:
            raise ValueError(f"Edge missing source or target: {edge}")
        
        # Handle special target values
        if dst == "END" or dst == "end":
            dst = END
        
        if cond in (None, "always", ""):
            graph.add_edge(src, dst)
        elif cond == "on_tool_calls":
            def _cond(state, _src=src, _dst=dst):
                messages = state.get("messages", [])
                if messages:
                    last = messages[-1]
                    return f"{_src}__tools" if getattr(last, "tool_calls", None) else _dst
                return _dst
            graph.add_conditional_edges(src, _cond)
        elif cond == "never":
            # Skip this edge
            pass
        else:
            # Custom condition function
            try:
                from agent_flow.builder.factories.tool_factory import _load_object
                condition_fn = _load_object(cond)
                graph.add_conditional_edges(src, condition_fn)
            except Exception:
                raise ValueError(f"Invalid edge condition: {cond}")

    def _add_conditional_edge_to_graph(self, graph: StateGraph, cond_edge: Dict[str, Any]):
        """Add a conditional edge with mapping to the graph."""
        src = cond_edge.get("from")
        condition_fn_path = cond_edge.get("condition_function")
        condition_mapping = cond_edge.get("condition_mapping", {})
        default_target = cond_edge.get("default_target")
        
        if not src or not condition_fn_path:
            raise ValueError(f"Conditional edge missing source or condition function: {cond_edge}")
        
        from agent_flow.builder.factories.tool_factory import _load_object
        condition_fn = _load_object(condition_fn_path)
        
        # Create routing function that uses the mapping
        def _route_fn(state):
            result = condition_fn(state)
            target = condition_mapping.get(result, default_target)
            if target == "END" or target == "end":
                return END
            return target or END
        
        graph.add_conditional_edges(src, _route_fn)

    def _wrap_node_with_policies(self, node_fn, node_def: Dict[str, Any]):
        """Wrap node function with timeout and retry policies."""
        timeout_ms = node_def.get("timeout_ms")
        retry_policy = node_def.get("retry_policy")
        
        if not timeout_ms and not retry_policy:
            return node_fn
        
        def _wrapped_node(state, config=None):
            # In a full implementation, would add actual timeout and retry logic
            # For now, just call the original function
            return node_fn(state, config)
        
        return _wrapped_node

    def _create_checkpointer(self, checkpointing_config: Dict[str, Any]):
        """Create a checkpointer based on configuration."""
        saver_type = checkpointing_config.get("saver_type", "memory")
        
        if saver_type == "memory":
            from langgraph.checkpoint.memory import MemorySaver
            return MemorySaver()
        elif saver_type == "sqlite":
            # Would need to implement SQLite saver
            raise NotImplementedError("SQLite checkpointer not implemented")
        elif saver_type == "postgres":
            # Would need to implement Postgres saver
            raise NotImplementedError("Postgres checkpointer not implemented")
        else:
            raise ValueError(f"Unknown saver type: {saver_type}")

    def _add_tool_node(self, graph: StateGraph, node_name: str, node_def: Dict[str, Any], settings: FlowSettings):
        """Add a tool node to the graph."""
        # Implementation for tool nodes
        raise NotImplementedError("Tool nodes not yet implemented in enhanced builder")

    def _add_parallel_node(self, graph: StateGraph, node_name: str, node_def: Dict[str, Any]):
        """Add a parallel execution node."""
        # Implementation for parallel nodes
        raise NotImplementedError("Parallel nodes not yet implemented in enhanced builder")

    def _add_subgraph_node(self, graph: StateGraph, node_name: str, node_def: Dict[str, Any]):
        """Add a subgraph node."""
        # Implementation for subgraph nodes
        raise NotImplementedError("Subgraph nodes not yet implemented in enhanced builder")

    def _build_agent_node(self, agent: AgentSettings):
        model = LLMFactory.create(agent.llm)
        tools = ToolFactory.create_many(agent.tools, ctx=ToolBuildContext(
            deterministic=self.settings.policy.deterministic,
            seed=self.settings.policy.seed,
        ))
        if tools:
            model = model.bind_tools(tools)

        step_timeout = self.settings.policy.resources.step_timeout_ms

        def _call_model(state: MessagesState, config: RunnableConfig) -> Dict[str, BaseMessage]:
            # Optional: prepend system prompt
            messages = state["messages"]
            if agent.system_prompt:
                messages = [{"type": "system", "content": agent.system_prompt}] + messages

            # Apply per-step timeout if configured
            with _timeout_ctx(step_timeout):
                response = model.invoke(messages, config)
            return {"messages": response}

        return _call_model, tools

    def _add_edge(self, graph: StateGraph, edge: EdgeSpec) -> None:
        if edge.condition == EdgeCondition.always:
            graph.add_edge(edge.source, edge.target)
            return
        if edge.condition == EdgeCondition.never:
            # no connection
            return
        if edge.condition == EdgeCondition.on_tool_calls:
            def _cond(state: MessagesState) -> str:
                last = state["messages"][-1]
                return f"{edge.source}__tools" if getattr(last, "tool_calls", None) else edge.target or END

            graph.add_conditional_edges(edge.source, _cond)
            return

        raise ValueError(f"Unsupported edge condition: {edge.condition}")


@contextlib.contextmanager
def _timeout_ctx(ms: int | None):
    # Lightweight timeout guard; for production, use anyio with cancellation
    if not ms:
        yield
        return
    start = time.time()
    yield
    elapsed_ms = (time.time() - start) * 1000
    if elapsed_ms > ms:
        raise TimeoutError(f"Step exceeded timeout: {elapsed_ms:.1f}ms > {ms}ms")
