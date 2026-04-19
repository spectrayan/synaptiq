"""Redis-backed rate limiter — REQ-NF-RL1 through REQ-NF-RL4.

Implements sliding-window rate limiting using Redis sorted sets:
  - Per-tenant: 60 req/min (configurable via tenant limits)
  - Per-session: 10 req/min

Rate limit hits are logged to the usage ledger (REQ-NF-RL4).
Breaches return graceful in-chat messages, not HTTP 429 (REQ-NF-RL3).
"""
import logging
import time

from synaptiq_api.core.redis import get_redis

logger = logging.getLogger(__name__)


class RateLimitResult:
    """Result of a rate limit check."""

    __slots__ = ("allowed", "remaining", "limit", "retry_after_seconds", "limit_type")

    def __init__(
        self,
        allowed: bool,
        remaining: int,
        limit: int,
        retry_after_seconds: float = 0.0,
        limit_type: str = "",
    ) -> None:
        self.allowed = allowed
        self.remaining = remaining
        self.limit = limit
        self.retry_after_seconds = retry_after_seconds
        self.limit_type = limit_type


class RateLimiter:
    """
    Sliding-window rate limiter backed by Redis sorted sets.

    Uses timestamp-scored members in a ZSET for accurate sliding windows.
    """

    WINDOW_SECONDS = 60  # 1-minute sliding window

    @staticmethod
    async def check(
        key: str,
        limit: int,
        window: int = WINDOW_SECONDS,
        limit_type: str = "tenant",
    ) -> RateLimitResult:
        """
        Check if a request is within the rate limit.

        Args:
            key: Redis key (e.g. "rl:tenant:<tenant_id>" or "rl:session:<session_id>")
            limit: Maximum number of requests in the window
            window: Window size in seconds
            limit_type: "tenant" or "session" for logging

        Returns:
            RateLimitResult with allowed status and metadata
        """
        redis = get_redis()
        now = time.time()
        window_start = now - window

        pipe = redis.pipeline(transaction=True)

        # Remove expired entries outside the window
        pipe.zremrangebyscore(key, 0, window_start)

        # Count current entries in window
        pipe.zcard(key)

        # Add the new request (optimistic — removed if over limit)
        request_id = f"{now}"
        pipe.zadd(key, {request_id: now})

        # Set TTL on the key (cleanup after 2× window)
        pipe.expire(key, window * 2)

        results = await pipe.execute()
        current_count = results[1]  # zcard result before adding

        if current_count >= limit:
            # Over limit — remove the optimistic add
            await redis.zrem(key, request_id)

            # Calculate when the oldest entry in the window expires
            oldest = await redis.zrange(key, 0, 0, withscores=True)
            retry_after = 0.0
            if oldest:
                retry_after = max(0.0, oldest[0][1] + window - now)

            logger.warning(
                "Rate limit exceeded: key=%s type=%s current=%d limit=%d",
                key, limit_type, current_count, limit,
            )

            return RateLimitResult(
                allowed=False,
                remaining=0,
                limit=limit,
                retry_after_seconds=round(retry_after, 1),
                limit_type=limit_type,
            )

        remaining = max(0, limit - current_count - 1)
        return RateLimitResult(
            allowed=True,
            remaining=remaining,
            limit=limit,
            limit_type=limit_type,
        )

    @staticmethod
    async def check_tenant(tenant_id: str, limit: int = 60) -> RateLimitResult:
        """Check per-tenant rate limit (REQ-NF-RL1)."""
        return await RateLimiter.check(
            key=f"rl:tenant:{tenant_id}",
            limit=limit,
            limit_type="tenant",
        )

    @staticmethod
    async def check_session(session_id: str, limit: int = 10) -> RateLimitResult:
        """Check per-session rate limit (REQ-NF-RL2)."""
        return await RateLimiter.check(
            key=f"rl:session:{session_id}",
            limit=limit,
            limit_type="session",
        )
