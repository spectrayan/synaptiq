import logging
from typing import Literal

from langchain_core.messages import SystemMessage
from langchain_core.runnables import RunnableConfig
from langgraph.graph import StateGraph, START, END
from langgraph.prebuilt import ToolNode

from synaptiq_api.services.agent.state import AgentState
from synaptiq_api.services.agent.tools import (
    search_catalog,
    query_collection,
    update_tenant_config,
    manage_schema_field,
    create_catalog_item,
)
from synaptiq_api.services.llm_provider import get_provider

logger = logging.getLogger(__name__)

# Compile tools — includes catalog, data query, and admin mutation tools (P1-A)
tools = [search_catalog, query_collection, update_tenant_config, manage_schema_field, create_catalog_item]
tool_node = ToolNode(tools)

async def call_model(state: AgentState, config: RunnableConfig):
    """
    Call the LLM with the current state and tools.
    Extract the initialized LangChain model from the config.
    """
    model = config["configurable"].get("model")
    if not model:
        raise ValueError("Model missing from configuration")
        
    messages = state["messages"]
    logger.info("Calling model with %d messages", len(messages))
    
    # We bind the tools to the model
    model_with_tools = model.bind_tools(tools)
    
    response = await model_with_tools.ainvoke(messages, config=config)
    return {"messages": [response]}


async def should_continue(state: AgentState) -> Literal["tools", END]:
    """
    Determine whether to use tools or end the conversation.
    """
    last_message = state["messages"][-1]
    if last_message.tool_calls:
        return "tools"
    return END

# Define a new graph
workflow = StateGraph(AgentState)

# Define the two nodes we will cycle between
workflow.add_node("agent", call_model)
workflow.add_node("tools", tool_node)

# Set the entrypoint as `agent`
workflow.add_edge(START, "agent")

# We now add a conditional edge
workflow.add_conditional_edges(
    "agent",
    should_continue,
    {"tools": "tools", END: END}
)

# We now add a normal edge from `tools` to `agent`.
# This means that after `tools` is called, `agent` node is called next.
workflow.add_edge("tools", "agent")

# Finally, we compile it!
agent_graph = workflow.compile()
