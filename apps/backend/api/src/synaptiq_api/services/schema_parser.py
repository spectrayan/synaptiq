"""Schema parser — converts OpenAPI specs, JSON Schema, and Synaptiq YAML
into a normalized list of SchemaField objects (T3.2, T3.3).

Supported input formats:
  - OpenAPI 3.x (JSON or YAML) — extracts from component schemas
  - JSON Schema (draft-07 / 2020-12) — extracts from properties
  - Synaptiq YAML/JSON — native format passthrough

Uses `jsonref` for $ref resolution (critical for real-world OpenAPI specs).
Type inference and designator heuristics are custom since no library covers
our domain-specific needs (catalog field classification).
"""
import json
import logging
import re
from typing import Any

import jsonref
import yaml

from synaptiq_api.models.catalog import (
    FieldDesignator,
    FieldType,
    FieldVisibility,
    SchemaField,
)

logger = logging.getLogger(__name__)


# ---------------------------------------------------------------------------
# Type mapping tables
# ---------------------------------------------------------------------------

_OPENAPI_TYPE_MAP: dict[str, FieldType] = {
    "string": FieldType.text,
    "integer": FieldType.number,
    "number": FieldType.number,
    "boolean": FieldType.boolean,
}

_JSON_FORMAT_MAP: dict[str, FieldType] = {
    "date": FieldType.date,
    "date-time": FieldType.date,
    "uri": FieldType.url,
    "url": FieldType.url,
    "email": FieldType.text,
    "currency": FieldType.currency,
}


# ---------------------------------------------------------------------------
# Heuristic helpers
# ---------------------------------------------------------------------------

_IMAGE_PATTERNS = re.compile(
    r"(image|photo|picture|thumbnail|avatar|logo|img|cover|banner)",
    re.IGNORECASE,
)
_PRICE_PATTERNS = re.compile(
    r"(price|cost|amount|fee|rate|charge|msrp|retail)",
    re.IGNORECASE,
)
_LABEL_PATTERNS = re.compile(
    r"(name|title|label|heading|display_name|product_name|item_name)",
    re.IGNORECASE,
)


def _slugify(name: str) -> str:
    """Convert a property name to a valid field_id slug."""
    slug = re.sub(r"[^a-z0-9_]", "_", name.lower().strip())
    slug = re.sub(r"_+", "_", slug).strip("_")
    if slug and not slug[0].isalpha():
        slug = f"f_{slug}"
    return slug or "field"


def _infer_type(prop: dict[str, Any], field_name: str) -> FieldType:
    """Infer a FieldType from an OpenAPI/JSON Schema property definition."""
    fmt = prop.get("format", "")
    if fmt in _JSON_FORMAT_MAP:
        return _JSON_FORMAT_MAP[fmt]

    if _IMAGE_PATTERNS.search(field_name):
        return FieldType.image_url

    if "enum" in prop:
        return FieldType.enum

    prop_type = prop.get("type", "string")
    if prop_type == "array":
        items = prop.get("items", {})
        if "enum" in items:
            return FieldType.multi_enum
        return FieldType.text

    return _OPENAPI_TYPE_MAP.get(prop_type, FieldType.text)


def _infer_designator(
    field_id: str,
    field_type: FieldType,
    assigned: set[FieldDesignator],
) -> FieldDesignator:
    """Heuristically assign a designator if not yet taken."""
    if FieldDesignator.primary_label not in assigned and _LABEL_PATTERNS.search(field_id):
        return FieldDesignator.primary_label

    if FieldDesignator.primary_image not in assigned and field_type == FieldType.image_url:
        return FieldDesignator.primary_image

    if FieldDesignator.primary_price not in assigned and _PRICE_PATTERNS.search(field_id):
        return FieldDesignator.primary_price

    return FieldDesignator.none


# ---------------------------------------------------------------------------
# Format detection
# ---------------------------------------------------------------------------

class SchemaFormat:
    OPENAPI = "openapi"
    JSON_SCHEMA = "json_schema"
    SYNAPTIQ = "synaptiq"


def detect_format(data: dict[str, Any]) -> str:
    """Detect whether the parsed dict is OpenAPI, JSON Schema, or Synaptiq native."""
    if "openapi" in data:
        return SchemaFormat.OPENAPI
    if "components" in data and "schemas" in data.get("components", {}):
        return SchemaFormat.OPENAPI
    if "fields" in data and isinstance(data["fields"], list):
        return SchemaFormat.SYNAPTIQ
    if data.get("$schema") or (data.get("type") == "object" and "properties" in data):
        return SchemaFormat.JSON_SCHEMA
    if "properties" in data:
        return SchemaFormat.JSON_SCHEMA
    raise ValueError(
        "Unrecognized schema format. Expected OpenAPI 3.x, JSON Schema, or Synaptiq YAML/JSON."
    )


# ---------------------------------------------------------------------------
# Property extraction
# ---------------------------------------------------------------------------

def _parse_properties(
    properties: dict[str, Any],
    required_fields: list[str] | None = None,
) -> list[SchemaField]:
    """Convert a JSON Schema / OpenAPI properties dict into SchemaField list."""
    required_set = set(required_fields or [])
    assigned_designators: set[FieldDesignator] = set()
    fields: list[SchemaField] = []

    for idx, (prop_name, prop_def) in enumerate(properties.items()):
        if not isinstance(prop_def, dict):
            continue

        field_id = _slugify(prop_name)
        field_type = _infer_type(prop_def, prop_name)

        designator = _infer_designator(field_id, field_type, assigned_designators)
        if designator != FieldDesignator.none:
            assigned_designators.add(designator)

        searchable = field_type in (
            FieldType.text, FieldType.rich_text, FieldType.enum,
        )
        filterable = field_type in (
            FieldType.enum, FieldType.multi_enum, FieldType.boolean,
            FieldType.number, FieldType.currency,
        )
        sortable = field_type in (
            FieldType.number, FieldType.currency, FieldType.date,
        )

        enum_values: list[str] = []
        if field_type == FieldType.enum:
            enum_values = [str(v) for v in prop_def.get("enum", [])]
        elif field_type == FieldType.multi_enum:
            items = prop_def.get("items", {})
            enum_values = [str(v) for v in items.get("enum", [])]

        label = prop_def.get("title") or prop_name.replace("_", " ").title()

        fields.append(SchemaField(
            field_id=field_id,
            label=label,
            type=field_type,
            required=prop_name in required_set,
            searchable=searchable,
            displayable=True,
            filterable=filterable,
            sortable=sortable,
            visibility=FieldVisibility.public,
            designator=designator,
            display_order=idx,
            enum_values=enum_values,
        ))

    return fields


# ---------------------------------------------------------------------------
# Format-specific parsers
# ---------------------------------------------------------------------------

def parse_openapi(data: dict[str, Any]) -> list[SchemaField]:
    """Extract fields from the largest component schema in an OpenAPI spec.

    Uses jsonref to resolve all $ref pointers before traversal.
    """
    # Resolve all $ref pointers in-place
    resolved = jsonref.replace_refs(data, lazy_load=False)
    schemas = resolved.get("components", {}).get("schemas", {})
    if not schemas:
        raise ValueError("OpenAPI spec contains no component schemas to import.")

    # Pick the schema with the most properties
    best_name = ""
    best_props: dict[str, Any] = {}
    best_required: list[str] = []

    for name, schema_def in schemas.items():
        if not isinstance(schema_def, dict):
            continue
        props = schema_def.get("properties", {})
        if len(props) > len(best_props):
            best_name = name
            best_props = props
            best_required = schema_def.get("required", [])

    if not best_props:
        raise ValueError("No properties found in any OpenAPI component schema.")

    logger.info("Importing from OpenAPI schema: %s (%d properties)", best_name, len(best_props))
    return _parse_properties(best_props, best_required)


def parse_json_schema(data: dict[str, Any]) -> list[SchemaField]:
    """Extract fields from a JSON Schema object. Resolves $ref pointers."""
    resolved = jsonref.replace_refs(data, lazy_load=False)
    properties = resolved.get("properties", {})
    if not properties:
        raise ValueError("JSON Schema contains no properties to import.")

    required_fields = resolved.get("required", [])
    return _parse_properties(properties, required_fields)


def parse_synaptiq(data: dict[str, Any]) -> list[SchemaField]:
    """Parse native Synaptiq schema format (passthrough with validation)."""
    raw_fields = data.get("fields", [])
    if not raw_fields:
        raise ValueError("Synaptiq schema contains no fields.")

    return [SchemaField(**f) for f in raw_fields]


# ---------------------------------------------------------------------------
# Main entry point
# ---------------------------------------------------------------------------

def parse_schema_input(raw_content: str, content_type: str = "auto") -> list[SchemaField]:
    """
    Parse raw schema content (string) into a normalized list of SchemaField.

    Args:
        raw_content: The raw schema string (JSON or YAML).
        content_type: Hint — 'json', 'yaml', or 'auto' (detect).

    Returns:
        List of SchemaField objects ready for CatalogSchema.

    Raises:
        ValueError: If format is unrecognized or content is invalid.
    """
    data: dict[str, Any]
    if content_type == "yaml" or (content_type == "auto" and not raw_content.strip().startswith("{")):
        try:
            data = yaml.safe_load(raw_content)
        except yaml.YAMLError as e:
            raise ValueError(f"Invalid YAML: {e}") from e
    else:
        try:
            data = json.loads(raw_content)
        except json.JSONDecodeError as e:
            raise ValueError(f"Invalid JSON: {e}") from e

    if not isinstance(data, dict):
        raise ValueError("Schema content must be a JSON/YAML object (dict).")

    fmt = detect_format(data)
    logger.info("Detected schema format: %s", fmt)

    if fmt == SchemaFormat.OPENAPI:
        fields = parse_openapi(data)
    elif fmt == SchemaFormat.JSON_SCHEMA:
        fields = parse_json_schema(data)
    elif fmt == SchemaFormat.SYNAPTIQ:
        fields = parse_synaptiq(data)
    else:
        raise ValueError(f"Unsupported schema format: {fmt}")

    # Ensure a primary_label exists
    has_primary_label = any(f.designator == FieldDesignator.primary_label for f in fields)
    if not has_primary_label and fields:
        for field in fields:
            if field.type in (FieldType.text, FieldType.rich_text):
                field.designator = FieldDesignator.primary_label
                logger.info("Auto-assigned primary_label to field: %s", field.field_id)
                break
        else:
            fields[0].designator = FieldDesignator.primary_label
            logger.info("Fallback: assigned primary_label to first field: %s", fields[0].field_id)

    # Enforce max 50 fields
    if len(fields) > 50:
        logger.warning("Schema has %d fields, truncating to 50", len(fields))
        fields = fields[:50]

    return fields
