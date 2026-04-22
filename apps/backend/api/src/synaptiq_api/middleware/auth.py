"""Firebase auth middleware — validates ID tokens on protected routes."""
import logging

from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request
from starlette.responses import JSONResponse, Response

from synaptiq_api.core.firebase import verify_firebase_token

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
]


class AuthMiddleware(BaseHTTPMiddleware):
    """Middleware to validate Firebase ID tokens on protected routes."""

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
            return JSONResponse(
                status_code=401,
                content={"detail": "Missing or invalid authorization header"},
            )

        token = auth_header[7:]  # Remove "Bearer " prefix

        try:
            # Verify Firebase token
            claims = verify_firebase_token(token)
            request.state.user = claims
            request.state.uid = claims.get("uid")
            request.state.email = claims.get("email")
            request.state.custom_claims = claims.get("custom_claims", {})

            # Extract role for dependency-based access control
            custom = claims.get("custom_claims") or {}
            request.state.user["role"] = custom.get("role")
            request.state.user["tenant_id"] = custom.get("tenant_id")

            logger.debug(f"Authenticated user: {request.state.uid}")

        except Exception as e:
            logger.warning(f"Token validation failed: {e}")
            return JSONResponse(
                status_code=401,
                content={"detail": "Invalid or expired token"},
            )

        return await call_next(request)
