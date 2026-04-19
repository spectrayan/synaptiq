"""Synaptiq API — FastAPI application entry point."""
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from synaptiq_api.core.config import settings
from synaptiq_api.core.mongodb import connect_db, disconnect_db
from synaptiq_api.middleware.tenant import TenantMiddleware
from synaptiq_api.routers import catalog, chat, health, tenants


@asynccontextmanager
async def lifespan(app: FastAPI):  # noqa: ARG001
    await connect_db()
    yield
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
# CORS
# ---------------------------------------------------------------------------
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ---------------------------------------------------------------------------
# Custom middleware
# ---------------------------------------------------------------------------
app.add_middleware(TenantMiddleware)

# ---------------------------------------------------------------------------
# Routers
# ---------------------------------------------------------------------------
app.include_router(health.router)
app.include_router(chat.router, prefix="/api/v1/chat", tags=["chat"])
app.include_router(catalog.router, prefix="/api/v1/catalog", tags=["catalog"])
app.include_router(tenants.router, prefix="/api/v1/tenants", tags=["tenants"])
