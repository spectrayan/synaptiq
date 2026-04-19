"""Auth service — Firebase authentication operations."""
import logging

import firebase_admin.auth as auth

from synaptiq_api.core.firebase import (
    create_user,
    get_user_by_email,
    set_custom_claims,
    verify_firebase_token,
)
from synaptiq_api.models.auth import (
    LoginRequest,
    RefreshTokenRequest,
    SignUpRequest,
    UserClaimsRequest,
    UserResponse,
)

logger = logging.getLogger(__name__)


class AuthService:
    """Service for Firebase authentication operations."""

    @staticmethod
    async def signup(request: SignUpRequest) -> UserResponse:
        """
        Create a new user with email/password.

        Args:
            request: SignUpRequest with email and password

        Returns:
            UserResponse with new user details

        Raises:
            auth.EmailAlreadyExistsError: Email already registered
        """
        user = await create_user(request.email, request.password)
        return UserResponse(
            uid=user.uid,
            email=user.email,
            email_verified=user.email_verified,
            display_name=user.display_name,
        )

    @staticmethod
    async def verify_token(token: str) -> dict:
        """
        Verify a Firebase ID token.

        Args:
            token: Firebase ID token

        Returns:
            Decoded token claims

        Raises:
            auth.InvalidIdTokenError: Token is invalid
        """
        return verify_firebase_token(token)

    @staticmethod
    async def get_user_by_uid(uid: str) -> UserResponse:
        """
        Get user by UID.

        Args:
            uid: Firebase user ID

        Returns:
            UserResponse with user details

        Raises:
            auth.UserNotFoundError: User not found
        """
        user = auth.get_user(uid)
        return UserResponse(
            uid=user.uid,
            email=user.email,
            email_verified=user.email_verified,
            display_name=user.display_name,
            custom_claims=user.custom_claims,
        )

    @staticmethod
    async def update_user_role(uid: str, request: UserClaimsRequest) -> UserResponse:
        """
        Update user custom claims with role.

        Args:
            uid: Firebase user ID
            request: UserClaimsRequest with role

        Returns:
            Updated UserResponse

        Raises:
            auth.UserNotFoundError: User not found
        """
        # Validate role
        valid_roles = {
            "platform_admin",
            "tenant_admin",
            "tenant_editor",
            "tenant_viewer",
        }
        if request.role not in valid_roles:
            raise ValueError(f"Invalid role: {request.role}")

        await set_custom_claims(uid, {"role": request.role})
        return await AuthService.get_user_by_uid(uid)

    @staticmethod
    async def refresh_token(request: RefreshTokenRequest) -> dict:
        """
        Refresh a Firebase ID token using refresh token.

        Note: Client-side Firebase SDK handles token refresh.
        This endpoint is optional but provided for server-side refresh flows.

        Args:
            request: RefreshTokenRequest with refresh_token

        Returns:
            New token response with id_token, refresh_token, expires_in
        """
        # Firebase token refresh is typically handled client-side
        # This is a placeholder for REST API endpoints that need server-side refresh
        logger.warning("Server-side token refresh requested (typically client-side)")
        return {
            "message": "Use Firebase SDK client-side for token refresh",
        }
