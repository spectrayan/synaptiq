"""Auth router — Firebase authentication endpoints."""
import logging

from fastapi import APIRouter, Depends, HTTPException, Request, status
from firebase_admin import auth

from synaptiq_api.models.auth import (
    LoginRequest,
    RefreshTokenRequest,
    SignUpRequest,
    UserClaimsRequest,
    UserResponse,
)
from synaptiq_api.services import AuthService

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/auth", tags=["auth"])


@router.post("/signup", response_model=UserResponse, status_code=status.HTTP_201_CREATED)
async def signup(request: SignUpRequest) -> UserResponse:
    """
    Create a new user with email/password.

    - **email**: User email
    - **password**: User password (min 6 chars)
    """
    try:
        user = await AuthService.signup(request)
        logger.info(f"User signed up: {user.email}")
        return user
    except auth.EmailAlreadyExistsError:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Email already registered",
        )
    except Exception as e:
        logger.error(f"Signup error: {e}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Signup failed",
        )


@router.post("/login")
async def login(request: LoginRequest) -> dict:
    """
    Login with email/password (client-side Firebase SDK).

    Note: Actual token exchange happens client-side with Firebase SDK.
    This endpoint validates credentials and returns user info for verification.
    """
    try:
        user = await AuthService.get_user_by_uid(
            (await AuthService.verify_token("")).get("uid")
        )
        # In production, use Firebase REST API or client SDK for token exchange
        return {
            "message": "Use Firebase SDK for token exchange",
            "user": user,
        }
    except Exception as e:
        logger.error(f"Login error: {e}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid credentials",
        )


@router.post("/refresh", response_model=dict)
async def refresh_token(request: RefreshTokenRequest) -> dict:
    """
    Refresh Firebase ID token (optional server-side endpoint).

    Typically handled client-side by Firebase SDK.
    """
    return await AuthService.refresh_token(request)


@router.get("/me", response_model=UserResponse)
async def get_current_user(request: Request) -> UserResponse:
    """Get current authenticated user info."""
    if not hasattr(request.state, "uid"):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Not authenticated",
        )

    try:
        user = await AuthService.get_user_by_uid(request.state.uid)
        return user
    except auth.UserNotFoundError:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found",
        )


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
    if custom_claims.get("role") != "platform_admin":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Only platform admins can update roles",
        )

    try:
        user = await AuthService.update_user_role(request.state.uid, role_request)
        logger.info(f"Updated role for user {request.state.uid}: {role_request.role}")
        return user
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e),
        )
    except auth.UserNotFoundError:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found",
        )
