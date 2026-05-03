"""Auth router — authentication endpoints.

Supports two providers (configurable via AUTH_PROVIDER):
  - "builtin": MongoDB users collection + JWT tokens
  - "firebase": Firebase Admin SDK (existing behaviour)
"""
import logging

from fastapi import APIRouter, HTTPException, Request, status

from synaptiq_api.core.config import settings
from synaptiq_api.models.auth import (
    LoginRequest,
    RefreshTokenRequest,
    SignUpRequest,
    UserClaimsRequest,
    UserResponse,
)

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/auth", tags=["auth"])


# ── Signup ────────────────────────────────────────────────────────────────────


@router.post("/signup", response_model=UserResponse, status_code=status.HTTP_201_CREATED)
async def signup(request: SignUpRequest) -> UserResponse:
    """
    Create a new user with email/password.

    - **email**: User email
    - **password**: User password (min 6 chars)
    """
    try:
        if settings.auth_provider == "builtin":
            from synaptiq_api.services.builtin_auth_service import BuiltinAuthService
            user = await BuiltinAuthService.signup(request)
        else:
            from synaptiq_api.services import AuthService
            user = await AuthService.signup(request)

        logger.info("User signed up: %s", request.email)
        return user
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail=str(e),
        )
    except Exception as e:
        logger.error("Signup error: %s", e)
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Signup failed",
        )


# ── Login ─────────────────────────────────────────────────────────────────────


@router.post("/login")
async def login(request: LoginRequest) -> dict:
    """
    Login with email/password.

    - builtin: Returns JWT token directly.
    - firebase: Returns guidance to use Firebase SDK client-side.
    """
    if settings.auth_provider == "builtin":
        from synaptiq_api.services.builtin_auth_service import BuiltinAuthService

        try:
            result = await BuiltinAuthService.login(request)
            return result.model_dump()
        except ValueError as e:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail=str(e),
            )
    else:
        # Firebase: token exchange happens client-side
        return {
            "message": "Use Firebase SDK for token exchange",
            "provider": "firebase",
        }


# ── Refresh ───────────────────────────────────────────────────────────────────


@router.post("/refresh", response_model=dict)
async def refresh_token(request: RefreshTokenRequest) -> dict:
    """
    Refresh an auth token.

    Builtin: Re-issues a JWT from refresh token (simplified).
    Firebase: Handled client-side.
    """
    if settings.auth_provider == "builtin":
        # Simplified: client should re-login. Full refresh-token rotation is deferred.
        return {"message": "Please re-authenticate to get a new token"}
    else:
        from synaptiq_api.services import AuthService
        return await AuthService.refresh_token(request)


# ── Current User ──────────────────────────────────────────────────────────────


@router.get("/me", response_model=UserResponse)
async def get_current_user(request: Request) -> UserResponse:
    """Get current authenticated user info."""
    if not hasattr(request.state, "uid"):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Not authenticated",
        )

    try:
        if settings.auth_provider == "builtin":
            from synaptiq_api.services.builtin_auth_service import BuiltinAuthService
            return await BuiltinAuthService.get_user_by_uid(request.state.uid)
        else:
            from synaptiq_api.services import AuthService
            return await AuthService.get_user_by_uid(request.state.uid)
    except (ValueError, Exception) as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found",
        )


# ── Update Role ───────────────────────────────────────────────────────────────


@router.patch("/me/role", response_model=UserResponse)
async def update_user_role(
    request: Request, role_request: UserClaimsRequest
) -> UserResponse:
    """
    Update user custom claims (admin only).

    Requires platform_admin role in custom claims.
    """
    if not hasattr(request.state, "uid"):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Not authenticated",
        )

    # Check if user is platform admin
    custom_claims = request.state.custom_claims or {}
    user_role = custom_claims.get("role") or getattr(request.state, "user", {}).get("role")
    if user_role != "platform_admin":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Only platform admins can update roles",
        )

    try:
        if settings.auth_provider == "builtin":
            from synaptiq_api.services.builtin_auth_service import BuiltinAuthService
            user = await BuiltinAuthService.update_user_role(request.state.uid, role_request.role)
        else:
            from synaptiq_api.services import AuthService
            user = await AuthService.update_user_role(request.state.uid, role_request)

        logger.info("Updated role for user %s: %s", request.state.uid, role_request.role)
        return user
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e),
        )


# ── Change Password (builtin only) ───────────────────────────────────────────


@router.post("/change-password")
async def change_password(request: Request) -> dict:
    """
    Change the current user's password (builtin auth only).

    Expects JSON body: { "current_password": "...", "new_password": "..." }
    """
    if settings.auth_provider != "builtin":
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Password change is only available with built-in auth provider",
        )

    if not hasattr(request.state, "uid"):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Not authenticated",
        )

    from synaptiq_api.models.user import ChangePasswordRequest
    from synaptiq_api.services.builtin_auth_service import BuiltinAuthService

    body = await request.json()
    try:
        change_req = ChangePasswordRequest(**body)
        result = await BuiltinAuthService.change_password(request.state.uid, change_req)
        return result
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e),
        )
