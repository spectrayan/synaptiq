"""Execution functions for coordinator workflow."""

from typing import Any, Dict, List
import json
import time


def execute_workflow(state: Dict[str, Any]) -> Dict[str, Any]:
    """Execute a user workflow based on current state.
    
    Args:
        state: Current workflow state containing workflow specification
        
    Returns:
        Updated state with execution results
    """
    try:
        # Get workflow specification
        workflow_spec = state.get("current_workflow_spec")
        if not workflow_spec:
            return _add_execution_error(state, "No workflow specification available")
        
        # Get input messages for workflow execution
        messages = state.get("messages", [])
        user_messages = [msg for msg in messages if _is_user_message(msg)]
        
        if not user_messages:
            return _add_execution_error(state, "No user input available for workflow execution")
        
        # Build and execute the workflow
        from agent_flow.builder.builder import build_workflow
        
        # Create proper FlowSpec format
        flow_spec = _convert_to_flow_spec(workflow_spec)
        
        # Build the workflow
        graph, settings, ctx = build_workflow(flow_spec)
        
        # Prepare input for execution
        workflow_input = {"messages": user_messages}
        
        # Execute workflow
        start_time = time.time()
        
        if hasattr(graph, "invoke"):
            result = graph.invoke(workflow_input)
            execution_time = (time.time() - start_time) * 1000
            
            # Add execution result to state
            execution_result = {
                "type": "result",
                "result": _safe_serialize(result),
                "execution_time_ms": round(execution_time, 2),
                "workflow_id": workflow_spec.get("id", "unknown"),
                "timestamp": time.time(),
                "iteration": state.get("iteration_count", 0)
            }
            
            return _add_execution_result(state, execution_result)
        else:
            return _add_execution_error(state, "Workflow graph does not support execution")
            
    except Exception as e:
        return _add_execution_error(state, f"Workflow execution failed: {e}")


def _convert_to_flow_spec(workflow_spec: Dict[str, Any]) -> Dict[str, Any]:
    """Convert workflow specification to FlowSpec format.
    
    Args:
        workflow_spec: Workflow specification
        
    Returns:
        FlowSpec format dictionary
    """
    # Create nodes from agents
    nodes = {}
    agents = workflow_spec.get("agents", [])
    
    for agent in agents:
        agent_id = agent["id"]
        nodes[agent_id] = {
            "type": "agent",
            "agent": agent_id
        }
    
    # Create FlowSpec structure
    flow_spec = {
        "entry_point": workflow_spec.get("entrypoint", agents[0]["id"] if agents else "unknown"),
        "nodes": nodes,
        "edges": workflow_spec.get("edges", []),
        "settings": workflow_spec
    }
    
    return flow_spec


def _is_user_message(message: Any) -> bool:
    """Check if message is from user.
    
    Args:
        message: Message to check
        
    Returns:
        True if message is from user
    """
    if hasattr(message, "type"):
        return message.type == "human"
    elif isinstance(message, dict):
        return message.get("type") == "human"
    return False


def _add_execution_result(state: Dict[str, Any], execution_result: Dict[str, Any]) -> Dict[str, Any]:
    """Add execution result to state.
    
    Args:
        state: Current state
        execution_result: Execution result to add
        
    Returns:
        Updated state
    """
    updated_state = dict(state)
    
    # Add to execution history
    execution_history = updated_state.get("execution_history", [])
    execution_history.append(execution_result)
    updated_state["execution_history"] = execution_history
    
    # Update iteration count
    updated_state["iteration_count"] = updated_state.get("iteration_count", 0) + 1
    
    # Extract messages from result if available
    result_data = execution_result.get("result", {})
    if isinstance(result_data, dict) and "messages" in result_data:
        result_messages = result_data["messages"]
        if isinstance(result_messages, list):
            messages = updated_state.get("messages", [])
            messages.extend(result_messages)
            updated_state["messages"] = messages
    
    return updated_state


def _add_execution_error(state: Dict[str, Any], error_message: str) -> Dict[str, Any]:
    """Add execution error to state.
    
    Args:
        state: Current state
        error_message: Error message
        
    Returns:
        Updated state with error
    """
    updated_state = dict(state)
    
    # Create error result
    error_result = {
        "type": "error",
        "error": error_message,
        "timestamp": time.time(),
        "iteration": state.get("iteration_count", 0)
    }
    
    # Add to execution history
    execution_history = updated_state.get("execution_history", [])
    execution_history.append(error_result)
    updated_state["execution_history"] = execution_history
    
    # Update iteration count
    updated_state["iteration_count"] = updated_state.get("iteration_count", 0) + 1
    
    # Set should_continue to False on error
    updated_state["should_continue"] = False
    
    return updated_state


def _safe_serialize(obj: Any) -> Any:
    """Safely serialize objects for storage.
    
    Args:
        obj: Object to serialize
        
    Returns:
        Serialized object
    """
    try:
        if hasattr(obj, "dict"):
            return obj.dict()
        elif hasattr(obj, "model_dump"):
            return obj.model_dump()
        elif isinstance(obj, dict):
            return obj
        return str(obj)
    except Exception:
        return str(obj)