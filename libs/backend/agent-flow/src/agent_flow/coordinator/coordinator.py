"""Coordinator system using LangGraph workflow for decision making."""

from __future__ import annotations

import json
import os
from typing import Any, Dict, Generator, List, Mapping, Optional, Tuple

from langchain_core.runnables import RunnableConfig

from agent_flow.builder.adapters.langgraph_builder import LangGraphBuilder
from agent_flow.builder.builder import build_workflow
from agent_flow.builder.models.settings import FlowSettings, FlowType


class CoordinatorRunner:
    """A runnable wrapper that orchestrates dynamic/hybrid flows using a LangGraph coordinator.

    The coordinator is itself a LangGraph workflow with an agent that makes decisions about
    workflow generation, execution, and termination using available tools.

    Responsibilities:
    - Load and execute the coordinator workflow
    - Maintain execution context and transcript
    - Generate and execute user workflows based on coordinator decisions
    - Handle iteration limits and termination conditions
    """

    def __init__(self, *, base_spec: FlowSettings) -> None:
        self.base_spec = base_spec
        self._transcript: List[Any] = []
        self._execution_history: List[Dict[str, Any]] = []
        self._current_workflow_spec: Optional[FlowSettings] = None
        self._iteration_count = 0
        self._max_iterations = 5
        
        # Load coordinator workflow
        self._coordinator_graph = self._load_coordinator_workflow()

    def _load_coordinator_workflow(self):
        """Load the coordinator LangGraph workflow."""
        coordinator_spec_path = os.path.join(
            os.path.dirname(__file__), 
            "coordinator_spec.json"
        )
        
        with open(coordinator_spec_path, "r") as f:
            coordinator_spec = json.load(f)
        
        # Build coordinator workflow
        coordinator_graph, _, _ = build_workflow(coordinator_spec)
        return coordinator_graph

    def invoke(self, input_state: Mapping[str, Any], config: Optional[RunnableConfig] = None) -> Dict[str, Any]:
        """Invoke the coordinator workflow and return final result."""
        result = None
        for _ in self.stream(input_state, config=config):
            pass
        
        # Return the last message or execution result
        if self._transcript:
            result = self._transcript[-1]
        return {"messages": [result] if result is not None else []}

    def stream(self, input_state: Mapping[str, Any], config: Optional[RunnableConfig] = None) -> Generator[Tuple[str, Any], None, None]:
        """Stream coordinator decisions and workflow executions."""
        # Initialize with user input
        messages = list(input_state.get("messages", []))
        self._transcript.extend(messages)
        
        # Main coordination loop
        while self._iteration_count < self._max_iterations:
            self._iteration_count += 1
            
            # Prepare coordinator input
            coordinator_input = {
                "messages": messages,
                "workflow_context": {
                    "current_workflow": self._current_workflow_spec.model_dump() if self._current_workflow_spec else None,
                    "iteration_count": self._iteration_count,
                    "max_iterations": self._max_iterations,
                    "base_spec": self.base_spec.model_dump()
                },
                "execution_history": self._execution_history,
                "should_continue": True
            }
            
            # Execute coordinator workflow
            coordinator_result = self._coordinator_graph.invoke(coordinator_input, config=config)
            
            # Extract coordinator decision from messages
            coordinator_messages = coordinator_result.get("messages", [])
            if coordinator_messages:
                last_message = coordinator_messages[-1]
                decision_content = getattr(last_message, "content", str(last_message))
                
                # Yield coordinator decision
                yield "coordinator", {"messages": [last_message]}
                
                # Parse coordinator decision and take action
                action_taken = self._process_coordinator_decision(decision_content)
                
                if action_taken.get("terminate"):
                    break
                elif action_taken.get("execute_workflow"):
                    # Execute the generated/current workflow
                    workflow_spec = action_taken.get("workflow_spec")
                    if workflow_spec:
                        yield from self._execute_workflow(workflow_spec, messages, config)
                
                # Update messages for next iteration
                messages = self._transcript[-3:] if len(self._transcript) > 3 else self._transcript
            else:
                # No decision from coordinator, terminate
                break
        
        # Final result
        yield "final", {"messages": self._transcript}

    def _process_coordinator_decision(self, decision_content: str) -> Dict[str, Any]:
        """Process the coordinator's decision and extract actions."""
        try:
            # Look for JSON in the decision content
            if "{" in decision_content and "}" in decision_content:
                start = decision_content.find("{")
                end = decision_content.rfind("}") + 1
                json_part = decision_content[start:end]
                decision_data = json.loads(json_part)
            else:
                # Parse text-based decision
                decision_data = self._parse_text_decision(decision_content)
            
            action = {
                "terminate": decision_data.get("should_terminate", False),
                "execute_workflow": decision_data.get("should_execute", False),
                "regenerate": decision_data.get("should_regenerate", False),
                "workflow_spec": decision_data.get("workflow_specification")
            }
            
            # Update current workflow if new one was generated
            if action["workflow_spec"]:
                from agent_flow.builder.builder import parse_spec_json
                self._current_workflow_spec = parse_spec_json(action["workflow_spec"])
            
            return action
            
        except Exception as e:
            # Default action on parsing error
            return {
                "terminate": True,
                "error": f"Failed to parse coordinator decision: {e}"
            }

    def _parse_text_decision(self, content: str) -> Dict[str, Any]:
        """Parse text-based coordinator decisions."""
        content_lower = content.lower()
        
        decision = {
            "should_terminate": False,
            "should_execute": False,
            "should_regenerate": False
        }
        
        if any(word in content_lower for word in ["terminate", "stop", "done", "complete"]):
            decision["should_terminate"] = True
        elif any(word in content_lower for word in ["execute", "run", "start"]):
            decision["should_execute"] = True
        elif any(word in content_lower for word in ["regenerate", "generate", "create"]):
            decision["should_regenerate"] = True
        
        return decision

    def _execute_workflow(self, workflow_spec: Dict[str, Any], input_messages: List[Any], config: Optional[RunnableConfig]) -> Generator[Tuple[str, Any], None, None]:
        """Execute a user workflow and yield results."""
        try:
            # Build the user workflow
            user_graph, user_settings, _ = build_workflow(workflow_spec)
            
            # Prepare input for user workflow
            workflow_input = {"messages": input_messages}
            
            # Execute user workflow
            if hasattr(user_graph, "stream"):
                for key, event in user_graph.stream(workflow_input, config=config):
                    # Track execution results
                    self._execution_history.append({
                        "type": "step",
                        "key": key,
                        "event": self._safe_serialize(event),
                        "iteration": self._iteration_count
                    })
                    
                    # Extract and store messages
                    msgs = self._extract_messages(event)
                    if msgs:
                        self._transcript.extend(msgs)
                    
                    yield f"workflow_{key}", event
            else:
                # Non-streaming execution
                result = user_graph.invoke(workflow_input, config=config)
                self._execution_history.append({
                    "type": "result",
                    "result": self._safe_serialize(result),
                    "iteration": self._iteration_count
                })
                
                msgs = self._extract_messages(result)
                if msgs:
                    self._transcript.extend(msgs)
                
                yield "workflow_result", result
                
        except Exception as e:
            error_event = {
                "type": "error",
                "error": str(e),
                "iteration": self._iteration_count
            }
            self._execution_history.append(error_event)
            yield "workflow_error", error_event

    def _extract_messages(self, event_or_state: Any) -> List[Any]:
        """Extract messages from workflow events or states."""
        try:
            if isinstance(event_or_state, dict) and "messages" in event_or_state:
                msgs = event_or_state.get("messages", [])
                if isinstance(msgs, list):
                    return msgs
                return [msgs]
        except Exception:
            pass
        return []

    def _safe_serialize(self, obj: Any) -> Any:
        """Safely serialize objects for storage."""
        try:
            if hasattr(obj, "dict"):
                return obj.dict()
            elif hasattr(obj, "model_dump"):
                return obj.model_dump()
            return obj
        except Exception:
            return str(obj)
