"""Synaptiq API — FastAPI application entry point."""
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from synaptiq_api.core.config import settings
from synaptiq_api.core.firebase import initialize_firebase
from synaptiq_api.core.mongodb import connect_db, disconnect_db
from synaptiq_api.core.redis import connect_redis, disconnect_redis
from synaptiq_api.middleware.auth import AuthMiddleware
from synaptiq_api.middleware.rate_limit import RateLimitMiddleware
from synaptiq_api.middleware.tenant import TenantMiddleware
from synaptiq_api.routers import actions, auth, catalog, chat, health, tenants


@asynccontextmanager
async def lifespan(app: FastAPI):  # noqa: ARG001
    await connect_db()
    await connect_redis()
    initialize_firebase()
    yield
    await disconnect_redis()
    await disconnect_db()


app = FastAPI(
    title="Synaptiq API",
    version="0.1.0",
    description="LLM-native catalog discovery engine",
    lifespan=lifespan,
    docs_url="/docs" if settings.debug else None,
    redoc_url="/redoc" if settings.debug else None,
)

# ---------------------------------------------------------------------------
# Middleware — execution order is BOTTOM → TOP (last added runs first).
# Request flow: CORS → Tenant → Auth → RateLimit → handler
# ---------------------------------------------------------------------------
app.add_middleware(RateLimitMiddleware)
app.add_middleware(AuthMiddleware)
app.add_middleware(TenantMiddleware)
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ---------------------------------------------------------------------------
# Routers
# ---------------------------------------------------------------------------
app.include_router(health.router)
app.include_router(auth.router, prefix="/api/v1", tags=["auth"])
app.include_router(chat.router, prefix="/api/v1/chat", tags=["chat"])
app.include_router(catalog.router, prefix="/api/v1/catalog", tags=["catalog"])
app.include_router(tenants.router, prefix="/api/v1/tenants", tags=["tenants"])
app.include_router(actions.router, prefix="/api/v1/actions", tags=["actions"])
