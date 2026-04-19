"""Catalog item service — CRUD, validation, import, and bulk ops (Phase 4).

Handles:
  - Item creation with schema validation (T4.1)
  - Paginated listing with status filter (T4.2)
  - Get / update single item (T4.3)
  - Soft-delete (archive) (T4.4)
  - Bulk status transitions (T4.5)
  - Tenant-scoped item limit enforcement (T4.6)
  - CSV import with row-level validation (T4.7)
"""
import csv
import io
import logging
from datetime import datetime
from typing import Any

from bson import ObjectId

from synaptiq_api.core.mongodb import get_db
from synaptiq_api.models.catalog import (
    CatalogItem,
    FieldDesignator,
    FieldType,
    ItemStatus,
    SchemaField,
)
from synaptiq_api.services.schema_service import SchemaService

logger = logging.getLogger(__name__)

ITEMS_COLLECTION = "catalog_items"
SCHEMAS_COLLECTION = "catalog_schemas"


# ---------------------------------------------------------------------------
# Schema-driven validation (REQ-D8)
# ---------------------------------------------------------------------------

def _coerce_value(raw: Any, field: SchemaField) -> Any:
    """Attempt to coerce a raw value to the expected field type."""
    if raw is None or raw == "":
        return None

    try:
        match field.type:
            case FieldType.number | FieldType.currency:
                return float(raw) if raw is not None else None
            case FieldType.boolean:
                if isinstance(raw, bool):
                    return raw
                return str(raw).lower() in ("true", "1", "yes")
            case FieldType.enum:
                val = str(raw)
                if field.enum_values and val not in field.enum_values:
                    raise ValueError(f"'{val}' not in allowed values: {field.enum_values}")
                return val
            case FieldType.multi_enum:
                if isinstance(raw, list):
                    vals = [str(v) for v in raw]
                else:
                    vals = [s.strip() for s in str(raw).split(",") if s.strip()]
                if field.enum_values:
                    invalid = [v for v in vals if v not in field.enum_values]
                    if invalid:
                        raise ValueError(f"Invalid values: {invalid}")
                return vals
            case _:
                return str(raw)
    except (TypeError, ValueError) as e:
        raise ValueError(f"Field '{field.field_id}': {e}") from e


def validate_item_data(
    data: dict[str, Any],
    fields: list[SchemaField],
) -> tuple[dict[str, Any], list[str]]:
    """
    Validate and coerce item data against the schema fields.

    Returns:
        (clean_data, errors) — clean_data has coerced values;
        errors is a list of validation messages (empty = valid).
    """
    field_map = {f.field_id: f for f in fields if not f.deprecated}
    clean: dict[str, Any] = {}
    errors: list[str] = []

    # Check required fields
    for field in fields:
        if field.required and not field.deprecated:
            if field.field_id not in data or data[field.field_id] in (None, ""):
                errors.append(f"Missing required field: '{field.field_id}'")

    # Validate each provided value
    for key, value in data.items():
        field = field_map.get(key)
        if not field:
            # Silently skip unknown fields
            continue
        try:
            clean[key] = _coerce_value(value, field)
        except ValueError as e:
            errors.append(str(e))

    return clean, errors


class CatalogService:
    """Service for catalog item operations."""

    # ------------------------------------------------------------------
    # Create (T4.1)
    # ------------------------------------------------------------------

    @staticmethod
    async def create_item(
        tenant_id: str,
        data: dict[str, Any],
        status: ItemStatus = ItemStatus.draft,
    ) -> dict:
        """
        Create a catalog item validated against the tenant's active schema.

        Enforces:
        - Schema validation (REQ-D8)
        - Tenant item limit (REQ-D10 / T4.6)
        """
        db = get_db()

        # Fetch active schema
        schema = await SchemaService.get_active_schema(tenant_id)
        if not schema:
            raise ValueError("No active schema. Import one before adding items.")

        fields = [SchemaField(**f) for f in schema["fields"]]

        # Validate data against schema
        clean_data, errors = validate_item_data(data, fields)
        if errors:
            raise ValueError(f"Validation failed: {'; '.join(errors)}")

        # Enforce item limit (T4.6)
        await CatalogService._enforce_item_limit(tenant_id)

        # Build and insert document
        doc = {
            "tenant_id": tenant_id,
            "status": status.value,
            "data": clean_data,
            "embedding": None,
            "embedding_model": "",
            "embedded_at": None,
            "created_at": datetime.utcnow(),
            "updated_at": datetime.utcnow(),
        }
        result = await db[ITEMS_COLLECTION].insert_one(doc)
        doc["_id"] = str(result.inserted_id)

        logger.info("Created catalog item %s for tenant %s", doc["_id"], tenant_id)
        return doc

    # ------------------------------------------------------------------
    # List (T4.2)
    # ------------------------------------------------------------------

    @staticmethod
    async def list_items(
        tenant_id: str,
        status_filter: ItemStatus | None = None,
        search: str | None = None,
        skip: int = 0,
        limit: int = 20,
    ) -> dict:
        """
        Paginated item listing with optional status filter.

        Returns {items, total, skip, limit}.
        """
        db = get_db()
        query: dict[str, Any] = {"tenant_id": tenant_id}

        if status_filter:
            query["status"] = status_filter.value

        if search:
            query["$text"] = {"$search": search}

        total = await db[ITEMS_COLLECTION].count_documents(query)

        cursor = db[ITEMS_COLLECTION].find(query).sort(
            "updated_at", -1
        ).skip(skip).limit(limit)

        items = []
        async for doc in cursor:
            doc["_id"] = str(doc["_id"])
            items.append(doc)

        return {"items": items, "total": total, "skip": skip, "limit": limit}

    # ------------------------------------------------------------------
    # Get / Update (T4.3)
    # ------------------------------------------------------------------

    @staticmethod
    async def get_item(tenant_id: str, item_id: str) -> dict | None:
        """Get a single item by ID (tenant-scoped)."""
        db = get_db()
        doc = await db[ITEMS_COLLECTION].find_one({
            "_id": ObjectId(item_id),
            "tenant_id": tenant_id,
        })
        if doc:
            doc["_id"] = str(doc["_id"])
        return doc

    @staticmethod
    async def update_item(
        tenant_id: str,
        item_id: str,
        data_updates: dict[str, Any],
    ) -> dict | None:
        """
        Update item data fields. Validates changed fields against schema.
        """
        db = get_db()

        existing = await db[ITEMS_COLLECTION].find_one({
            "_id": ObjectId(item_id),
            "tenant_id": tenant_id,
        })
        if not existing:
            return None

        if existing.get("status") == ItemStatus.archived.value:
            raise ValueError("Cannot update an archived item. Restore it first.")

        # Fetch schema for validation
        schema = await SchemaService.get_active_schema(tenant_id)
        if schema:
            fields = [SchemaField(**f) for f in schema["fields"]]
            # Merge existing data with updates for full validation
            merged = {**existing.get("data", {}), **data_updates}
            clean_data, errors = validate_item_data(merged, fields)
            if errors:
                raise ValueError(f"Validation failed: {'; '.join(errors)}")
            data_updates = clean_data

        await db[ITEMS_COLLECTION].update_one(
            {"_id": ObjectId(item_id)},
            {
                "$set": {
                    "data": {**existing.get("data", {}), **data_updates},
                    "updated_at": datetime.utcnow(),
                    # Clear embedding — data changed, needs re-embedding
                    "embedding": None,
                    "embedded_at": None,
                },
            },
        )

        return await CatalogService.get_item(tenant_id, item_id)

    # ------------------------------------------------------------------
    # Soft-delete / Archive (T4.4)
    # ------------------------------------------------------------------

    @staticmethod
    async def delete_item(tenant_id: str, item_id: str) -> bool:
        """Soft-delete (archive) an item."""
        db = get_db()
        result = await db[ITEMS_COLLECTION].update_one(
            {"_id": ObjectId(item_id), "tenant_id": tenant_id},
            {
                "$set": {
                    "status": ItemStatus.archived.value,
                    "updated_at": datetime.utcnow(),
                },
            },
        )
        return result.modified_count > 0

    # ------------------------------------------------------------------
    # Bulk ops (T4.5)
    # ------------------------------------------------------------------

    @staticmethod
    async def bulk_update_status(
        tenant_id: str,
        item_ids: list[str],
        target_status: ItemStatus,
    ) -> int:
        """
        Bulk-update status for a list of items (REQ-D7).

        Returns the number of modified documents.
        """
        db = get_db()
        oids = [ObjectId(id_) for id_ in item_ids]

        result = await db[ITEMS_COLLECTION].update_many(
            {"_id": {"$in": oids}, "tenant_id": tenant_id},
            {
                "$set": {
                    "status": target_status.value,
                    "updated_at": datetime.utcnow(),
                },
            },
        )
        logger.info(
            "Bulk status update for tenant %s: %d → %s",
            tenant_id, result.modified_count, target_status.value,
        )
        return result.modified_count

    # ------------------------------------------------------------------
    # CSV Import (T4.7)
    # ------------------------------------------------------------------

    @staticmethod
    async def import_csv(
        tenant_id: str,
        csv_content: str,
        field_mapping: dict[str, str] | None = None,
    ) -> dict:
        """
        Import items from CSV. Each row becomes a catalog item.

        Args:
            tenant_id: Tenant scope.
            csv_content: Raw CSV string.
            field_mapping: Optional {csv_header: field_id} mapping.
                If None, CSV headers are slugified and matched to schema fields.

        Returns:
            {imported: int, failed: int, errors: list[{row, messages}]}
        """
        db = get_db()

        # Fetch schema
        schema = await SchemaService.get_active_schema(tenant_id)
        if not schema:
            raise ValueError("No active schema. Import one before uploading items.")

        fields = [SchemaField(**f) for f in schema["fields"]]
        field_ids = {f.field_id for f in fields}

        # Enforce item limit check
        await CatalogService._enforce_item_limit(tenant_id)

        reader = csv.DictReader(io.StringIO(csv_content))
        if not reader.fieldnames:
            raise ValueError("CSV file has no headers.")

        # Build mapping: csv_header → field_id
        if field_mapping is None:
            # Auto-map: slugify CSV headers
            from synaptiq_api.services.schema_parser import _slugify
            field_mapping = {}
            for header in reader.fieldnames:
                slug = _slugify(header)
                if slug in field_ids:
                    field_mapping[header] = slug

        if not field_mapping:
            raise ValueError("No CSV columns could be mapped to schema fields.")

        imported = 0
        failed = 0
        row_errors: list[dict] = []
        docs_to_insert: list[dict] = []

        for row_num, row in enumerate(reader, start=2):  # row 1 = headers
            data = {}
            for csv_col, field_id in field_mapping.items():
                if csv_col in row:
                    data[field_id] = row[csv_col]

            clean_data, errors = validate_item_data(data, fields)
            if errors:
                failed += 1
                row_errors.append({"row": row_num, "messages": errors})
                continue

            docs_to_insert.append({
                "tenant_id": tenant_id,
                "status": ItemStatus.draft.value,
                "data": clean_data,
                "embedding": None,
                "embedding_model": "",
                "embedded_at": None,
                "created_at": datetime.utcnow(),
                "updated_at": datetime.utcnow(),
            })

        # Batch insert valid rows
        if docs_to_insert:
            result = await db[ITEMS_COLLECTION].insert_many(docs_to_insert)
            imported = len(result.inserted_ids)

        logger.info(
            "CSV import for tenant %s: %d imported, %d failed",
            tenant_id, imported, failed,
        )

        return {
            "imported": imported,
            "failed": failed,
            "total_rows": imported + failed,
            "errors": row_errors[:50],  # Cap error list
            "field_mapping": field_mapping,
        }

    # ------------------------------------------------------------------
    # Item limit enforcement (T4.6)
    # ------------------------------------------------------------------

    @staticmethod
    async def _enforce_item_limit(tenant_id: str) -> None:
        """
        Check tenant hasn't exceeded their catalog item limit (REQ-D10).
        Fetches the limit from tenant config.
        """
        db = get_db()

        # Get tenant limit
        tenant = await db["tenants"].find_one(
            {"tenant_id": tenant_id},
            {"config.max_catalog_items": 1},
        )
        max_items = 1_000  # default
        if tenant and tenant.get("config"):
            max_items = tenant["config"].get("max_catalog_items", 1_000)

        # Count active + draft items (archived don't count)
        count = await db[ITEMS_COLLECTION].count_documents({
            "tenant_id": tenant_id,
            "status": {"$ne": ItemStatus.archived.value},
        })

        if count >= max_items:
            raise ValueError(
                f"Catalog item limit reached ({max_items}). "
                "Archive or delete items, or upgrade your plan."
            )
