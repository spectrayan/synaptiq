"""Catalog schema and item domain models.

Schema (REQ-S1–S12): defines the tenant's field structure.
Item  (REQ-D1–D10): dynamic key-value documents validated against the schema.
"""
from datetime import datetime
from enum import StrEnum
from typing import Any

from pydantic import BaseModel, Field, field_validator

from synaptiq_api.models.base import (
    MongoBaseModel,
    TenantScopedMixin,
    TimestampMixin,
)


# ---------------------------------------------------------------------------
# Enumerations
# ---------------------------------------------------------------------------

class FieldType(StrEnum):
    """Supported catalog field types (REQ-S3)."""
    text = "text"
    number = "number"
    currency = "currency"
    boolean = "boolean"
    enum = "enum"
    multi_enum = "multi_enum"
    image_url = "image_url"
    url = "url"
    date = "date"
    rich_text = "rich_text"


class FieldVisibility(StrEnum):
    """Field visibility levels (REQ-S3, REQ-S11, REQ-S12)."""
    public = "public"
    admin_only = "admin_only"
    hidden = "hidden"


class FieldDesignator(StrEnum):
    """Special field designators (REQ-S5–S7)."""
    none = "none"
    primary_label = "primary_label"
    primary_image = "primary_image"
    primary_price = "primary_price"


class ItemStatus(StrEnum):
    """Catalog item lifecycle status (REQ-D5)."""
    active = "active"
    draft = "draft"
    archived = "archived"


# ---------------------------------------------------------------------------
# Schema field definition
# ---------------------------------------------------------------------------

class SchemaField(BaseModel):
    """A single field definition within a catalog schema (REQ-S3)."""

    field_id: str = Field(
        ...,
        min_length=1,
        max_length=64,
        pattern=r"^[a-z][a-z0-9_]*$",
        description="System key / slug",
    )
    label: str = Field(..., min_length=1, max_length=100, description="Display name")
    type: FieldType
    required: bool = False
    searchable: bool = True
    displayable: bool = True
    filterable: bool = False
    sortable: bool = False
    unit: str = Field(default="", max_length=20, description="e.g. 'sqft', 'kg'")
    visibility: FieldVisibility = FieldVisibility.public
    designator: FieldDesignator = FieldDesignator.none
    deprecated: bool = Field(default=False, description="REQ-S9: soft deprecation")
    display_order: int = Field(default=0, ge=0, description="REQ-S8: display priority")

    # Enum-specific
    enum_values: list[str] = Field(
        default_factory=list,
        description="Allowed values for enum / multi_enum types",
    )


# ---------------------------------------------------------------------------
# Catalog Schema document
# ---------------------------------------------------------------------------

class CatalogSchema(MongoBaseModel, TenantScopedMixin, TimestampMixin):
    """
    Per-tenant catalog schema — stored in the ``catalog_schemas`` collection.

    Enforces REQ-S4 (1–50 fields), REQ-S5 (one primary label), and REQ-S8
    (field ordering).
    """

    name: str = Field(default="Default Schema", max_length=100)
    fields: list[SchemaField] = Field(
        ...,
        min_length=1,
        max_length=50,
        description="1–50 fields per schema (REQ-S4)",
    )
    version: int = Field(default=1, ge=1, description="Schema version for migrations")
    is_active: bool = True

    @field_validator("fields")
    @classmethod
    def validate_schema_fields(cls, v: list[SchemaField]) -> list[SchemaField]:
        """Enforce one-and-only-one primary label designator (REQ-S5)."""
        primaries = [f for f in v if f.designator == FieldDesignator.primary_label]
        if len(primaries) != 1:
            raise ValueError("Exactly one field must be designated as primary_label (REQ-S5)")

        images = [f for f in v if f.designator == FieldDesignator.primary_image]
        if len(images) > 1:
            raise ValueError("At most one field may be designated as primary_image (REQ-S6)")

        prices = [f for f in v if f.designator == FieldDesignator.primary_price]
        if len(prices) > 1:
            raise ValueError("At most one field may be designated as primary_price (REQ-S7)")

        # Ensure unique field_ids
        ids = [f.field_id for f in v]
        if len(ids) != len(set(ids)):
            raise ValueError("Duplicate field_id values in schema")

        return v

    class Settings:
        collection = "catalog_schemas"


# ---------------------------------------------------------------------------
# Catalog Item document
# ---------------------------------------------------------------------------

class CatalogItem(MongoBaseModel, TenantScopedMixin, TimestampMixin):
    """
    A single catalog item — stored in the ``catalog_items`` collection.

    ``data`` holds the dynamic key→value fields defined by the tenant's schema.
    ``embedding`` stores the vector for Atlas Vector Search.
    """

    status: ItemStatus = ItemStatus.draft
    data: dict[str, Any] = Field(
        ...,
        description="Dynamic field values keyed by schema field_id",
    )
    embedding: list[float] | None = Field(
        default=None,
        description="Vector embedding for Atlas Vector Search (REQ-NF4)",
    )
    embedding_model: str = Field(
        default="",
        description="Model used to generate the embedding",
    )
    embedded_at: datetime | None = None

    class Settings:
        collection = "catalog_items"
