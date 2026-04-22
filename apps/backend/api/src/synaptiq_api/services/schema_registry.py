"""General-purpose schema registry — discovers and introspects tenant data (Phase 3).

Extends the existing catalog-specific SchemaService with generic capabilities:
  - Collection listing (what data does the tenant have?)
  - Schema auto-inference (sample documents to derive field types)
  - Schema registration (tenant defines via chat)
  - Field-level authorization (mark fields as hidden/admin-only)

This enables the AI to dynamically generate views for ANY data shape.
"""
import logging
from datetime import datetime
from typing import Any

from synaptiq_api.core.mongodb import get_db

logger = logging.getLogger(__name__)

REGISTRY_COLLECTION = "data_registry"


class CollectionInfo:
    """Metadata about a tenant's data collection."""

    def __init__(
        self,
        collection_name: str,
        display_name: str = "",
        description: str = "",
        document_count: int = 0,
        fields: list[dict] | None = None,
        created_at: datetime | None = None,
    ):
        self.collection_name = collection_name
        self.display_name = display_name or collection_name.replace("_", " ").title()
        self.description = description
        self.document_count = document_count
        self.fields = fields or []
        self.created_at = created_at or datetime.utcnow()

    def to_dict(self) -> dict:
        return {
            "collection_name": self.collection_name,
            "display_name": self.display_name,
            "description": self.description,
            "document_count": self.document_count,
            "fields": self.fields,
            "created_at": self.created_at.isoformat() if self.created_at else None,
        }


# Type mapping from Python/BSON types to schema field types
_TYPE_MAP = {
    "str": "text",
    "int": "number",
    "float": "number",
    "bool": "boolean",
    "datetime": "date",
    "list": "array",
    "dict": "object",
    "ObjectId": "text",
    "NoneType": "text",
}


class SchemaRegistry:
    """General-purpose schema introspection and registry service."""

    # ------------------------------------------------------------------
    # Collection discovery
    # ------------------------------------------------------------------

    @staticmethod
    async def get_collections(tenant_id: str) -> list[dict]:
        """
        List all data collections registered for a tenant.

        Returns metadata about each collection including field count
        and document count.
        """
        db = get_db()

        # Check the registry first
        cursor = db[REGISTRY_COLLECTION].find(
            {"tenant_id": tenant_id},
            {"_id": 0},
        )

        collections = []
        async for doc in cursor:
            collections.append(doc)

        # Always include the built-in catalog_items collection
        catalog_count = await db["catalog_items"].count_documents(
            {"tenant_id": tenant_id}
        )
        if catalog_count > 0 and not any(
            c["collection_name"] == "catalog_items" for c in collections
        ):
            collections.insert(0, {
                "collection_name": "catalog_items",
                "display_name": "Catalog Items",
                "description": "Product catalog (built-in)",
                "document_count": catalog_count,
                "is_builtin": True,
            })

        return collections

    # ------------------------------------------------------------------
    # Schema introspection
    # ------------------------------------------------------------------

    @staticmethod
    async def get_schema(
        tenant_id: str,
        collection_name: str,
    ) -> dict | None:
        """
        Get the registered schema for a specific collection.
        """
        db = get_db()
        doc = await db[REGISTRY_COLLECTION].find_one({
            "tenant_id": tenant_id,
            "collection_name": collection_name,
        })

        if doc:
            doc["_id"] = str(doc["_id"])
            return doc

        return None

    @staticmethod
    async def infer_schema(
        tenant_id: str,
        collection_name: str,
        sample_size: int = 50,
    ) -> dict:
        """
        Auto-infer schema by sampling documents from a collection.

        Analyzes up to `sample_size` documents to discover field names,
        types, and cardinality. This powers the "upload and auto-infer"
        onboarding flow.
        """
        db = get_db()
        collection = db[collection_name]

        # Sample documents for this tenant
        pipeline: list[dict[str, Any]] = [
            {"$match": {"tenant_id": tenant_id}},
            {"$sample": {"size": sample_size}},
        ]

        samples: list[dict] = []
        async for doc in collection.aggregate(pipeline):
            samples.append(doc)

        if not samples:
            return {
                "collection_name": collection_name,
                "tenant_id": tenant_id,
                "fields": [],
                "document_count": 0,
                "inferred": True,
            }

        # Analyze fields across all samples
        field_stats: dict[str, dict] = {}

        for doc in samples:
            SchemaRegistry._analyze_document(doc, field_stats, prefix="")

        # Convert to schema fields
        fields = []
        for field_path, stats in field_stats.items():
            # Skip internal fields
            if field_path.startswith("_") or field_path == "tenant_id":
                continue

            # Determine the most common type
            type_counts = stats.get("types", {})
            if type_counts:
                primary_type = max(type_counts, key=type_counts.get)
            else:
                primary_type = "text"

            # Determine if field is likely sortable/filterable
            unique_count = len(stats.get("unique_values", set()))
            total_count = stats.get("count", 0)
            cardinality_ratio = unique_count / total_count if total_count > 0 else 0

            fields.append({
                "field_id": field_path,
                "label": field_path.replace("_", " ").replace(".", " > ").title(),
                "type": primary_type,
                "nullable": stats.get("null_count", 0) > 0,
                "searchable": primary_type == "text",
                "filterable": cardinality_ratio < 0.5 and unique_count <= 50,
                "sortable": primary_type in ("number", "date", "text"),
                "sample_values": list(stats.get("unique_values", set()))[:5],
                "null_rate": round(stats.get("null_count", 0) / total_count, 2)
                if total_count > 0 else 0,
            })

        # Sort by field name
        fields.sort(key=lambda f: f["field_id"])

        doc_count = await collection.count_documents({"tenant_id": tenant_id})

        return {
            "collection_name": collection_name,
            "tenant_id": tenant_id,
            "fields": fields,
            "document_count": doc_count,
            "sample_size": len(samples),
            "inferred": True,
        }

    @staticmethod
    def _analyze_document(
        doc: dict,
        field_stats: dict[str, dict],
        prefix: str,
    ) -> None:
        """Recursively analyze document fields for type inference."""
        for key, value in doc.items():
            field_path = f"{prefix}{key}" if not prefix else f"{prefix}.{key}"

            if field_path not in field_stats:
                field_stats[field_path] = {
                    "types": {},
                    "count": 0,
                    "null_count": 0,
                    "unique_values": set(),
                }

            stats = field_stats[field_path]
            stats["count"] += 1

            if value is None:
                stats["null_count"] += 1
                return

            # Map Python type to schema type
            type_name = type(value).__name__
            schema_type = _TYPE_MAP.get(type_name, "text")
            stats["types"][schema_type] = stats["types"].get(schema_type, 0) + 1

            # Track unique values for cardinality estimation (cap at 100)
            if len(stats["unique_values"]) < 100:
                try:
                    stats["unique_values"].add(str(value)[:100])
                except (TypeError, ValueError):
                    pass

            # Recurse into nested objects
            if isinstance(value, dict) and len(value) < 20:
                SchemaRegistry._analyze_document(value, field_stats, field_path)

    # ------------------------------------------------------------------
    # Collection registration
    # ------------------------------------------------------------------

    @staticmethod
    async def register_collection(
        tenant_id: str,
        collection_name: str,
        display_name: str = "",
        description: str = "",
        fields: list[dict] | None = None,
        auto_infer: bool = True,
    ) -> dict:
        """
        Register a data collection for a tenant.

        If `auto_infer` is True and no fields are provided, the schema
        is automatically inferred from existing data.
        """
        db = get_db()

        # Auto-infer if needed
        if auto_infer and not fields:
            inferred = await SchemaRegistry.infer_schema(tenant_id, collection_name)
            fields = inferred.get("fields", [])

        doc = {
            "tenant_id": tenant_id,
            "collection_name": collection_name,
            "display_name": display_name or collection_name.replace("_", " ").title(),
            "description": description,
            "fields": fields or [],
            "created_at": datetime.utcnow(),
            "updated_at": datetime.utcnow(),
        }

        # Upsert
        result = await db[REGISTRY_COLLECTION].update_one(
            {"tenant_id": tenant_id, "collection_name": collection_name},
            {"$set": doc},
            upsert=True,
        )

        logger.info(
            "Registered collection '%s' for tenant %s (%d fields)",
            collection_name, tenant_id, len(fields or []),
        )

        # Count documents
        try:
            count = await db[collection_name].count_documents({"tenant_id": tenant_id})
            doc["document_count"] = count
        except Exception:
            doc["document_count"] = 0

        return doc

    # ------------------------------------------------------------------
    # Data query execution
    # ------------------------------------------------------------------

    @staticmethod
    async def query_data(
        tenant_id: str,
        collection_name: str,
        pipeline: list[dict] | None = None,
        filter_query: dict | None = None,
        sort: dict | None = None,
        limit: int = 100,
        skip: int = 0,
    ) -> dict:
        """
        Execute a query or aggregation pipeline against tenant data.

        Supports both simple find queries and full aggregation pipelines.
        Always enforces tenant_id scoping.
        """
        db = get_db()
        collection = db[collection_name]

        if pipeline:
            # Aggregation pipeline — prepend tenant filter
            scoped_pipeline = [
                {"$match": {"tenant_id": tenant_id}},
                *pipeline,
            ]
            results = []
            async for doc in collection.aggregate(scoped_pipeline):
                if "_id" in doc:
                    doc["_id"] = str(doc["_id"])
                results.append(doc)

            return {
                "collection": collection_name,
                "results": results,
                "count": len(results),
                "pipeline": True,
            }

        # Simple find query
        query = {"tenant_id": tenant_id, **(filter_query or {})}
        cursor = collection.find(query)

        if sort:
            cursor = cursor.sort(list(sort.items()))
        cursor = cursor.skip(skip).limit(limit)

        results = []
        async for doc in cursor:
            doc["_id"] = str(doc["_id"])
            results.append(doc)

        total = await collection.count_documents(query)

        return {
            "collection": collection_name,
            "results": results,
            "count": len(results),
            "total": total,
        }

    # ------------------------------------------------------------------
    # Schema-aware prompt injection
    # ------------------------------------------------------------------

    @staticmethod
    async def build_schema_context(tenant_id: str) -> str:
        """
        Build a schema context string for injection into the system prompt.

        Returns a formatted description of all registered collections
        and their schemas, enabling the AI to understand what data is
        available and generate appropriate views.
        """
        collections = await SchemaRegistry.get_collections(tenant_id)

        if not collections:
            return ""

        lines = ["## Available Data Collections"]
        for col in collections:
            name = col.get("display_name", col.get("collection_name", ""))
            desc = col.get("description", "")
            count = col.get("document_count", 0)
            lines.append(f"\n### {name} (`{col.get('collection_name', '')}`) — {count} documents")
            if desc:
                lines.append(f"{desc}")

            fields = col.get("fields", [])
            if fields:
                lines.append("Fields:")
                for f in fields[:20]:  # Cap at 20 fields per collection
                    ftype = f.get("type", "text")
                    flabel = f.get("label", f.get("field_id", ""))
                    attrs = []
                    if f.get("searchable"):
                        attrs.append("searchable")
                    if f.get("filterable"):
                        attrs.append("filterable")
                    if f.get("sortable"):
                        attrs.append("sortable")
                    attr_str = f" [{', '.join(attrs)}]" if attrs else ""
                    lines.append(f"  - {f.get('field_id', '')} ({ftype}): {flabel}{attr_str}")

        return "\n".join(lines)
