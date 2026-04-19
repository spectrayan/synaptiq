"""Catalog schema service — CRUD, import, and cache lifecycle (T3.1–T3.7).

Handles:
  - Schema import (parse + normalize + persist)
  - Active schema retrieval (per-tenant)
  - Field annotation updates (toggle searchable/filterable, change designators, etc.)
  - LLM prompt cache invalidation on schema changes
"""
import logging
from datetime import datetime

from synaptiq_api.core.mongodb import get_db
from synaptiq_api.models.catalog import (
    CatalogSchema,
    FieldDesignator,
    SchemaField,
)
from synaptiq_api.services.schema_parser import parse_schema_input

logger = logging.getLogger(__name__)

COLLECTION = "catalog_schemas"


class SchemaService:
    """Service for catalog schema operations."""

    # ------------------------------------------------------------------
    # Import (T3.2)
    # ------------------------------------------------------------------

    @staticmethod
    async def import_schema(
        tenant_id: str,
        raw_content: str,
        content_type: str = "auto",
        schema_name: str | None = None,
    ) -> dict:
        """
        Parse an uploaded schema spec, normalize it, and persist as the
        tenant's active schema.

        If the tenant already has an active schema, it is deactivated
        (soft versioning) and the new one becomes active.
        """
        # Parse and validate
        fields = parse_schema_input(raw_content, content_type)

        # Build the schema model (triggers Pydantic validators: REQ-S4–S7)
        schema = CatalogSchema(
            tenant_id=tenant_id,
            name=schema_name or "Imported Schema",
            fields=fields,
            version=1,
            is_active=True,
        )

        db = get_db()

        # Determine next version number
        existing = await db[COLLECTION].find_one(
            {"tenant_id": tenant_id, "is_active": True},
            sort=[("version", -1)],
        )
        if existing:
            schema.version = existing.get("version", 0) + 1
            # Deactivate the old schema
            await db[COLLECTION].update_one(
                {"_id": existing["_id"]},
                {"$set": {"is_active": False, "updated_at": datetime.utcnow()}},
            )

        doc = schema.model_dump(by_alias=True, exclude_none=True)
        doc["created_at"] = datetime.utcnow()
        doc["updated_at"] = datetime.utcnow()

        result = await db[COLLECTION].insert_one(doc)
        logger.info(
            "Imported schema for tenant %s: v%d (%d fields, id=%s)",
            tenant_id, schema.version, len(fields), result.inserted_id,
        )

        # Invalidate caches (T3.7)
        await SchemaService._invalidate_caches(tenant_id)

        return {
            "schema_id": str(result.inserted_id),
            "tenant_id": tenant_id,
            "version": schema.version,
            "field_count": len(fields),
            "fields": [f.model_dump() for f in fields],
        }

    # ------------------------------------------------------------------
    # Read (T3.5)
    # ------------------------------------------------------------------

    @staticmethod
    async def get_active_schema(
        tenant_id: str,
        include_admin_fields: bool = True,
    ) -> dict | None:
        """
        Retrieve the active schema for a tenant.

        If include_admin_fields is False, fields with visibility=admin_only
        are stripped from the response (T3.7 / REQ-S11, REQ-S12).
        """
        db = get_db()
        doc = await db[COLLECTION].find_one(
            {"tenant_id": tenant_id, "is_active": True},
        )

        if not doc:
            return None

        doc["_id"] = str(doc["_id"])

        # Strip admin-only fields for end-user responses
        if not include_admin_fields:
            doc["fields"] = [
                f for f in doc.get("fields", [])
                if f.get("visibility") != "admin_only"
            ]

        return doc

    # ------------------------------------------------------------------
    # Update / Annotate (T3.6)
    # ------------------------------------------------------------------

    @staticmethod
    async def update_schema(tenant_id: str, updates: dict) -> dict | None:
        """
        Update field annotations on the active schema.

        Accepts a dict with:
          - name: str (optional, rename schema)
          - fields: list[dict] (optional, full field list replacement)
          - field_updates: dict[field_id, dict] (optional, partial updates per field)

        Returns the updated schema or None if not found.
        """
        db = get_db()
        existing = await db[COLLECTION].find_one(
            {"tenant_id": tenant_id, "is_active": True},
        )
        if not existing:
            return None

        set_ops: dict = {"updated_at": datetime.utcnow()}

        # Full schema rename
        if "name" in updates:
            set_ops["name"] = updates["name"]

        # Full field list replacement (re-validates via Pydantic)
        if "fields" in updates:
            validated_fields = [SchemaField(**f) for f in updates["fields"]]
            # Re-run the CatalogSchema validator
            CatalogSchema(
                tenant_id=tenant_id,
                fields=validated_fields,
                name=existing.get("name", ""),
            )
            set_ops["fields"] = [f.model_dump() for f in validated_fields]

        # Partial per-field updates (toggle searchable, change label, etc.)
        if "field_updates" in updates:
            fields = existing.get("fields", [])
            field_map = {f["field_id"]: f for f in fields}

            for field_id, field_patch in updates["field_updates"].items():
                if field_id not in field_map:
                    continue
                # Only allow safe annotation keys
                allowed_keys = {
                    "label", "searchable", "displayable", "filterable",
                    "sortable", "visibility", "designator", "display_order",
                    "required", "deprecated",
                }
                for key, value in field_patch.items():
                    if key in allowed_keys:
                        field_map[field_id][key] = value

            updated_fields = list(field_map.values())
            # Validate designator constraints
            validated = [SchemaField(**f) for f in updated_fields]
            CatalogSchema(
                tenant_id=tenant_id,
                fields=validated,
                name=existing.get("name", ""),
            )
            set_ops["fields"] = [f.model_dump() for f in validated]

        await db[COLLECTION].update_one(
            {"_id": existing["_id"]},
            {"$set": set_ops},
        )

        # Invalidate caches (T3.7)
        await SchemaService._invalidate_caches(tenant_id)

        # Return updated doc
        return await SchemaService.get_active_schema(tenant_id)

    # ------------------------------------------------------------------
    # Schema history
    # ------------------------------------------------------------------

    @staticmethod
    async def list_schema_versions(tenant_id: str) -> list[dict]:
        """List all schema versions for a tenant (latest first)."""
        db = get_db()
        cursor = db[COLLECTION].find(
            {"tenant_id": tenant_id},
            {"fields": 0},  # Exclude full field list for summary
        ).sort("version", -1)

        results = []
        async for doc in cursor:
            doc["_id"] = str(doc["_id"])
            results.append(doc)
        return results

    # ------------------------------------------------------------------
    # Cache invalidation (T3.7)
    # ------------------------------------------------------------------

    @staticmethod
    async def _invalidate_caches(tenant_id: str) -> None:
        """
        Invalidate tenant-scoped caches when schema changes:
          - LLM system prompt cache (REQ-S10)
          - Tenant config cache
        """
        try:
            from synaptiq_api.core.redis import get_redis
            redis = get_redis()
            await redis.delete(
                f"prompt:{tenant_id}",
                f"tenant:{tenant_id}",
                f"schema:{tenant_id}",
            )
            logger.info("Invalidated caches for tenant: %s", tenant_id)
        except Exception:
            logger.warning("Cache invalidation failed for tenant: %s", tenant_id)
