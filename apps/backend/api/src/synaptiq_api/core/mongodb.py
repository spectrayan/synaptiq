"""MongoDB async client — Motor."""
import logging

from motor.motor_asyncio import AsyncIOMotorClient, AsyncIOMotorDatabase

from synaptiq_api.core.config import settings

logger = logging.getLogger(__name__)

_client: AsyncIOMotorClient | None = None


async def connect_db() -> None:
    global _client  # noqa: PLW0603
    _client = AsyncIOMotorClient(settings.mongodb_uri)
    logger.info("Connected to MongoDB at %s", settings.mongodb_uri)


async def disconnect_db() -> None:
    global _client  # noqa: PLW0603
    if _client:
        _client.close()
        logger.info("MongoDB connection closed")


def get_db() -> AsyncIOMotorDatabase:
    if _client is None:
        raise RuntimeError("Database not initialised — call connect_db() first")
    return _client[settings.mongodb_db_name]
