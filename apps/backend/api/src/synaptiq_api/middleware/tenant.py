"""Tenant resolution middleware — REQ-T2, REQ-NF-CACHE1.

Resolves tenant_id from:
  1. X-Tenant-ID header (service-to-service / local dev)
  2. Subdomain extraction from Host header

Resolved tenants are cached in Redis to avoid MongoDB lookups on every request.
"""
import json
import logging

from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request
from starlette.responses import JSONResponse, Response

from synaptiq_api.core.config import settings

logger = logging.getLogger(__name__)

TENANT_HEADER = "X-Tenant-ID"

# Routes that bypass tenant resolution
TENANT_EXEMPT_ROUTES = (
    "/health",
    "/docs",
    "/redoc",
    "/openapi.json",
    "/api/v1/auth/signup",
    "/api/v1/auth/login",
    "/api/v1/auth/refresh",
)


class TenantMiddleware(BaseHTTPMiddleware):
    """Resolve tenant_id and attach to request.state with Redis-backed caching."""

    async def dispatch(self, request: Request, call_next) -> Response:
        # Let CORS preflight requests pass through without tenant resolution
        if request.method == "OPTIONS":
            return await call_next(request)

        # Exempt routes skip tenant resolution entirely
        if any(request.url.path.startswith(route) for route in TENANT_EXEMPT_ROUTES):
            return await call_next(request)

        # 1. Try explicit header (service-to-service / local dev)
        tenant_id = request.headers.get(TENANT_HEADER)

        # 2. Fall back to subdomain extraction
        if not tenant_id:
            host = request.headers.get("host", "")
            base = settings.base_domain
            if host and host.endswith(f".{base}"):
                tenant_id = host.removesuffix(f".{base}").split(":")[0]

        if not tenant_id:
            return JSONResponse(
                status_code=400,
                content={"detail": "Unable to resolve tenant from request"},
            )

        # 3. Validate tenant exists (Redis cache → MongoDB fallback)
        tenant_info = await self._resolve_tenant(tenant_id)
        if not tenant_info:
            return JSONResponse(
                status_code=404,
                content={"detail": f"Tenant '{tenant_id}' not found"},
            )

        if tenant_info.get("status") == "suspended":
            return JSONResponse(
                status_code=403,
                content={"detail": "Tenant is suspended"},
            )

        # Attach to request state
        request.state.tenant_id = tenant_id
        request.state.tenant_info = tenant_info
        logger.debug("Resolved tenant: %s (status=%s)", tenant_id, tenant_info.get("status"))
        return await call_next(request)

    async def _resolve_tenant(self, tenant_id: str) -> dict | None:
        """Look up tenant — Redis cache first, then MongoDB."""
        try:
            from synaptiq_api.core.redis import get_redis

            redis = get_redis()
            cache_key = f"tenant:{tenant_id}"
            cached = await redis.get(cache_key)
            if cached:
                return json.loads(cached)
        except Exception:
            # Redis unavailable — fall through to DB
            logger.debug("Redis unavailable for tenant cache, falling through to DB")

        try:
            from synaptiq_api.core.mongodb import get_db

            db = get_db()
            doc = await db.tenants.find_one(
                {"tenant_id": tenant_id},
                {"_id": 0, "tenant_id": 1, "name": 1, "status": 1, "limits": 1, "slug": 1},
            )
            if not doc:
                return None

            # Cache in Redis for 5 minutes
            try:
                from synaptiq_api.core.redis import get_redis

                redis = get_redis()
                await redis.setex(
                    f"tenant:{tenant_id}",
                    300,
                    json.dumps(doc, default=str),
                )
            except Exception:
                pass  # Cache miss is non-fatal

            return doc

        except Exception as e:
            logger.error("Failed to resolve tenant %s: %s", tenant_id, e)
            # In development, allow passthrough with minimal info
            if settings.environment == "development":
                return {"tenant_id": tenant_id, "status": "active", "name": tenant_id}
            return None
