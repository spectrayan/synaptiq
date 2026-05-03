"""Redis async client — aioredis via redis-py."""
import logging
from typing import Any

import redis.asyncio as aioredis

from synaptiq_api.core.config import settings

logger = logging.getLogger(__name__)

_pool: aioredis.Redis | None = None


async def connect_redis() -> None:
    """Create the global Redis connection pool."""
    global _pool  # noqa: PLW0603
    _pool = aioredis.from_url(
        settings.redis_url,
        encoding="utf-8",
        decode_responses=True,
        max_connections=20,
    )
    # Test connectivity
    try:
        await _pool.ping()
        logger.info("Connected to Redis at %s", settings.redis_url)
    except Exception as e:
        logger.warning("Redis unavailable (%s) — session caching disabled", e)
        _pool = None  # Allow app to start without Redis


async def disconnect_redis() -> None:
    """Close the Redis connection pool."""
    global _pool  # noqa: PLW0603
    if _pool:
        await _pool.aclose()
        logger.info("Redis connection closed")


def get_redis() -> aioredis.Redis:
    """Get the Redis client instance. Raises if not connected."""
    if _pool is None:
        raise RuntimeError("Redis not initialised — call connect_redis() first")
    return _pool
