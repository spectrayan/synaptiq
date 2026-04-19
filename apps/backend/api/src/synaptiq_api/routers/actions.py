"""Actions router — Phase 8 (T8.1–T8.6).

Provides endpoints for executing DSL form actions and retrieving saved items:

    POST  /api/v1/actions/execute
    GET   /api/v1/actions/saved_items?session_id=
    DELETE /api/v1/actions/saved_items/{item_id}?session_id=

All actions are logged to the ``action_logs`` collection for audit (T8.5).
"""
import asyncio
import logging
from typing import Any

from fastapi import APIRouter, HTTPException, Query, Request
from pydantic import BaseModel, Field

from synaptiq_api.core.mongodb import get_db
from synaptiq_api.services.action_service import (
    ActionRequest,
    ActionResult,
    dispatch_action,
)

# Import handlers to trigger registration
import synaptiq_api.services.actions  # noqa: F401

logger = logging.getLogger(__name__)

router = APIRouter()

# Max retries for transient failures (T8.6)
MAX_RETRIES = 3
RETRY_BASE_DELAY = 0.3  # seconds


# ---------------------------------------------------------------------------
# Request/Response models
# ---------------------------------------------------------------------------

class ExecuteActionBody(BaseModel):
    """POST /actions/execute body — matches the DSL FormSubmitEvent shape."""

    action: str = Field(..., min_length=1, max_length=100, description="Action identifier")
    values: dict[str, Any] = Field(default_factory=dict, description="Form field values")
    metadata: dict[str, Any] = Field(
        default_factory=dict,
        description="Extra context (session_id, source component, etc.)",
    )


class ExecuteActionResponse(BaseModel):
    """Standard action response."""

    success: bool
    message: str
    data: dict[str, Any] = Field(default_factory=dict)
    suggestions: list[dict[str, str]] = Field(default_factory=list)


class SavedItemOut(BaseModel):
    """Serialised saved item for the frontend."""

    item_id: str
    session_id: str
    item_snapshot: dict[str, Any] = Field(default_factory=dict)
    created_at: str | None = None


class SavedItemsResponse(BaseModel):
    """GET /actions/saved_items response."""

    items: list[SavedItemOut] = Field(default_factory=list)
    total: int = 0


# ---------------------------------------------------------------------------
# Tenant config permission check (T8.4)
# ---------------------------------------------------------------------------

async def _check_action_permission(tenant_id: str, action: str) -> None:
    """
    Verify that the action is enabled in the tenant's configuration.

    Raises ``HTTPException(403)`` if the action is explicitly disabled.
    """
    db = get_db()
    tenant = await db["tenants"].find_one({"_id": tenant_id}, {"config": 1})

    if not tenant or not tenant.get("config"):
        return  # No config → allow all (defaults-open)

    config = tenant["config"]
    disabled_actions: list[str] = config.get("disabled_actions", [])

    if action in disabled_actions:
        raise HTTPException(
            status_code=403,
            detail=f"Action '{action}' is disabled for this tenant.",
        )


# ---------------------------------------------------------------------------
# Retry wrapper (T8.6)
# ---------------------------------------------------------------------------

async def _dispatch_with_retry(
    tenant_id: str,
    request: ActionRequest,
    max_retries: int = MAX_RETRIES,
) -> ActionResult:
    """
    Dispatch with exponential backoff retry on transient failures (T8.6).

    Only retries on unexpected exceptions, not on business-logic failures
    (those return ActionResult with success=False).
    """
    last_exc: Exception | None = None

    for attempt in range(max_retries):
        try:
            result = await dispatch_action(tenant_id, request)
            return result
        except (ConnectionError, TimeoutError, OSError) as exc:
            last_exc = exc
            if attempt < max_retries - 1:
                delay = RETRY_BASE_DELAY * (2 ** attempt)
                logger.warning(
                    "Action '%s' attempt %d failed (transient), retrying in %.1fs: %s",
                    request.action, attempt + 1, delay, exc,
                )
                await asyncio.sleep(delay)
            else:
                logger.error(
                    "Action '%s' failed after %d retries: %s",
                    request.action, max_retries, exc,
                )

    return ActionResult(
        success=False,
        message=f"Action failed after {max_retries} retries. Please try again later.",
        data={"error": str(last_exc) if last_exc else "Unknown error"},
    )


# ---------------------------------------------------------------------------
# Endpoints
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
      - ``save_item``        — Bookmark a catalog item

    Pre-flight checks:
      - T8.4: Permission check against tenant config
      - T8.5: Append-only audit log on every execution
      - T8.6: Retry transient failures 3× with exponential backoff
    """
    tenant_id: str = request.state.tenant_id

    # T8.4 — permission check
    await _check_action_permission(tenant_id, body.action)

    action_request = ActionRequest(
        action=body.action,
        values=body.values,
        metadata=body.metadata,
    )

    # T8.6 — dispatch with retry
    result: ActionResult = await _dispatch_with_retry(tenant_id, action_request)

    return ExecuteActionResponse(
        success=result.success,
        message=result.message,
        data=result.data,
        suggestions=result.suggestions,
    )


@router.get(
    "/saved_items",
    summary="Retrieve saved items for a session",
    response_model=SavedItemsResponse,
)
async def get_saved_items(
    request: Request,
    session_id: str = Query(..., min_length=1, description="Session ID to filter by"),
) -> SavedItemsResponse:
    """
    Retrieve all saved/bookmarked items for a given session (T8.2).

    Returns items with their full snapshot data.
    """
    tenant_id: str = request.state.tenant_id
    db = get_db()

    cursor = db["saved_items"].find(
        {"tenant_id": tenant_id, "session_id": session_id},
    ).sort("created_at", -1)

    items: list[SavedItemOut] = []
    async for doc in cursor:
        items.append(SavedItemOut(
            item_id=doc.get("item_id", ""),
            session_id=doc.get("session_id", ""),
            item_snapshot=doc.get("item_snapshot", {}),
            created_at=doc["created_at"].isoformat() if doc.get("created_at") else None,
        ))

    return SavedItemsResponse(items=items, total=len(items))


@router.delete(
    "/saved_items/{item_id}",
    summary="Remove a saved item",
)
async def delete_saved_item(
    item_id: str,
    request: Request,
    session_id: str = Query(..., min_length=1, description="Session ID"),
) -> dict[str, Any]:
    """Remove a previously saved item from the user's list."""
    tenant_id: str = request.state.tenant_id
    db = get_db()

    result = await db["saved_items"].delete_one({
        "tenant_id": tenant_id,
        "item_id": item_id,
        "session_id": session_id,
    })

    if result.deleted_count == 0:
        raise HTTPException(status_code=404, detail="Saved item not found.")

    return {"success": True, "message": f"Item {item_id} removed from saved items."}
