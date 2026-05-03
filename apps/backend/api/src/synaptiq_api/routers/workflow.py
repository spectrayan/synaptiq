"""Workflow router — SSE streaming workflow generation, execution + CRUD endpoints."""
import json
import logging
from typing import Any

logger = logging.getLogger(__name__)

from fastapi import APIRouter, HTTPException, Request, status
from fastapi.responses import StreamingResponse
from pydantic import BaseModel, Field

from synaptiq_api.services.workflow_service import WorkflowService
from synaptiq_api.services.workflow_executor import WorkflowExecutor

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
    dry_run: bool = Field(default=False, description="Simulate execution without calling LLMs")


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
    "/templates",
    summary="Get starter workflow templates",
)
async def get_templates():
    """Return a list of pre-built workflow templates for quick start."""
    templates = await workflow_service.get_templates()
    return {"templates": templates}


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
            tenant_id=tenant_id,
            dry_run=body.dry_run,
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
