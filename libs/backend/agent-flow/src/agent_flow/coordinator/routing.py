"""Routing functions for coordinator workflow."""

from typing import Any, Dict, List
import re


def route_coordinator_decision(state: Dict[str, Any]) -> str:
    """Route based on coordinator agent's decision.
    
    Args:
        state: Current workflow state
        
    Returns:
        Target node name based on coordinator decision
    """
    messages = state.get("messages", [])
    if not messages:
        return "terminate"
    
    # Get the last message from coordinator
    last_message = messages[-1]
    content = getattr(last_message, "content", str(last_message))
    
    if isinstance(content, str):
        content_lower = content.lower()
        
        # Look for explicit decision format
        decision_match = re.search(r"decision:\s*(\w+)", content_lower)
        if decision_match:
            decision = decision_match.group(1)
            if decision in ["execute", "terminate", "continue"]:
                return decision
        
        # Fallback to keyword detection
        if any(word in content_lower for word in ["execute", "run", "start"]):
            return "execute"
        elif any(word in content_lower for word in ["terminate", "stop", "done", "complete", "finish"]):
            return "terminate"
        elif any(word in content_lower for word in ["continue", "next", "proceed"]):
            return "continue"
    
    # Default to terminate if unclear
    return "terminate"


def check_iteration_limit(state: Dict[str, Any]) -> str:
    """Check if iteration limit has been reached.
    
    Args:
        state: Current workflow state
        
    Returns:
        'continue' if under limit, 'terminate' if limit reached
    """
    iteration_count = state.get("iteration_count", 0)
    max_iterations = state.get("max_iterations", 5)
    
    if iteration_count >= max_iterations:
        return "terminate"
    
    # Check if coordinator explicitly requested termination
    coordinator_decision = state.get("coordinator_decision")
    if coordinator_decision == "terminate":
        return "terminate"
    
    return "continue"


def should_regenerate_workflow(state: Dict[str, Any]) -> bool:
    """Determine if workflow should be regenerated based on execution results.
    
    Args:
        state: Current workflow state
        
    Returns:
        True if workflow should be regenerated
    """
    execution_history = state.get("execution_history", [])
    
    if not execution_history:
        return True  # No workflow exists, need to generate
    
    # Check recent execution results
    recent_executions = execution_history[-3:] if len(execution_history) > 3 else execution_history
    
    # Count failures in recent executions
    failure_count = sum(1 for exec_result in recent_executions 
                       if exec_result.get("type") == "error")
    
    # Regenerate if more than half of recent executions failed
    if len(recent_executions) > 0 and failure_count / len(recent_executions) > 0.5:
        return True
    
    # Check if coordinator explicitly requested regeneration
    messages = state.get("messages", [])
    if messages:
        last_message = messages[-1]
        content = getattr(last_message, "content", str(last_message))
        if isinstance(content, str):
            content_lower = content.lower()
            if any(word in content_lower for word in ["regenerate", "generate", "create", "new"]):
                return True
    
    return False


def extract_workflow_specification(state: Dict[str, Any]) -> Dict[str, Any] | None:
    """Extract workflow specification from coordinator messages.
    
    Args:
        state: Current workflow state
        
    Returns:
        Workflow specification dict or None if not found
    """
    messages = state.get("messages", [])
    
    # Look through recent messages for workflow specifications
    for message in reversed(messages[-5:]):  # Check last 5 messages
        content = getattr(message, "content", str(message))
        if isinstance(content, str):
            # Look for JSON-like content
            if "{" in content and "}" in content:
                try:
                    import json
                    # Try to extract JSON from the content
                    start = content.find("{")
                    end = content.rfind("}") + 1
                    json_part = content[start:end]
                    
                    spec = json.loads(json_part)
                    
                    # Validate it looks like a workflow spec
                    if isinstance(spec, dict) and "id" in spec and "agents" in spec:
                        return spec
                        
                except (json.JSONDecodeError, ValueError):
                    continue
    
    return None


def update_workflow_context(state: Dict[str, Any], workflow_spec: Dict[str, Any]) -> Dict[str, Any]:
    """Update workflow context with new specification.
    
    Args:
        state: Current workflow state
        workflow_spec: New workflow specification
        
    Returns:
        Updated state
    """
    updated_state = dict(state)
    
    # Update workflow context
    workflow_context = updated_state.get("workflow_context", {})
    workflow_context.update({
        "current_spec": workflow_spec,
        "spec_updated_at": "now",  # In real implementation, use proper timestamp
        "spec_version": workflow_context.get("spec_version", 0) + 1
    })
    
    updated_state["workflow_context"] = workflow_context
    updated_state["current_workflow_spec"] = workflow_spec
    
    return updated_state