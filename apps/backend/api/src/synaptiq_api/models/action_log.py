"""Action execution audit log — REQ-A-DET1 through REQ-A-DET5.

All action executions are logged immutably. Logs are append-only and
cannot be modified or deleted by the business admin.
"""
from enum import StrEnum
from typing import Any

from pydantic import Field

from synaptiq_api.models.base import (
    MongoBaseModel,
    TenantScopedMixin,
    TimestampMixin,
)


class ActionOutcome(StrEnum):
    success = "success"
    failure = "failure"
    cancelled = "cancelled"


class ActionLog(MongoBaseModel, TenantScopedMixin, TimestampMixin):
    """
    Immutable action execution log — stored in the ``action_logs`` collection.

    REQ-A-DET3: action_id, session_id, tenant_id, timestamp, input_snapshot,
    outcome, and error (if any).
    """

    action_id: str = Field(..., description="e.g. save_item, contact_enquiry")
    session_id: str
    user_uid: str | None = None
    input_snapshot: dict[str, Any] = Field(
        default_factory=dict,
        description="Copy of the input data at execution time",
    )
    outcome: ActionOutcome = ActionOutcome.success
    error: str | None = None
    retry_count: int = Field(default=0, ge=0, description="REQ-A-DET4: retries used")
    duration_ms: int = 0

    class Settings:
        collection = "action_logs"
