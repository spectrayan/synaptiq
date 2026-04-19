"""Authentication data models."""
from pydantic import BaseModel, EmailStr


class SignUpRequest(BaseModel):
    """Email/password signup request."""

    email: EmailStr
    password: str


class LoginRequest(BaseModel):
    """Email/password login request."""

    email: EmailStr
    password: str


class TokenResponse(BaseModel):
    """Firebase token response."""

    id_token: str
    refresh_token: str
    expires_in: int


class UserResponse(BaseModel):
    """User information response."""

    uid: str
    email: str
    email_verified: bool
    display_name: str | None = None
    custom_claims: dict | None = None


class UserClaimsRequest(BaseModel):
    """Request to update user custom claims."""

    role: str  # platform_admin, tenant_admin, tenant_editor, tenant_viewer


class RefreshTokenRequest(BaseModel):
    """Firebase token refresh request."""

    refresh_token: str
