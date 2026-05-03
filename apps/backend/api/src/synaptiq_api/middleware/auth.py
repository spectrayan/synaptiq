"""Auth middleware — validates tokens on protected routes.

Supports two auth providers (configurable via AUTH_PROVIDER env var):
  - "builtin": Verifies HS256 JWT issued by the built-in auth service
  - "firebase": Verifies Firebase ID tokens via Firebase Admin SDK
"""
import logging

from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request
from starlette.responses import JSONResponse, Response

from synaptiq_api.core.config import settings
from synaptiq_api.middleware.tenant import _cors_json

logger = logging.getLogger(__name__)

# Routes that don't require auth
PUBLIC_ROUTES = [
    "/health",
    "/docs",
    "/redoc",
    "/openapi.json",
    "/api/v1/auth/signup",
    "/api/v1/auth/login",
    "/api/v1/auth/refresh",
    "/api/v1/chat/",  # Chat is public for public-access tenants
    "/api/v1/config/branding/public",  # Public persona/branding config (no auth)
    "/api/v1/workflow/",  # Workflow endpoints — tenant-scoped via TenantMiddleware
]


def _verify_builtin_token(token: str) -> dict:
    """Decode a built-in JWT and return normalised claims."""
    from synaptiq_api.core.security import decode_access_token

    payload = decode_access_token(token)
    return {
        "uid": payload["sub"],
        "email": payload.get("email", ""),
        "role": payload.get("role"),
        "tenant_id": payload.get("tenant_id"),
        "must_change_password": payload.get("must_change_password", False),
        "custom_claims": {
            "role": payload.get("role"),
            "tenant_id": payload.get("tenant_id"),
        },
    }


def _verify_firebase_token(token: str) -> dict:
    """Verify a Firebase ID token and return normalised claims."""
    from synaptiq_api.core.firebase import verify_firebase_token

    decoded = verify_firebase_token(token)
    return decoded


class AuthMiddleware(BaseHTTPMiddleware):
    """Middleware to validate auth tokens on protected routes."""

    async def dispatch(self, request: Request, call_next) -> Response:
        # Let CORS preflight requests pass through
        if request.method == "OPTIONS":
            return await call_next(request)

        # Check if route is public
        if any(request.url.path.startswith(route) for route in PUBLIC_ROUTES):
            return await call_next(request)

        # Extract Authorization header
        auth_header = request.headers.get("Authorization", "")
        if not auth_header.startswith("Bearer "):
            return _cors_json(401, {"detail": "Missing or invalid authorization header"}, request)

        token = auth_header[7:]  # Remove "Bearer " prefix

        try:
            # Verify token using configured provider
            if settings.auth_provider == "builtin":
                claims = _verify_builtin_token(token)
            else:
                claims = _verify_firebase_token(token)

            request.state.user = claims
            request.state.uid = claims.get("uid")
            request.state.email = claims.get("email")
            request.state.custom_claims = claims.get("custom_claims", {})

            # Extract role for dependency-based access control
            custom = claims.get("custom_claims") or {}
            request.state.user["role"] = custom.get("role") or claims.get("role")
            request.state.user["tenant_id"] = custom.get("tenant_id") or claims.get("tenant_id")

            logger.debug("Authenticated user: %s (provider=%s)", request.state.uid, settings.auth_provider)

        except Exception as e:
            logger.warning("Token validation failed: %s", e)
            return _cors_json(401, {"detail": "Invalid or expired token"}, request)

        return await call_next(request)
