from typing import TypedDict
from langchain_core.messages import BaseMessage
from langgraph.graph import add_messages
from typing import Annotated

class AgentState(TypedDict):
    """The state of the AI agent."""
    messages: Annotated[list[BaseMessage], add_messages]
    tenant_id: str
    session_id: str
