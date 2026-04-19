"""Health check router."""
from fastapi import APIRouter

router = APIRouter(tags=["health"])


@router.get("/health", summary="Liveness probe")
async def health() -> dict:
    return {"status": "ok"}


@router.get("/health/ready", summary="Readiness probe")
async def ready() -> dict:
    # TODO: add DB ping when needed
    return {"status": "ready"}
