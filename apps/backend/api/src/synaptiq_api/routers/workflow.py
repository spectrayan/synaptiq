"""Workflow router — SSE streaming workflow generation, execution + CRUD endpoints."""
import json
import logging
from typing import Any

logger = logging.getLogger(__name__)

from fastapi import APIRouter, HTTPException, Request, status, WebSocket, WebSocketDisconnect
from fastapi.responses import StreamingResponse
from pydantic import BaseModel, Field

from synaptiq_api.services.workflow_service import WorkflowService
from synaptiq_api.services.workflow_executor import WorkflowExecutor
from synaptiq_api.services.tool_registry import get_all_tools, get_tools_by_category, TOOL_CATEGORIES
from synaptiq_api.websockets.connection_manager import manager

router = APIRouter()

workflow_service = WorkflowService()
workflow_executor = WorkflowExecutor()


# ---------------------------------------------------------------------------
# Request / Response models
# ---------------------------------------------------------------------------

class GenerateWorkflowRequest(BaseModel):
    """POST /workflow/generate — generate a workflow from natural language."""
    prompt: str = Field(..., min_length=5, max_length=4000, description="Natural language description of the desired workflow")
    session_id: str | None = Field(default=None, description="Associated chat session (optional)")


class SaveWorkflowRequest(BaseModel):
    """POST /workflow/save — persist a workflow spec."""
    spec: dict = Field(..., description="Complete workflow specification JSON")


class UpdateWorkflowRequest(BaseModel):
    """PATCH /workflow/{workflow_id} — update an existing workflow spec."""
    spec: dict[str, Any] = Field(..., description="Full or partial workflow specification updates")


class ExecuteWorkflowRequest(BaseModel):
    """POST /workflow/execute — execute a workflow spec step-by-step."""
    spec: dict[str, Any] = Field(..., description="Complete workflow specification JSON")
    input_text: str = Field(default="", description="Initial input text for the workflow")
    input_variables: dict[str, Any] = Field(default_factory=dict, description="Values for the workflow input variables")
    dry_run: bool = Field(default=False, description="Simulate execution without calling LLMs")
    start_node_id: str | None = Field(default=None, description="Start execution from this node (partial re-execution)")
    prior_context: str = Field(default="", description="Context to use when starting from a non-entry node")


class RegeneratePromptRequest(BaseModel):
    """POST /workflow/regenerate-prompt — ask AI to improve a node's system prompt."""
    node_id: str = Field(..., description="ID of the node whose prompt to improve")
    node_label: str = Field(default="", description="Label of the node for context")
    node_description: str = Field(default="", description="Description of the node")
    current_prompt: str = Field(default="", description="The current system prompt to improve")
    instruction: str = Field(default="", description="User instruction: 'Make more concise', 'Add error handling', etc.")
    workflow_context: dict[str, Any] = Field(default_factory=dict, description="Full workflow spec for context")


# ---------------------------------------------------------------------------
# SSE Generation endpoint
# ---------------------------------------------------------------------------

@router.post(
    "/generate",
    summary="Generate a workflow from natural language (SSE stream)",
    response_class=StreamingResponse,
)
async def generate_workflow(body: GenerateWorkflowRequest, request: Request):
    """
    Stream workflow generation via Server-Sent Events.

    Events emitted:
      - `status`: progress messages
      - `component`: the generated workflow spec (type=workflow)
      - `text`: summary description
      - `error`: error messages
      - `done`: generation complete
    """
    tenant_id: str = request.state.tenant_id

    async def event_stream():
        async for event in workflow_service.generate_workflow(
            prompt=body.prompt,
            tenant_id=tenant_id,
        ):
            data = event.data
            if isinstance(data, dict):
                data = json.dumps(data, default=str)
            elif data is None:
                data = ""
            yield f"event: {event.event}\ndata: {data}\n\n"

    return StreamingResponse(
        event_stream(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "X-Accel-Buffering": "no",
        },
    )


# ---------------------------------------------------------------------------
# Templates
# ---------------------------------------------------------------------------

@router.get(
    "/community-templates",
    summary="List public community workflow templates",
)
async def list_templates(limit: int = 50):
    """List workflows that have been shared publicly to be used as templates."""
    logger.info("[workflow] list_templates limit=%d", limit)
    templates = await workflow_service.list_public_templates(limit)
    return {"templates": templates}


# ---------------------------------------------------------------------------
# Prompt Regeneration
# ---------------------------------------------------------------------------

@router.post(
    "/regenerate-prompt",
    summary="Regenerate a node's system prompt using AI",
)
async def regenerate_prompt(body: RegeneratePromptRequest, request: Request):
    """Use an LLM to improve or regenerate a specific node's system prompt."""
    tenant_id: str = request.state.tenant_id
    logger.info(
        "[workflow] regenerate_prompt node=%s instruction=%s tenant=%s",
        body.node_id, body.instruction[:50], tenant_id,
    )
    try:
        improved = await workflow_service.regenerate_prompt(
            node_id=body.node_id,
            node_label=body.node_label,
            node_description=body.node_description,
            current_prompt=body.current_prompt,
            instruction=body.instruction,
            workflow_context=body.workflow_context,
            tenant_id=tenant_id,
        )
        return {"improved_prompt": improved, "success": True}
    except Exception as exc:
        logger.error("[workflow] regenerate_prompt failed: %s", exc)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Prompt regeneration failed: {exc!s}",
        )


# ---------------------------------------------------------------------------
# Templates
# ---------------------------------------------------------------------------

@router.get(
    "/templates",
    summary="Get starter workflow templates",
)
async def get_templates():
    """Return a list of pre-built workflow templates for quick start."""
    templates = await workflow_service.get_templates()
    return {"templates": templates}


# ---------------------------------------------------------------------------
# Tool Registry
# ---------------------------------------------------------------------------

@router.get(
    "/tools",
    summary="Get available tools for workflow agents",
)
async def get_available_tools():
    """Return the full catalog of tools available for workflow agent nodes."""
    return {
        "tools": get_all_tools(),
        "categories": TOOL_CATEGORIES,
        "by_category": get_tools_by_category(),
    }


# ---------------------------------------------------------------------------
# CRUD
# ---------------------------------------------------------------------------

@router.post(
    "/save",
    summary="Save a workflow specification",
    status_code=status.HTTP_201_CREATED,
)
async def save_workflow(body: SaveWorkflowRequest, request: Request):
    """Persist a workflow spec (create or update)."""
    tenant_id: str = request.state.tenant_id
    workflow_id = await workflow_service.save_workflow(body.spec, tenant_id)
    return {"id": workflow_id, "success": True}


@router.get(
    "/list",
    summary="List saved workflows",
)
async def list_workflows(request: Request, limit: int = 20):
    """List all saved workflows for the current tenant."""
    tenant_id: str = request.state.tenant_id
    logger.info("[workflow] list_workflows tenant=%s limit=%d", tenant_id, limit)
    workflows = await workflow_service.list_workflows(tenant_id, limit)
    logger.info("[workflow] list_workflows returned %d workflows", len(workflows))
    return {"workflows": workflows}


@router.get(
    "/{workflow_id}",
    summary="Get a specific workflow",
)
async def get_workflow(workflow_id: str, request: Request):
    """Retrieve a saved workflow by ID."""
    tenant_id: str = request.state.tenant_id
    logger.info("[workflow] get_workflow id=%s tenant=%s", workflow_id, tenant_id)
    workflow = await workflow_service.get_workflow(workflow_id, tenant_id)
    if not workflow:
        logger.warning("[workflow] get_workflow %s not found for tenant %s", workflow_id, tenant_id)
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Workflow {workflow_id} not found",
        )
    logger.info("[workflow] get_workflow found: name=%s agents=%d", workflow.get('name', '?'), len(workflow.get('agents', [])))
    return workflow


@router.patch(
    "/{workflow_id}",
    summary="Update an existing workflow",
)
async def update_workflow(workflow_id: str, body: UpdateWorkflowRequest, request: Request):
    """Partially or fully update a saved workflow spec."""
    tenant_id: str = request.state.tenant_id
    logger.info("[workflow] update_workflow id=%s tenant=%s", workflow_id, tenant_id)
    try:
        updated_at = await workflow_service.update_workflow(workflow_id, body.spec, tenant_id)
        logger.info("[workflow] update_workflow %s updated_at=%s", workflow_id, updated_at)
        return {"id": workflow_id, "success": True, "updated_at": updated_at}
    except ValueError as exc:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(exc))


@router.delete(
    "/{workflow_id}",
    summary="Delete a workflow and its execution history",
    status_code=status.HTTP_204_NO_CONTENT,
)
async def delete_workflow(workflow_id: str, request: Request):
    """Delete a saved workflow and all associated runs."""
    tenant_id: str = request.state.tenant_id
    logger.info("[workflow] delete_workflow id=%s tenant=%s", workflow_id, tenant_id)
    try:
        await workflow_service.delete_workflow(workflow_id, tenant_id)
        logger.info("[workflow] delete_workflow %s done", workflow_id)
    except ValueError as exc:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(exc))


@router.post(
    "/{workflow_id}/duplicate",
    summary="Duplicate an existing workflow",
    status_code=status.HTTP_201_CREATED,
)
async def duplicate_workflow(workflow_id: str, request: Request):
    """Create a copy of an existing workflow with a new ID."""
    tenant_id: str = request.state.tenant_id
    logger.info("[workflow] duplicate_workflow id=%s tenant=%s", workflow_id, tenant_id)
    try:
        new_id = await workflow_service.duplicate_workflow(workflow_id, tenant_id)
        logger.info("[workflow] duplicate_workflow %s → %s", workflow_id, new_id)
        return {"id": new_id, "success": True}
    except ValueError as exc:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(exc))


@router.post(
    "/{workflow_id}/share",
    summary="Generate a public share token for a workflow",
)
async def share_workflow(workflow_id: str, request: Request):
    """Make a workflow public and generate a unique sharing token."""
    tenant_id: str = request.state.tenant_id
    logger.info("[workflow] share_workflow id=%s tenant=%s", workflow_id, tenant_id)
    try:
        share_token = await workflow_service.share_workflow(workflow_id, tenant_id)
        logger.info("[workflow] share_workflow %s -> token %s", workflow_id, share_token)
        return {"share_token": share_token, "success": True}
    except ValueError as exc:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(exc))


@router.get(
    "/shared/{share_token}",
    summary="Get a shared workflow by token",
)
async def get_shared_workflow(share_token: str):
    """Retrieve a workflow using its public share token (no authentication required)."""
    logger.info("[workflow] get_shared_workflow token=%s", share_token)
    workflow = await workflow_service.get_shared_workflow(share_token)
    if not workflow:
        logger.warning("[workflow] get_shared_workflow token=%s not found", share_token)
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Shared workflow not found or is no longer public",
        )
    return workflow


# ---------------------------------------------------------------------------
# Execution
# ---------------------------------------------------------------------------

@router.post(
    "/execute",
    summary="Execute a workflow step-by-step (SSE stream)",
    response_class=StreamingResponse,
)
async def execute_workflow(body: ExecuteWorkflowRequest, request: Request):
    """Stream workflow execution via Server-Sent Events.

    Events emitted:
      - `execution_start`: run metadata and initial node statuses
      - `node_start`: a node begins execution
      - `node_complete`: a node finishes successfully
      - `node_error`: a node failed
      - `execution_complete`: workflow finished
      - `execution_error`: workflow failed
    """
    tenant_id: str = request.state.tenant_id

    async def event_stream():
        async for event in workflow_executor.execute(
            spec_dict=body.spec,
            input_text=body.input_text,
            input_variables=body.input_variables,
            tenant_id=tenant_id,
            dry_run=body.dry_run,
            start_node_id=body.start_node_id,
            prior_context=body.prior_context,
        ):
            data = event.data
            if isinstance(data, dict):
                data = json.dumps(data, default=str)
            elif data is None:
                data = ""
            yield f"event: {event.event}\ndata: {data}\n\n"

    return StreamingResponse(
        event_stream(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "X-Accel-Buffering": "no",
        },
    )


# ---------------------------------------------------------------------------
# Execution History
# ---------------------------------------------------------------------------

@router.get(
    "/{workflow_id}/runs",
    summary="List past execution runs for a workflow",
)
async def list_workflow_runs(
    workflow_id: str, request: Request, limit: int = 20,
):
    """List past runs (most recent first) for a specific workflow."""
    tenant_id: str = request.state.tenant_id
    logger.info("[workflow] list_runs workflow_id=%s tenant=%s", workflow_id, tenant_id)
    runs = await workflow_service.list_workflow_runs(workflow_id, tenant_id, limit)
    logger.info("[workflow] list_runs returned %d runs", len(runs))
    return {"runs": runs}


@router.get(
    "/runs/{run_id}",
    summary="Get full execution run details",
)
async def get_workflow_run(run_id: str, request: Request):
    """Retrieve a full execution run including all node outputs."""
    tenant_id: str = request.state.tenant_id
    logger.info("[workflow] get_run run_id=%s tenant=%s", run_id, tenant_id)
    run = await workflow_service.get_workflow_run(run_id, tenant_id)
    if not run:
        logger.warning("[workflow] get_run %s not found for tenant %s", run_id, tenant_id)
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Run {run_id} not found",
        )
    logger.info("[workflow] get_run found: status=%s", run.get('status', '?'))
    return run

# ---------------------------------------------------------------------------
# WebSocket endpoint for Real-Time Collaboration
# ---------------------------------------------------------------------------

@router.websocket("/{workflow_id}/sync")
async def websocket_sync_endpoint(websocket: WebSocket, workflow_id: str, token: str | None = None):
    # In a real implementation, we would validate the token to get the user
    # For now, we will assign a random guest name or use token as a hint
    import random
    colors = ['#f43f5e', '#8b5cf6', '#10b981', '#f59e0b', '#3b82f6', '#ec4899']
    user_info = {
        "id": f"user_{id(websocket)}", 
        "name": f"Collaborator {str(id(websocket))[-4:]}",
        "color": random.choice(colors)
    }
    
    await manager.connect(websocket, workflow_id, user_info)
    try:
        while True:
            data = await websocket.receive_text()
            # We expect JSON messages: { "type": "...", ... }
            try:
                message = json.loads(data)
                
                if message.get("type") in ["cursor_move", "node_moved", "spec_change", "node_selected"]:
                    # Broadcast the message to all OTHER clients in this workflow
                    message["user_id"] = user_info["id"]
                    message["user"] = user_info
                    await manager.broadcast(workflow_id, message, exclude=websocket)
            except json.JSONDecodeError:
                logger.warning(f"Invalid JSON received on websocket for {workflow_id}")
                
    except WebSocketDisconnect:
        manager.disconnect(websocket, workflow_id)
        await manager.broadcast(workflow_id, {
            "type": "user_left",
            "user_id": user_info["id"],
            "user": user_info
        })
