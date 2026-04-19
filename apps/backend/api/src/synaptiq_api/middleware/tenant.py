"""Tenant resolution middleware — extracts tenantId from subdomain."""
import logging

from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request
from starlette.responses import JSONResponse, Response

from synaptiq_api.core.config import settings

logger = logging.getLogger(__name__)

TENANT_HEADER = "X-Tenant-ID"


class TenantMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next) -> Response:
        # 1. Try explicit header (service-to-service / local dev)
        tenant_id = request.headers.get(TENANT_HEADER)

        # 2. Fall back to subdomain extraction
        if not tenant_id:
            host = request.headers.get("host", "")
            base = settings.base_domain
            if host and host.endswith(f".{base}"):
                tenant_id = host.removesuffix(f".{base}").split(":")[0]

        # 3. Health endpoints are public
        if request.url.path.startswith("/health"):
            return await call_next(request)

        if not tenant_id:
            return JSONResponse(
                status_code=400,
                content={"detail": "Unable to resolve tenant from request"},
            )

        request.state.tenant_id = tenant_id
        logger.debug("Resolved tenant: %s", tenant_id)
        return await call_next(request)
