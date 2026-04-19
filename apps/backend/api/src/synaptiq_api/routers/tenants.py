"""Tenants router — tenant provisioning and config."""
from fastapi import APIRouter, Request
from pydantic import BaseModel

router = APIRouter()


class TenantConfig(BaseModel):
    name: str
    slug: str
    embedding_provider: str = "vertexai"
    llm_provider: str = "vertexai"
    branding: dict | None = None


@router.get("/{tenant_id}", summary="Get tenant configuration")
async def get_tenant(tenant_id: str, request: Request) -> dict:
    # TODO: load from MongoDB
    return {"tenant_id": tenant_id}


@router.post("/", summary="Provision a new tenant")
async def create_tenant(body: TenantConfig) -> dict:
    # TODO: insert tenant into MongoDB, seed default schema
    return {"slug": body.slug, "status": "provisioned"}


@router.put("/{tenant_id}/config", summary="Update tenant configuration")
async def update_tenant(tenant_id: str, body: dict) -> dict:
    # TODO: merge-update tenant config
    return {"tenant_id": tenant_id, "updated": True}
