"""Workflow router — SSE streaming workflow generation + CRUD endpoints."""
import json

from fastapi import APIRouter, HTTPException, Request, status
from fastapi.responses import StreamingResponse
from pydantic import BaseModel, Field

from synaptiq_api.services.workflow_service import WorkflowService

router = APIRouter()

workflow_service = WorkflowService()


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
    workflows = await workflow_service.list_workflows(tenant_id, limit)
    return {"workflows": workflows}


@router.get(
    "/{workflow_id}",
    summary="Get a specific workflow",
)
async def get_workflow(workflow_id: str, request: Request):
    """Retrieve a saved workflow by ID."""
    tenant_id: str = request.state.tenant_id
    workflow = await workflow_service.get_workflow(workflow_id, tenant_id)
    if not workflow:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Workflow {workflow_id} not found",
        )
    return workflow
