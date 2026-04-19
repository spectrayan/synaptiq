"""Session and conversation turn models.

Sessions (REQ-C3, REQ-NF-SL1): stateless, DB-backed conversation containers.
Turns: individual messages within a session for analytics (REQ-AN1, REQ-AN2).
"""
from datetime import datetime
from enum import StrEnum

from pydantic import BaseModel, Field

from synaptiq_api.models.base import (
    MongoBaseModel,
    TenantScopedMixin,
    TimestampMixin,
)


class MessageRole(StrEnum):
    user = "user"
    assistant = "assistant"
    system = "system"


class ConversationTurn(BaseModel):
    """A single message in the conversation history."""

    turn_id: str = Field(..., description="UUID per turn")
    role: MessageRole
    content: str = ""
    ui_components: list[dict] | None = None
    token_count_input: int = 0
    token_count_output: int = 0
    model_id: str = ""
    latency_ms: int = 0
    created_at: datetime = Field(default_factory=datetime.utcnow)


class ActiveFilter(BaseModel):
    """Currently active catalog filter within a session."""

    field_id: str
    operator: str = "eq"  # eq, gte, lte, in, contains
    value: str | int | float | bool | list[str] = ""


class Session(MongoBaseModel, TenantScopedMixin, TimestampMixin):
    """
    Conversation session — stored in the ``sessions`` collection.

    Anonymous sessions use ``session_id`` (UUID); authenticated sessions
    also carry ``user_uid`` (Phase 2).
    """

    session_id: str = Field(..., description="Client-generated UUID")
    user_uid: str | None = Field(
        default=None, description="Firebase UID — None for anonymous users"
    )
    turns: list[ConversationTurn] = Field(
        default_factory=list,
        description="Conversation history (last N kept in Redis for LLM context)",
    )
    active_filters: list[ActiveFilter] = Field(default_factory=list)
    metadata: dict | None = None
    expires_at: datetime | None = Field(
        default=None,
        description="TTL for session cleanup",
    )

    class Settings:
        collection = "sessions"
