"""Rate limiting middleware — REQ-NF-RL1 through REQ-NF-RL4.

Applied after auth + tenant resolution. Checks both per-tenant and
per-session limits. Returns graceful in-chat messages (REQ-NF-RL3).
Logs events to the usage ledger (REQ-NF-RL4).
"""
import logging

from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request
from starlette.responses import JSONResponse, Response

from synaptiq_api.core.rate_limiter import RateLimiter

logger = logging.getLogger(__name__)

# Only rate-limit LLM-backed endpoints (not admin/config endpoints)
RATE_LIMITED_PREFIXES = (
    "/api/v1/chat",
)


class RateLimitMiddleware(BaseHTTPMiddleware):
    """Apply sliding-window rate limits on LLM-backed endpoints."""

    async def dispatch(self, request: Request, call_next) -> Response:
        # Only rate-limit specific paths
        if not any(request.url.path.startswith(p) for p in RATE_LIMITED_PREFIXES):
            return await call_next(request)

        # Need tenant_id from upstream middleware
        tenant_id = getattr(request.state, "tenant_id", None)
        if not tenant_id:
            return await call_next(request)

        # Rate limiting requires Redis — skip gracefully if unavailable
        try:
            # Get tenant-specific limit from cached tenant_info
            tenant_info = getattr(request.state, "tenant_info", {})
            limits = tenant_info.get("limits", {})
            tenant_limit = limits.get("max_requests_per_minute", 60)

            # 1. Check per-tenant rate limit (REQ-NF-RL1)
            tenant_result = await RateLimiter.check_tenant(tenant_id, limit=tenant_limit)
            if not tenant_result.allowed:
                await self._log_rate_limit_event(request, "tenant", tenant_result)
                return self._graceful_response(
                    message="We're handling a lot of requests right now. Please try again in a moment.",
                    retry_after=tenant_result.retry_after_seconds,
                )

            # 2. Check per-session rate limit (REQ-NF-RL2)
            session_id = self._extract_session_id(request)
            if session_id:
                session_result = await RateLimiter.check_session(session_id, limit=10)
                if not session_result.allowed:
                    await self._log_rate_limit_event(request, "session", session_result)
                    return self._graceful_response(
                        message="You're sending messages a bit quickly. Take a breath and try again shortly.",
                        retry_after=session_result.retry_after_seconds,
                    )

            # Attach rate limit headers for observability
            response = await call_next(request)
            response.headers["X-RateLimit-Remaining"] = str(tenant_result.remaining)
            response.headers["X-RateLimit-Limit"] = str(tenant_result.limit)
            return response

        except (RuntimeError, ConnectionError, OSError) as e:
            # Redis unavailable — skip rate limiting, let request through
            logger.debug("Rate limiting skipped (Redis unavailable): %s", e)
            return await call_next(request)

    @staticmethod
    def _graceful_response(message: str, retry_after: float) -> JSONResponse:
        """Return a graceful in-chat message, not HTTP 429 (REQ-NF-RL3)."""
        return JSONResponse(
            status_code=200,
            content={
                "type": "rate_limited",
                "content": message,
                "retry_after_seconds": retry_after,
                "ui_components": [
                    {
                        "type": "info-banner",
                        "props": {
                            "variant": "warning",
                            "message": message,
                        },
                    }
                ],
            },
        )

    @staticmethod
    def _extract_session_id(request: Request) -> str | None:
        """Extract session_id from request body or headers."""
        # Try header first (for SSE streaming where body may not be parsed)
        session_id = request.headers.get("X-Session-ID")
        if session_id:
            return session_id

        # Session ID is typically in the request body for chat endpoints,
        # but we can't read the body in middleware without consuming it.
        # Use the query param or header approach instead.
        return request.query_params.get("session_id")

    @staticmethod
    async def _log_rate_limit_event(
        request: Request,
        limit_type: str,
        result: object,
    ) -> None:
        """Log rate limit hit to usage ledger (REQ-NF-RL4)."""
        try:
            from synaptiq_api.core.mongodb import get_db

            db = get_db()
            from datetime import datetime

            await db.usage_ledger.insert_one({
                "event_type": "rate_limit_hit",
                "tenant_id": getattr(request.state, "tenant_id", ""),
                "session_id": request.headers.get("X-Session-ID", ""),
                "rate_limit_type": limit_type,
                "rate_limit_window_seconds": 60,
                "created_at": datetime.utcnow(),
                "metadata": {
                    "path": str(request.url.path),
                    "ip": request.client.host if request.client else "",
                },
            })
        except Exception as e:
            # Logging failure is non-fatal
            logger.error("Failed to log rate limit event: %s", e)
