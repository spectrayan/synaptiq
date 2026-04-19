"""Actions router — Phase 8 (T8.3).

Provides a single endpoint for executing DSL form actions:

    POST /api/v1/actions/execute

The frontend sends a ``FormSubmitEvent`` here when a user submits
a DSL form component (e.g. "Add Product", "Contact Us").
"""
import logging

from fastapi import APIRouter, Request
from pydantic import BaseModel, Field
from typing import Any

from synaptiq_api.services.action_service import (
    ActionRequest,
    ActionResult,
    dispatch_action,
)

# Import handlers to trigger registration
import synaptiq_api.services.actions  # noqa: F401

logger = logging.getLogger(__name__)

router = APIRouter()


# ---------------------------------------------------------------------------
# Request model (maps to frontend FormSubmitEvent)
# ---------------------------------------------------------------------------

class ExecuteActionBody(BaseModel):
    """POST /actions/execute body — matches the DSL FormSubmitEvent shape."""

    action: str = Field(..., min_length=1, max_length=100, description="Action identifier")
    values: dict[str, Any] = Field(default_factory=dict, description="Form field values")
    metadata: dict[str, Any] = Field(
        default_factory=dict,
        description="Extra context (session_id, source component, etc.)",
    )


# ---------------------------------------------------------------------------
# Response model
# ---------------------------------------------------------------------------

class ExecuteActionResponse(BaseModel):
    """Standard action response."""

    success: bool
    message: str
    data: dict[str, Any] = Field(default_factory=dict)
    suggestions: list[dict[str, str]] = Field(default_factory=list)


# ---------------------------------------------------------------------------
# Endpoint
# ---------------------------------------------------------------------------

@router.post(
    "/execute",
    summary="Execute a DSL form action",
    response_model=ExecuteActionResponse,
)
async def execute_action(body: ExecuteActionBody, request: Request) -> ExecuteActionResponse:
    """
    Dispatch a form submission to the appropriate action handler.

    Supported actions:
      - ``create_item``      — Create a catalog item from form data
      - ``update_schema``    — Add/update a schema field (admin)
      - ``contact_enquiry``  — Save an enquiry / lead capture

    All actions are logged to the ``action_logs`` collection for audit.
    """
    tenant_id: str = request.state.tenant_id

    action_request = ActionRequest(
        action=body.action,
        values=body.values,
        metadata=body.metadata,
    )

    result: ActionResult = await dispatch_action(tenant_id, action_request)

    return ExecuteActionResponse(
        success=result.success,
        message=result.message,
        data=result.data,
        suggestions=result.suggestions,
    )
