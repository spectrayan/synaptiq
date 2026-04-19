"""Usage ledger models — REQ-PR5 through REQ-PR7, REQ-NF-RL4.

Append-only ledger for seat starts, token consumption, and rate-limit events.
Used for billing reports and platform admin cost rollups.
"""
from enum import StrEnum
from typing import Any

from pydantic import BaseModel, Field

from synaptiq_api.models.base import (
    MongoBaseModel,
    TenantScopedMixin,
    TimestampMixin,
)


class UsageEventType(StrEnum):
    """Types of usage events tracked in the ledger."""
    seat_start = "seat_start"
    token_consumption = "token_consumption"
    rate_limit_hit = "rate_limit_hit"
    action_execution = "action_execution"


class UsageLedgerEntry(MongoBaseModel, TenantScopedMixin, TimestampMixin):
    """
    Append-only usage event — stored in the ``usage_ledger`` collection.

    REQ-PR5: written in real-time on seat starts and token consumption.
    REQ-NF-RL4: rate-limit events are logged here.
    """

    event_type: UsageEventType
    session_id: str = ""
    user_uid: str | None = None

    # Token tracking
    tokens_input: int = Field(default=0, ge=0)
    tokens_output: int = Field(default=0, ge=0)
    model_id: str = ""
    provider: str = ""  # platform_managed, openai, gemini

    # Cost tracking (REQ-PR6, REQ-PR7)
    estimated_cost_usd: float = Field(default=0.0, ge=0.0)

    # Rate limit details (REQ-NF-RL4)
    rate_limit_type: str = ""  # tenant, session
    rate_limit_window_seconds: int = 0

    metadata: dict[str, Any] = Field(default_factory=dict)

    class Settings:
        collection = "usage_ledger"
