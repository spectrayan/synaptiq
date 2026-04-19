"""Catalog router — schema import, retrieval, and annotation (Phase 3).

Endpoints:
  POST   /catalog/schema/import  — upload OpenAPI/JSON Schema/YAML → normalize & persist (T3.2)
  GET    /catalog/schema         — retrieve active schema for tenant (T3.5)
  PATCH  /catalog/schema         — annotate fields (toggle searchable, etc.) (T3.6)
  GET    /catalog/schema/versions — list schema version history

Item endpoints (Phase 4 — stubs):
  GET    /catalog/items          — list items
  POST   /catalog/items          — create item
"""
import logging

from fastapi import APIRouter, Depends, HTTPException, Request, UploadFile, File, Form, status
from pydantic import BaseModel, Field

from synaptiq_api.core.dependencies import require_tenant_admin
from synaptiq_api.services.schema_service import SchemaService

logger = logging.getLogger(__name__)
router = APIRouter()


# ---------------------------------------------------------------------------
# Request / Response models
# ---------------------------------------------------------------------------

class SchemaImportResponse(BaseModel):
    schema_id: str
    tenant_id: str
    version: int
    field_count: int
    fields: list[dict]


class SchemaAnnotateRequest(BaseModel):
    """Payload for PATCH /catalog/schema — annotate fields."""
    name: str | None = None
    fields: list[dict] | None = Field(
        default=None,
        description="Full field list replacement (re-validates all constraints)",
    )
    field_updates: dict[str, dict] | None = Field(
        default=None,
        description="Partial per-field updates keyed by field_id. "
        "Allowed keys: label, searchable, displayable, filterable, "
        "sortable, visibility, designator, display_order, required, deprecated.",
    )


# ---------------------------------------------------------------------------
# Schema endpoints
# ---------------------------------------------------------------------------

@router.post(
    "/schema/import",
    summary="Import a catalog schema",
    response_model=SchemaImportResponse,
    status_code=status.HTTP_201_CREATED,
    dependencies=[Depends(require_tenant_admin)],
)
async def import_schema(
    request: Request,
    file: UploadFile = File(..., description="OpenAPI spec, JSON Schema, or Synaptiq YAML"),
    schema_name: str = Form(default="Imported Schema"),
):
    """
    Upload an OpenAPI spec (JSON/YAML), JSON Schema, or Synaptiq YAML file.

    The parser will:
    1. Auto-detect the format
    2. Resolve $ref pointers
    3. Extract fields with inferred types and heuristic designators
    4. Validate constraints (1–50 fields, one primary label)
    5. Persist as the tenant's active schema

    Previous active schema is deactivated (version history preserved).
    """
    tenant_id: str = request.state.tenant_id

    content = await file.read()
    raw = content.decode("utf-8")

    # Detect content type from filename
    filename = (file.filename or "").lower()
    if filename.endswith((".yml", ".yaml")):
        content_type = "yaml"
    elif filename.endswith(".json"):
        content_type = "json"
    else:
        content_type = "auto"

    try:
        result = await SchemaService.import_schema(
            tenant_id=tenant_id,
            raw_content=raw,
            content_type=content_type,
            schema_name=schema_name,
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=str(e),
        ) from e

    return result


@router.post(
    "/schema/import/raw",
    summary="Import schema from raw JSON/YAML body",
    response_model=SchemaImportResponse,
    status_code=status.HTTP_201_CREATED,
    dependencies=[Depends(require_tenant_admin)],
)
async def import_schema_raw(
    request: Request,
    schema_name: str = "Imported Schema",
):
    """
    Alternative import endpoint — accepts raw JSON/YAML in the request body
    (for programmatic / API-first workflows).
    """
    tenant_id: str = request.state.tenant_id

    body = await request.body()
    raw = body.decode("utf-8")

    try:
        result = await SchemaService.import_schema(
            tenant_id=tenant_id,
            raw_content=raw,
            content_type="auto",
            schema_name=schema_name,
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=str(e),
        ) from e

    return result


@router.get(
    "/schema",
    summary="Get active catalog schema",
)
async def get_schema(request: Request):
    """
    Retrieve the active schema for the current tenant.

    Admin users see all fields (including admin_only visibility).
    End-users see only public fields (T3.7 / REQ-S11, REQ-S12).
    """
    tenant_id: str = request.state.tenant_id
    user = getattr(request.state, "user", None)
    is_admin = False

    if user:
        role = user.get("role") or (user.get("custom_claims") or {}).get("role")
        is_admin = role in ("platform_admin", "tenant_admin", "tenant_editor")

    schema = await SchemaService.get_active_schema(
        tenant_id=tenant_id,
        include_admin_fields=is_admin,
    )

    if not schema:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="No active schema found for this tenant. Import one via POST /catalog/schema/import.",
        )

    return schema


@router.patch(
    "/schema",
    summary="Annotate catalog schema fields",
    dependencies=[Depends(require_tenant_admin)],
)
async def update_schema(body: SchemaAnnotateRequest, request: Request):
    """
    Update field annotations on the active schema.

    Supports:
    - Renaming the schema
    - Full field list replacement
    - Partial per-field updates (toggle searchable, change label, etc.)

    Automatically invalidates the LLM prompt cache.
    """
    tenant_id: str = request.state.tenant_id

    updates = body.model_dump(exclude_none=True)
    if not updates:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="No updates provided.",
        )

    try:
        result = await SchemaService.update_schema(tenant_id, updates)
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=str(e),
        ) from e

    if not result:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="No active schema found for this tenant.",
        )

    return result


@router.get(
    "/schema/versions",
    summary="List schema version history",
    dependencies=[Depends(require_tenant_admin)],
)
async def list_schema_versions(request: Request):
    """List all schema versions for the tenant (latest first)."""
    tenant_id: str = request.state.tenant_id
    versions = await SchemaService.list_schema_versions(tenant_id)
    return {"versions": versions, "total": len(versions)}


# ---------------------------------------------------------------------------
# Item endpoints (Phase 4)
# ---------------------------------------------------------------------------

from synaptiq_api.models.catalog import ItemStatus
from synaptiq_api.services.catalog_service import CatalogService


class CreateItemRequest(BaseModel):
    data: dict = Field(..., description="Field values keyed by schema field_id")
    status: str = Field(default="draft", description="Initial status: draft | active")


class UpdateItemRequest(BaseModel):
    data: dict = Field(default_factory=dict, description="Field values to update")


class BulkStatusRequest(BaseModel):
    item_ids: list[str] = Field(..., min_length=1, max_length=200)
    status: str = Field(..., description="Target status: active | draft | archived")


class CsvImportRequest(BaseModel):
    field_mapping: dict[str, str] | None = Field(
        default=None,
        description="Optional {csv_header: field_id} mapping. Auto-mapped if omitted.",
    )


@router.post(
    "/items",
    summary="Create a catalog item",
    status_code=status.HTTP_201_CREATED,
    dependencies=[Depends(require_tenant_admin)],
)
async def create_item(body: CreateItemRequest, request: Request):
    """Create a catalog item validated against the tenant's active schema."""
    tenant_id: str = request.state.tenant_id

    try:
        item_status = ItemStatus(body.status)
    except ValueError:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Invalid status: '{body.status}'. Use: draft, active.",
        )

    try:
        result = await CatalogService.create_item(
            tenant_id=tenant_id,
            data=body.data,
            status=item_status,
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=str(e),
        ) from e

    return result


@router.get("/items", summary="List catalog items")
async def list_items(
    request: Request,
    status_filter: str | None = None,
    q: str | None = None,
    skip: int = 0,
    limit: int = 20,
):
    """Paginated item listing with optional status and text search filters."""
    tenant_id: str = request.state.tenant_id

    item_status = None
    if status_filter:
        try:
            item_status = ItemStatus(status_filter)
        except ValueError:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Invalid status filter: '{status_filter}'",
            )

    return await CatalogService.list_items(
        tenant_id=tenant_id,
        status_filter=item_status,
        search=q,
        skip=max(0, skip),
        limit=min(limit, 100),
    )


@router.get("/items/{item_id}", summary="Get a catalog item")
async def get_item(item_id: str, request: Request):
    """Get a single catalog item by ID."""
    tenant_id: str = request.state.tenant_id
    item = await CatalogService.get_item(tenant_id, item_id)
    if not item:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Item not found")
    return item


@router.patch(
    "/items/{item_id}",
    summary="Update a catalog item",
    dependencies=[Depends(require_tenant_admin)],
)
async def update_item(item_id: str, body: UpdateItemRequest, request: Request):
    """Update data fields on a catalog item. Re-validates against schema."""
    tenant_id: str = request.state.tenant_id

    try:
        result = await CatalogService.update_item(tenant_id, item_id, body.data)
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=str(e),
        ) from e

    if not result:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Item not found")
    return result


@router.delete(
    "/items/{item_id}",
    summary="Archive a catalog item",
    dependencies=[Depends(require_tenant_admin)],
)
async def delete_item(item_id: str, request: Request):
    """Soft-delete (archive) a catalog item."""
    tenant_id: str = request.state.tenant_id
    deleted = await CatalogService.delete_item(tenant_id, item_id)
    if not deleted:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Item not found")
    return {"detail": "Item archived", "item_id": item_id}


@router.patch(
    "/items/bulk/status",
    summary="Bulk update item status",
    dependencies=[Depends(require_tenant_admin)],
)
async def bulk_update_status(body: BulkStatusRequest, request: Request):
    """Bulk activate/deactivate/archive items (REQ-D7)."""
    tenant_id: str = request.state.tenant_id

    try:
        target = ItemStatus(body.status)
    except ValueError:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Invalid target status: '{body.status}'",
        )

    count = await CatalogService.bulk_update_status(
        tenant_id=tenant_id,
        item_ids=body.item_ids,
        target_status=target,
    )
    return {"modified": count, "target_status": body.status}


@router.post(
    "/import/csv",
    summary="Import items from CSV",
    status_code=status.HTTP_201_CREATED,
    dependencies=[Depends(require_tenant_admin)],
)
async def import_csv(
    request: Request,
    file: UploadFile = File(..., description="CSV file with item data"),
):
    """
    Upload a CSV file. Rows are validated per-schema and imported as draft items.
    Headers are auto-mapped to schema field_ids by slugifying column names.
    """
    tenant_id: str = request.state.tenant_id

    content = await file.read()
    csv_text = content.decode("utf-8")

    try:
        result = await CatalogService.import_csv(
            tenant_id=tenant_id,
            csv_content=csv_text,
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=str(e),
        ) from e

    return result

