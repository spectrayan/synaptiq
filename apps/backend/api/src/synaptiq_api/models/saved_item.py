"""Saved/bookmarked item models — REQ-A7 through REQ-A10.

Saved items are keyed by session_id for anonymous users.
A full item snapshot is stored at save time to handle future catalog changes.
"""
from typing import Any

from pydantic import Field

from synaptiq_api.models.base import (
    MongoBaseModel,
    TenantScopedMixin,
    TimestampMixin,
)


class SavedItem(MongoBaseModel, TenantScopedMixin, TimestampMixin):
    """
    A bookmarked catalog item — stored in the ``saved_items`` collection.

    REQ-A10: includes item_id, tenant_id, session_id, saved_at, item_snapshot.
    """

    item_id: str = Field(..., description="Reference to catalog_items._id")
    session_id: str = Field(..., description="Session that saved this item")
    user_uid: str | None = Field(
        default=None, description="Firebase UID — Phase 2 authenticated users"
    )
    item_snapshot: dict[str, Any] = Field(
        ...,
        description="Full copy of item data at save time (REQ-A10)",
    )

    class Settings:
        collection = "saved_items"
