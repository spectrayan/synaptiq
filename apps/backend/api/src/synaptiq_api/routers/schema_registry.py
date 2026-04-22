"""Schema Registry API router — generic data introspection (Phase 3).

Provides REST endpoints for:
  - Listing registered collections
  - Getting/inferring collection schemas
  - Registering new collections
  - Querying data with aggregation support
"""
import logging
from typing import Any

from fastapi import APIRouter, HTTPException, Request
from pydantic import BaseModel

from synaptiq_api.services.schema_registry import SchemaRegistry

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/schema-registry", tags=["schema-registry"])


class RegisterCollectionRequest(BaseModel):
    collection_name: str
    display_name: str = ""
    description: str = ""
    fields: list[dict] | None = None
    auto_infer: bool = True


class QueryDataRequest(BaseModel):
    collection_name: str
    pipeline: list[dict] | None = None
    filter_query: dict | None = None
    sort: dict | None = None
    limit: int = 100
    skip: int = 0


@router.get("/collections")
async def list_collections(request: Request) -> list[dict]:
    """List all data collections for the authenticated tenant."""
    tenant_id: str = request.state.tenant_id
    return await SchemaRegistry.get_collections(tenant_id)


@router.get("/collections/{collection_name}/schema")
async def get_collection_schema(
    collection_name: str,
    request: Request,
) -> dict:
    """Get the registered schema for a collection."""
    tenant_id: str = request.state.tenant_id
    schema = await SchemaRegistry.get_schema(tenant_id, collection_name)
    if not schema:
        raise HTTPException(status_code=404, detail="Collection schema not found")
    return schema


@router.post("/collections/{collection_name}/infer")
async def infer_collection_schema(
    collection_name: str,
    request: Request,
    sample_size: int = 50,
) -> dict:
    """Auto-infer schema from sampled documents."""
    tenant_id: str = request.state.tenant_id
    return await SchemaRegistry.infer_schema(tenant_id, collection_name, sample_size)


@router.post("/collections/register")
async def register_collection(
    body: RegisterCollectionRequest,
    request: Request,
) -> dict:
    """Register a new data collection."""
    tenant_id: str = request.state.tenant_id
    role = getattr(request.state, "role", "user")
    if role not in ("admin", "owner", "platform_admin"):
        raise HTTPException(status_code=403, detail="Admin access required")

    return await SchemaRegistry.register_collection(
        tenant_id=tenant_id,
        collection_name=body.collection_name,
        display_name=body.display_name,
        description=body.description,
        fields=body.fields,
        auto_infer=body.auto_infer,
    )


@router.post("/query")
async def query_data(
    body: QueryDataRequest,
    request: Request,
) -> dict:
    """Execute a query against tenant data with tenant scoping."""
    tenant_id: str = request.state.tenant_id
    return await SchemaRegistry.query_data(
        tenant_id=tenant_id,
        collection_name=body.collection_name,
        pipeline=body.pipeline,
        filter_query=body.filter_query,
        sort=body.sort,
        limit=body.limit,
        skip=body.skip,
    )
