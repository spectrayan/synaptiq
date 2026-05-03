"""Tools for dynamic workflow generation and coordination."""

from __future__ import annotations

import json
import os
from typing import Any, Dict, List, Optional

from langchain_core.tools import tool

from agent_flow.builder.builder import build_workflow, parse_spec_json
from agent_flow.builder.models.settings import FlowSettings, FlowType
from agent_flow.flow_generation.generator import get_generator


def _coerce_flow_type(flow_type: Optional[str]) -> FlowType:
    if not flow_type:
        return FlowType.dynamic
    try:
        return FlowType(flow_type)
    except Exception:
        # default to dynamic if unknown
        return FlowType.dynamic


@tool
def generate_flow_spec(user_input: str, base_spec_json: str | None = None, flow_type: str | None = None) -> str:
    """Generate a Flow Settings JSON using the configured generator.

    - user_input: Free-form prompt or instruction to drive generation.
    - base_spec_json: Optional base Flow Settings JSON (used by static/hybrid or to seed dynamic generator).
    - flow_type: One of {"static", "dynamic", "hybrid"}. Defaults to "dynamic".

    Returns validated Flow Settings as a JSON string.
    """
    ftype = _coerce_flow_type(flow_type)
    GenCls = get_generator(ftype)
    generator = GenCls()

    base_spec = parse_spec_json(base_spec_json) if base_spec_json else None
    # Prepare a dict input expected by DynamicFlowGenerator
    payload: dict[str, Any] = {"prompt": user_input}

    spec = generator.generate(user_input=payload, base_spec=base_spec)
    # Ensure flowType stays as requested for static/hybrid, and defaults to static for dynamic per generator
    if ftype in (FlowType.static, FlowType.hybrid):
        spec.flowType = ftype
    return spec.model_dump_json()


@tool
def generate_workflow(
    user_requirements: str,
    context: Optional[str] = None,
    execution_history: Optional[str] = None
) -> str:
    """Generate a workflow specification using the generator agent.
    
    Args:
        user_requirements: Natural language description of workflow requirements
        context: Additional context about user environment or constraints (JSON string)
        execution_history: Previous execution results for context (JSON string)
        
    Returns:
        JSON string containing the result with status and specification
    """
    try:
        # Parse optional JSON inputs
        context_dict = json.loads(context) if context else {}
        history_list = json.loads(execution_history) if execution_history else []
        
        # Load generator settings
        generator_settings_path = os.path.join(
            os.path.dirname(__file__), 
            "..", 
            "flow_generation", 
            "generator_settings.json"
        )
        
        with open(generator_settings_path, "r") as f:
            generator_settings = json.load(f)
        
        # Build generator workflow
        generator_graph, generator_spec, _ = build_workflow(
            spec_json={
                "entry_point": "generator_agent", 
                "nodes": {
                    "generator_agent": {
                        "type": "agent", 
                        "agent": "generator_agent"
                    }
                },
                "edges": []
            },
            settings_json=generator_settings
        )
        
        # Prepare input for generator
        generator_input = {
            "messages": [{
                "type": "human",
                "content": f"""Generate a workflow specification for the following requirements:

Requirements: {user_requirements}

Context: {json.dumps(context_dict, indent=2)}

Execution History: {json.dumps(history_list, indent=2)}

Please generate a complete FlowSettings JSON specification that addresses these requirements."""
            }]
        }
        
        # Execute generator
        result = generator_graph.invoke(generator_input)
        
        # Extract generated specification from the last message
        if "messages" in result and result["messages"]:
            last_message = result["messages"][-1]
            content = getattr(last_message, "content", str(last_message))
            
            # Clean up the content (remove code fences if present)
            if isinstance(content, str):
                content = content.strip()
                if content.startswith("```"):
                    lines = content.split("\n")
                    if len(lines) > 2:
                        # Remove first and last lines (code fences)
                        content = "\n".join(lines[1:-1])
                
                # Parse and validate the generated specification
                try:
                    spec_dict = json.loads(content)
                    # Validate by parsing as FlowSettings
                    flow_settings = parse_spec_json(spec_dict)
                    return json.dumps({
                        "status": "success",
                        "specification": flow_settings.model_dump(),
                        "message": "Workflow specification generated successfully"
                    })
                except json.JSONDecodeError as e:
                    return json.dumps({
                        "status": "error",
                        "error": f"Generated content is not valid JSON: {e}",
                        "content": content[:500] + "..." if len(content) > 500 else content
                    })
                except Exception as e:
                    return json.dumps({
                        "status": "error", 
                        "error": f"Generated specification is invalid: {e}",
                        "content": content[:500] + "..." if len(content) > 500 else content
                    })
        
        return json.dumps({
            "status": "error",
            "error": "Generator did not produce a valid response",
            "result": str(result)
        })
        
    except Exception as e:
        return json.dumps({
            "status": "error",
            "error": f"Failed to generate workflow: {e}"
        })


@tool
def analyze_execution(
    workflow_specification: str,
    execution_results: str,
    user_feedback: Optional[str] = None,
    iteration_count: int = 0,
    max_iterations: int = 3
) -> str:
    """Analyze execution results and determine next steps.
    
    Args:
        workflow_specification: Current workflow specification (JSON string)
        execution_results: Results from workflow execution (JSON string)
        user_feedback: Optional user feedback
        iteration_count: Current iteration number
        max_iterations: Maximum allowed iterations
        
    Returns:
        JSON string with analysis results and recommendations
    """
    try:
        # Parse inputs
        workflow_spec = json.loads(workflow_specification) if workflow_specification else {}
        execution_list = json.loads(execution_results) if execution_results else []
        
        # Basic analysis logic
        analysis = {
            "iteration_count": iteration_count,
            "max_iterations": max_iterations,
            "should_continue": iteration_count < max_iterations,
            "should_regenerate": False,
            "should_terminate": False,
            "confidence_score": 0.0,
            "recommendations": []
        }
        
        # Check if we've hit iteration limit
        if iteration_count >= max_iterations:
            analysis.update({
                "should_continue": False,
                "should_terminate": True,
                "recommendations": ["Maximum iterations reached. Terminating execution."]
            })
            return json.dumps(analysis)
        
        # Analyze execution results
        if execution_list:
            last_result = execution_list[-1]
            
            # Check for errors
            if last_result.get("type") == "error":
                analysis.update({
                    "should_regenerate": True,
                    "confidence_score": 0.2,
                    "recommendations": [
                        "Execution failed with error. Consider regenerating workflow.",
                        f"Error: {last_result.get('error', 'Unknown error')}"
                    ]
                })
            
            # Check for successful completion
            elif last_result.get("type") == "result":
                analysis.update({
                    "should_terminate": True,
                    "confidence_score": 0.9,
                    "recommendations": [
                        "Workflow executed successfully. Task completed."
                    ]
                })
            
            # Check for partial progress
            else:
                analysis.update({
                    "should_continue": True,
                    "confidence_score": 0.7,
                    "recommendations": [
                        "Workflow is progressing. Continue execution."
                    ]
                })
        
        # Consider user feedback
        if user_feedback:
            feedback_lower = user_feedback.lower()
            if any(word in feedback_lower for word in ["stop", "terminate", "done", "finished"]):
                analysis.update({
                    "should_terminate": True,
                    "recommendations": analysis["recommendations"] + ["User requested termination."]
                })
            elif any(word in feedback_lower for word in ["regenerate", "restart", "different", "change"]):
                analysis.update({
                    "should_regenerate": True,
                    "recommendations": analysis["recommendations"] + ["User requested workflow regeneration."]
                })
        
        return json.dumps(analysis)
        
    except Exception as e:
        return json.dumps({
            "status": "error",
            "error": f"Failed to analyze execution: {e}",
            "should_terminate": True,
            "recommendations": ["Analysis failed. Terminating for safety."]
        })
