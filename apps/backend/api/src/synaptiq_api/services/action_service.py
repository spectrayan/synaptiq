"""Action dispatcher service — Phase 8 (T8.1).

Central hub that receives form-submitted events from the DSL frontend and
dispatches them to the appropriate handler based on ``action_id``.

Responsibilities:
  - Validate the incoming action payload
  - Route to registered handlers
  - Log every action to the ``action_logs`` collection for audit
  - Return a structured result for the frontend
"""
import logging
from datetime import datetime
from typing import Any

from pydantic import BaseModel, Field

from synaptiq_api.core.mongodb import get_db

logger = logging.getLogger(__name__)

LOGS_COLLECTION = "action_logs"


# ---------------------------------------------------------------------------
# DTOs — mirror the frontend FormSubmitEvent
# ---------------------------------------------------------------------------

class ActionRequest(BaseModel):
    """Payload received from POST /api/v1/actions/execute."""

    action: str = Field(..., description="Action identifier (e.g. create_item, update_schema)")
    values: dict[str, Any] = Field(default_factory=dict, description="Form field values")
    metadata: dict[str, Any] = Field(default_factory=dict, description="Extra context (session_id, etc.)")


class ActionResult(BaseModel):
    """Response returned to the frontend."""

    success: bool
    message: str
    data: dict[str, Any] = Field(default_factory=dict)
    suggestions: list[dict[str, str]] = Field(default_factory=list)


# ---------------------------------------------------------------------------
# Handler registry
# ---------------------------------------------------------------------------

ActionHandler = Any  # callable(tenant_id, request) -> ActionResult

_handlers: dict[str, ActionHandler] = {}


def register_handler(action_id: str, handler: ActionHandler) -> None:
    """Register a handler for a given action_id."""
    _handlers[action_id] = handler
    logger.info("Registered action handler: %s", action_id)


# ---------------------------------------------------------------------------
# Dispatcher
# ---------------------------------------------------------------------------

async def dispatch_action(tenant_id: str, request: ActionRequest) -> ActionResult:
    """
    Look up the handler for ``request.action`` and invoke it.

    Every invocation is logged to ``action_logs`` regardless of outcome.
    """
    handler = _handlers.get(request.action)

    if handler is None:
        result = ActionResult(
            success=False,
            message=f"Unknown action: '{request.action}'",
        )
        await _log_action(tenant_id, request, result)
        return result

    try:
        result = await handler(tenant_id, request)
    except ValueError as exc:
        result = ActionResult(success=False, message=str(exc))
    except Exception:
        logger.exception("Action handler '%s' raised", request.action)
        result = ActionResult(success=False, message="An unexpected error occurred.")

    await _log_action(tenant_id, request, result)
    return result


# ---------------------------------------------------------------------------
# Audit log
# ---------------------------------------------------------------------------

async def _log_action(tenant_id: str, request: ActionRequest, result: ActionResult) -> None:
    """Persist an action log entry for observability and analytics."""
    db = get_db()
    await db[LOGS_COLLECTION].insert_one({
        "tenant_id": tenant_id,
        "action": request.action,
        "values": request.values,
        "metadata": request.metadata,
        "success": result.success,
        "message": result.message,
        "created_at": datetime.utcnow(),
    })
