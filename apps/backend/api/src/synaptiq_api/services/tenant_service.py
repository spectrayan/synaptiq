"""Tenant service — tenant CRUD, provisioning, and cache management."""
import json
import logging
from datetime import datetime

from synaptiq_api.core.mongodb import get_db
from synaptiq_api.models.tenant import Tenant, TenantStatus

logger = logging.getLogger(__name__)


class TenantService:
    """Service for tenant management operations."""

    @staticmethod
    async def create_tenant(tenant: Tenant) -> dict:
        """
        Provision a new tenant with default configuration (REQ-T4).

        Inserts the tenant document and invalidates any Redis cache.
        """
        db = get_db()
        doc = tenant.model_dump(by_alias=True, exclude_none=True)
        doc["created_at"] = datetime.utcnow()
        doc["updated_at"] = datetime.utcnow()

        result = await db.tenants.insert_one(doc)
        logger.info("Provisioned tenant: %s (id=%s)", tenant.tenant_id, result.inserted_id)
        return {"tenant_id": tenant.tenant_id, "inserted_id": str(result.inserted_id)}

    @staticmethod
    async def get_tenant(tenant_id: str) -> dict | None:
        """Get tenant by tenant_id. Checks Redis cache first."""
        # Try Redis cache
        try:
            from synaptiq_api.core.redis import get_redis
            redis = get_redis()
            cached = await redis.get(f"tenant:{tenant_id}")
            if cached:
                return json.loads(cached)
        except Exception:
            pass

        db = get_db()
        doc = await db.tenants.find_one({"tenant_id": tenant_id})
        if not doc:
            return None

        # Serialize ObjectId
        doc["_id"] = str(doc["_id"])
        return doc

    @staticmethod
    async def update_tenant(tenant_id: str, updates: dict) -> bool:
        """Update tenant fields and invalidate cache."""
        db = get_db()
        updates["updated_at"] = datetime.utcnow()
        result = await db.tenants.update_one(
            {"tenant_id": tenant_id},
            {"$set": updates},
        )

        if result.modified_count > 0:
            await TenantService._invalidate_cache(tenant_id)
            return True
        return False

    @staticmethod
    async def list_tenants(
        status: TenantStatus | None = None,
        skip: int = 0,
        limit: int = 50,
    ) -> list[dict]:
        """List tenants with optional status filter."""
        db = get_db()
        query: dict = {}
        if status:
            query["status"] = status.value

        cursor = db.tenants.find(query).skip(skip).limit(limit)
        results = []
        async for doc in cursor:
            doc["_id"] = str(doc["_id"])
            results.append(doc)
        return results

    @staticmethod
    async def _invalidate_cache(tenant_id: str) -> None:
        """Remove tenant from Redis cache."""
        try:
            from synaptiq_api.core.redis import get_redis
            redis = get_redis()
            await redis.delete(f"tenant:{tenant_id}")
        except Exception:
            pass
