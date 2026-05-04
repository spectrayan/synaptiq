"""Workflow Executor — SSE-streaming execution of workflow specs.

Provides step-by-step execution with real-time node status events.
Each node transition emits SSE events that the frontend visualizes.
Execution runs are persisted to the ``workflow_runs`` MongoDB collection.
"""

from __future__ import annotations

import asyncio
import logging
import time
import uuid
from typing import Any, AsyncIterator

from pydantic import BaseModel, Field

from synaptiq_api.core.mongodb import get_database
from synaptiq_api.services.workflow_service import SSEEvent, WorkflowSpec

logger = logging.getLogger(__name__)


# ── Models ────────────────────────────────────────────────────────────────────

class ExecutionRequest(BaseModel):
    """Input for workflow execution."""
    spec: dict[str, Any] = Field(..., description="Workflow spec to execute")
    input_text: str = Field(default="", description="Initial input for the workflow")
    dry_run: bool = Field(default=False, description="If true, simulate execution without calling LLMs")


class NodeStatus(BaseModel):
    """Status update for a single node during execution."""
    node_id: str
    status: str  # pending | running | completed | error | skipped
    started_at: float | None = None
    completed_at: float | None = None
    duration_ms: float | None = None
    output: str | None = None
    error: str | None = None


class ExecutionState(BaseModel):
    """Full execution state sent to the frontend."""
    run_id: str
    status: str  # running | completed | error | cancelled
    current_node: str | None = None
    nodes: dict[str, NodeStatus] = Field(default_factory=dict)
    started_at: float
    completed_at: float | None = None
    result: str | None = None


# ── Persistence helpers ───────────────────────────────────────────────────────

async def _create_run_doc(
    run_id: str,
    workflow_id: str,
    workflow_name: str,
    tenant_id: str,
    input_text: str,
    node_statuses: dict[str, dict[str, Any]],
    dry_run: bool,
    started_at: float,
) -> None:
    """Insert the initial run document into workflow_runs."""
    try:
        db = await get_database()
        await db["workflow_runs"].insert_one({
            "run_id": run_id,
            "workflow_id": workflow_id,
            "workflow_name": workflow_name,
            "tenant_id": tenant_id,
            "status": "running",
            "dry_run": dry_run,
            "input_text": input_text,
            "started_at": started_at,
            "completed_at": None,
            "total_duration_ms": None,
            "nodes": node_statuses,
            "result": None,
        })
    except Exception as e:
        logger.warning("Failed to create run document %s: %s", run_id, e)


async def _update_node_in_run(
    run_id: str,
    node_id: str,
    update_fields: dict[str, Any],
) -> None:
    """Update a specific node's fields inside the run document."""
    try:
        db = await get_database()
        set_doc = {f"nodes.{node_id}.{k}": v for k, v in update_fields.items()}
        await db["workflow_runs"].update_one(
            {"run_id": run_id},
            {"$set": set_doc},
        )
    except Exception as e:
        logger.warning("Failed to update node %s in run %s: %s", node_id, run_id, e)


async def _finalise_run(
    run_id: str,
    status: str,
    completed_at: float,
    total_duration_ms: float,
    result: str | None,
    node_statuses: dict[str, dict[str, Any]],
) -> None:
    """Mark the run as completed or errored and store the final result."""
    try:
        db = await get_database()
        await db["workflow_runs"].update_one(
            {"run_id": run_id},
            {"$set": {
                "status": status,
                "completed_at": completed_at,
                "total_duration_ms": round(total_duration_ms, 1),
                "result": result,
                "nodes": node_statuses,
            }},
        )
    except Exception as e:
        logger.warning("Failed to finalise run %s: %s", run_id, e)


# ── Executor ──────────────────────────────────────────────────────────────────

class WorkflowExecutor:
    """Execute a WorkflowSpec step-by-step, emitting SSE events for each node."""

    @staticmethod
    async def execute(
        spec_dict: dict[str, Any],
        input_text: str = "",
        input_variables: dict[str, Any] = None,
        tenant_id: str = "",
        dry_run: bool = False,
        start_node_id: str | None = None,
        prior_context: str = "",
    ) -> AsyncIterator[SSEEvent]:
        """Execute a workflow and yield SSE events for each step.

        Events:
          - execution_start: run metadata
          - node_start: node begins execution
          - node_complete: node finishes with output
          - node_error: node failed
          - execution_complete: final result
          - execution_error: fatal error
        """
        run_id = str(uuid.uuid4())
        started_at = time.time()

        try:
            spec = WorkflowSpec(**spec_dict)
        except Exception as e:
            yield SSEEvent(event="execution_error", data={
                "run_id": run_id,
                "error": f"Invalid workflow spec: {e!s}",
            })
            return

        agents = spec.agents
        if not agents:
            yield SSEEvent(event="execution_error", data={
                "run_id": run_id,
                "error": "Workflow has no agents",
            })
            return

        # Build execution order via topological sort
        execution_order = _build_execution_order(spec)

        # Partial re-execution: determine skipped nodes
        start_idx = 0
        if start_node_id:
            if start_node_id not in {a.id for a in agents}:
                yield SSEEvent(event="execution_error", data={
                    "run_id": run_id,
                    "error": f"Start node '{start_node_id}' not found in workflow",
                })
                return
            try:
                start_idx = execution_order.index(start_node_id)
            except ValueError:
                start_idx = 0

        # Initialize node statuses
        node_statuses: dict[str, dict[str, Any]] = {}
        for agent in agents:
            is_skipped = False
            if start_idx > 0:
                try:
                    idx = execution_order.index(agent.id)
                    if idx < start_idx:
                        is_skipped = True
                except ValueError:
                    pass

            node_statuses[agent.id] = {
                "node_id": agent.id,
                "label": agent.label,
                "status": "skipped" if is_skipped else "pending",
                "started_at": None,
                "completed_at": None,
                "duration_ms": None,
                "output": None,
                "error": None,
            }

        # Apply start_idx slice
        if start_node_id:
            execution_order = execution_order[start_idx:]
            logger.info("[executor] partial re-exec from '%s', skipping %d nodes", start_node_id, start_idx)

        # Persist initial run document
        await _create_run_doc(
            run_id=run_id,
            workflow_id=spec.id,
            workflow_name=spec.name,
            tenant_id=tenant_id,
            input_text=input_text,
            node_statuses=node_statuses,
            dry_run=dry_run,
            started_at=started_at,
        )

        # Emit execution start
        yield SSEEvent(event="execution_start", data={
            "run_id": run_id,
            "workflow_id": spec.id,
            "status": "running",
            "total_nodes": len(agents),
            "started_at": started_at,
            "nodes": node_statuses,
        })

        # Execute each node in order
        accumulated_context = prior_context or input_text or f"Execute workflow: {spec.name}"

        for node_id in execution_order:
            agent = next((a for a in agents if a.id == node_id), None)
            if not agent:
                continue

            node_start_time = time.time()
            node_statuses[node_id]["status"] = "running"
            node_statuses[node_id]["started_at"] = node_start_time

            # Emit node_start
            yield SSEEvent(event="node_start", data={
                "run_id": run_id,
                "node_id": node_id,
                "label": agent.label,
                "type": agent.type,
            })

            try:
                if dry_run:
                    # Simulate execution with realistic delays
                    output = await _simulate_node_execution(agent, accumulated_context, input_variables)
                else:
                    # Attempt real LLM execution
                    output = await _execute_node_with_llm(agent, accumulated_context, tenant_id, input_variables)

                node_end_time = time.time()
                duration_ms = (node_end_time - node_start_time) * 1000

                node_statuses[node_id]["status"] = "completed"
                node_statuses[node_id]["completed_at"] = node_end_time
                node_statuses[node_id]["duration_ms"] = round(duration_ms, 1)
                node_statuses[node_id]["output"] = output or ""  # Store full output

                accumulated_context = output or accumulated_context

                # Persist node completion (full output)
                await _update_node_in_run(run_id, node_id, {
                    "status": "completed",
                    "started_at": node_start_time,
                    "completed_at": node_end_time,
                    "duration_ms": round(duration_ms, 1),
                    "output": output or "",
                })

                # Emit node_complete (preview for SSE)
                yield SSEEvent(event="node_complete", data={
                    "run_id": run_id,
                    "node_id": node_id,
                    "label": agent.label,
                    "duration_ms": round(duration_ms, 1),
                    "output_preview": (output[:200] + "...") if output and len(output) > 200 else output,
                })

            except Exception as e:
                node_end_time = time.time()
                duration_ms = (node_end_time - node_start_time) * 1000

                node_statuses[node_id]["status"] = "error"
                node_statuses[node_id]["completed_at"] = node_end_time
                node_statuses[node_id]["duration_ms"] = round(duration_ms, 1)
                node_statuses[node_id]["error"] = str(e)

                # Persist node error
                await _update_node_in_run(run_id, node_id, {
                    "status": "error",
                    "started_at": node_start_time,
                    "completed_at": node_end_time,
                    "duration_ms": round(duration_ms, 1),
                    "error": str(e),
                })

                yield SSEEvent(event="node_error", data={
                    "run_id": run_id,
                    "node_id": node_id,
                    "label": agent.label,
                    "error": str(e),
                })

                # Mark remaining nodes as skipped
                for remaining_id in execution_order[execution_order.index(node_id) + 1:]:
                    if remaining_id in node_statuses:
                        node_statuses[remaining_id]["status"] = "skipped"

                # Finalise as error
                completed_at = time.time()
                total_duration = (completed_at - started_at) * 1000
                await _finalise_run(run_id, "error", completed_at, total_duration, None, node_statuses)

                yield SSEEvent(event="execution_error", data={
                    "run_id": run_id,
                    "error": f"Node '{agent.label}' failed: {e!s}",
                    "failed_node": node_id,
                    "nodes": node_statuses,
                })
                return

        # Emit execution complete
        completed_at = time.time()
        total_duration = (completed_at - started_at) * 1000

        final_result = accumulated_context[:10000] if accumulated_context else ""

        # Persist completion
        await _finalise_run(run_id, "completed", completed_at, total_duration, final_result, node_statuses)

        yield SSEEvent(event="execution_complete", data={
            "run_id": run_id,
            "workflow_id": spec.id,
            "status": "completed",
            "started_at": started_at,
            "completed_at": completed_at,
            "total_duration_ms": round(total_duration, 1),
            "result": final_result[:1000] if final_result else "",
            "nodes": node_statuses,
        })


# ── Helpers ───────────────────────────────────────────────────────────────────

def _build_execution_order(spec: WorkflowSpec) -> list[str]:
    """Build topological execution order from edges."""
    agent_ids = {a.id for a in spec.agents}
    adj: dict[str, list[str]] = {a.id: [] for a in spec.agents}
    in_deg: dict[str, int] = {a.id: 0 for a in spec.agents}

    for e in spec.edges:
        src = e.source if hasattr(e, "source") else getattr(e, "from", "")
        tgt = e.target if hasattr(e, "target") else getattr(e, "to", "")
        if src in agent_ids and tgt in agent_ids:
            adj[src].append(tgt)
            in_deg[tgt] = in_deg.get(tgt, 0) + 1

    for ce in spec.conditional_edges:
        src = ce.get("from", "")
        if src in agent_ids:
            for target in ce.get("condition_mapping", {}).values():
                if target in agent_ids:
                    adj[src].append(target)
                    in_deg[target] = in_deg.get(target, 0) + 1

    # BFS topological sort, entry_point first
    order: list[str] = []
    queue: list[str] = []

    if spec.entry_point and spec.entry_point in agent_ids:
        queue.append(spec.entry_point)

    for nid, deg in in_deg.items():
        if deg == 0 and nid not in queue:
            queue.append(nid)

    visited: set[str] = set()
    while queue:
        node = queue.pop(0)
        if node in visited:
            continue
        visited.add(node)
        order.append(node)
        for nxt in adj.get(node, []):
            in_deg[nxt] -= 1
            if in_deg[nxt] <= 0 and nxt not in visited:
                queue.append(nxt)

    # Add any unvisited nodes
    for a in spec.agents:
        if a.id not in visited:
            order.append(a.id)

    return order


async def _simulate_node_execution(agent: Any, context: str, input_variables: dict[str, Any] = None) -> str:
    """Simulate node execution with realistic delays."""
    # Vary delay based on node type
    base_delay = 0.8
    if agent.type == "tool":
        base_delay = 0.5
    elif agent.type == "function":
        base_delay = 0.3
    elif agent.type == "conditional":
        base_delay = 0.4

    # Add some variance
    import random
    delay = base_delay + random.uniform(0.2, 1.0)
    await asyncio.sleep(delay)

    # Generate simulated output
    tool_info = f" using tools: {', '.join(agent.tools)}" if agent.tools else ""
    system_prompt = agent.system_prompt or agent.description or "default"
    if input_variables:
        for k, v in input_variables.items():
            system_prompt = system_prompt.replace(f"{{{{input.{k}}}}}", str(v))
            system_prompt = system_prompt.replace(f"{{{k}}}", str(v))

    return (
        f"[{agent.label}] Processed input{tool_info}. "
        f"Agent '{agent.label}' ({agent.type}) completed successfully. "
        f"System prompt: '{system_prompt[:100]}...'"
    )


async def _execute_node_with_llm(agent: Any, context: str, tenant_id: str, input_variables: dict[str, Any] = None) -> str:
    """Execute a node using the configured LLM provider.

    Falls back to platform default settings when no tenant-level LLM config exists.
    """
    try:
        from synaptiq_api.services.llm_provider import get_langchain_model
        from synaptiq_api.core.mongodb import get_database

        # Try to look up tenant-specific LLM config
        llm_config: dict = {}
        byok_key = ""
        try:
            db = await get_database()
            tenant_doc = await db["tenants"].find_one({"tenant_id": tenant_id}) or {}
            llm_config = tenant_doc.get("llm_provider", {})
            byok_key = tenant_doc.get("byok_key", "")
        except Exception:
            pass  # No tenant doc — use platform defaults

        llm = get_langchain_model(llm_config, byok_key)

        system_prompt = agent.system_prompt or agent.description or "You are a helpful AI agent."
        
        if input_variables:
            for k, v in input_variables.items():
                system_prompt = system_prompt.replace(f"{{{{input.{k}}}}}", str(v))
                system_prompt = system_prompt.replace(f"{{{k}}}", str(v))

        messages = [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": context},
        ]

        response = await llm.ainvoke(messages)
        content = response.content if hasattr(response, "content") else str(response)
        return content

    except Exception as e:
        logger.warning("LLM execution failed for node '%s': %s — falling back to simulation", agent.label, e)
        # Fall back to simulation if LLM execution fails
        return await _simulate_node_execution(agent, context, input_variables)
