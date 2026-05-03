"""User document model for built-in auth (MongoDB `users` collection)."""
from datetime import datetime

from pydantic import BaseModel, EmailStr, Field

from synaptiq_api.models.base import MongoBaseModel, TimestampMixin


class UserDocument(MongoBaseModel, TimestampMixin):
    """MongoDB user document — stored in the `users` collection."""

    email: EmailStr
    password_hash: str
    display_name: str = ""
    role: str = "tenant_viewer"  # platform_admin | tenant_admin | tenant_editor | tenant_viewer
    tenant_id: str = ""
    must_change_password: bool = False
    email_verified: bool = False
    disabled: bool = False


class ChangePasswordRequest(BaseModel):
    """Request to change user password."""

    current_password: str = Field(..., min_length=1)
    new_password: str = Field(..., min_length=6)


class BuiltinTokenResponse(BaseModel):
    """JWT token response for built-in auth."""

    id_token: str
    refresh_token: str
    expires_in: int
    must_change_password: bool = False
    user: dict | None = None
