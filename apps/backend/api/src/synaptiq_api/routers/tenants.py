"""Tenants router — Phase 2: tenant provisioning, CRUD, and admin management.

Endpoints:
    T2.1  POST   /tenants              Create tenant (platform admin)
    T2.2  GET    /tenants/{id}          Get tenant details
    T2.2  PATCH  /tenants/{id}          Update tenant config/status/limits
    T2.4  POST   /tenants/{id}/admins   Invite admin via Firebase email
    T2.5  GET    /tenants/{id}/admins   List admins with roles
    T2.6  Limit enforcement is handled at the service layer
"""
import logging
from datetime import datetime

from fastapi import APIRouter, Depends, HTTPException, Request, status
from pydantic import BaseModel, EmailStr, Field

import firebase_admin.auth as fb_auth

from synaptiq_api.core.dependencies import require_platform_admin, require_tenant_admin
from synaptiq_api.models.tenant import (
    AccessMode,
    AdminRole,
    Tenant,
    TenantAdmin,
    TenantLimits,
    TenantStatus,
)
from synaptiq_api.services.tenant_service import TenantService

logger = logging.getLogger(__name__)

router = APIRouter()


# ---------------------------------------------------------------------------
# Request / Response schemas
# ---------------------------------------------------------------------------

class CreateTenantRequest(BaseModel):
    """Body for POST /tenants (T2.1)."""

    tenant_id: str = Field(..., min_length=1, max_length=63, pattern=r"^[a-z0-9-]+$")
    name: str = Field(..., min_length=1, max_length=200)
    slug: str = Field(..., min_length=1, max_length=63, pattern=r"^[a-z0-9-]+$")
    catalog_label: str = Field(default="Products", max_length=50)
    access_mode: AccessMode = AccessMode.public


class UpdateTenantRequest(BaseModel):
    """Body for PATCH /tenants/{id} (T2.2)."""

    name: str | None = None
    status: TenantStatus | None = None
    access_mode: AccessMode | None = None
    catalog_label: str | None = None
    limits: TenantLimits | None = None


class InviteAdminRequest(BaseModel):
    """Body for POST /tenants/{id}/admins (T2.4)."""

    email: EmailStr
    role: AdminRole = AdminRole.editor


class TenantResponse(BaseModel):
    """Standard tenant response."""

    tenant_id: str
    name: str
    slug: str
    status: TenantStatus
    access_mode: AccessMode
    catalog_label: str
    limits: TenantLimits | None = None
    admins: list[TenantAdmin] | None = None
    created_at: datetime | None = None
    updated_at: datetime | None = None


class AdminResponse(BaseModel):
    """Response for admin invite."""

    uid: str
    email: str
    role: AdminRole
    invited_at: datetime
    accepted: bool


# ---------------------------------------------------------------------------
# T2.1 — POST /tenants (platform admin only)
# ---------------------------------------------------------------------------

@router.post(
    "/",
    summary="Create a new tenant (platform admin only)",
    status_code=status.HTTP_201_CREATED,
    response_model=TenantResponse,
    dependencies=[Depends(require_platform_admin)],
)
async def create_tenant(body: CreateTenantRequest) -> TenantResponse:
    """
    Provision a new tenant with default configuration (REQ-T1, REQ-T3).

    Auto-provisions default config document (T2.3):
    - Default TenantLimits
    - Default AIPersonaConfig
    - Default BrandingConfig
    - Default ComponentEnablement
    - Default ActionsConfig
    """
    # Check for duplicate tenant_id or slug
    existing = await TenantService.get_tenant(body.tenant_id)
    if existing:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail=f"Tenant '{body.tenant_id}' already exists",
        )

    # Create tenant with full defaults (T2.3 — auto-provision)
    tenant = Tenant(
        tenant_id=body.tenant_id,
        name=body.name,
        slug=body.slug,
        catalog_label=body.catalog_label,
        access_mode=body.access_mode,
        status=TenantStatus.onboarding,
        # All sub-documents get Pydantic defaults automatically
    )

    result = await TenantService.create_tenant(tenant)
    logger.info("Created tenant: %s", body.tenant_id)

    return TenantResponse(
        tenant_id=body.tenant_id,
        name=body.name,
        slug=body.slug,
        status=TenantStatus.onboarding,
        access_mode=body.access_mode,
        catalog_label=body.catalog_label,
        limits=tenant.limits,
        created_at=datetime.utcnow(),
        updated_at=datetime.utcnow(),
    )


# ---------------------------------------------------------------------------
# T2.2 — GET /tenants/{tenant_id}
# ---------------------------------------------------------------------------

@router.get(
    "/{tenant_id}",
    summary="Get tenant configuration",
    response_model=TenantResponse,
    dependencies=[Depends(require_tenant_admin)],
)
async def get_tenant(tenant_id: str) -> TenantResponse:
    """Get tenant details by ID."""
    doc = await TenantService.get_tenant(tenant_id)
    if not doc:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Tenant '{tenant_id}' not found",
        )

    return TenantResponse(
        tenant_id=doc["tenant_id"],
        name=doc["name"],
        slug=doc["slug"],
        status=doc.get("status", "onboarding"),
        access_mode=doc.get("access_mode", "public"),
        catalog_label=doc.get("catalog_label", "Products"),
        limits=doc.get("limits"),
        admins=doc.get("admins"),
        created_at=doc.get("created_at"),
        updated_at=doc.get("updated_at"),
    )


# ---------------------------------------------------------------------------
# T2.2 — PATCH /tenants/{tenant_id}
# ---------------------------------------------------------------------------

@router.patch(
    "/{tenant_id}",
    summary="Update tenant config, status, or limits",
    response_model=TenantResponse,
    dependencies=[Depends(require_tenant_admin)],
)
async def update_tenant(tenant_id: str, body: UpdateTenantRequest) -> TenantResponse:
    """
    Partially update tenant fields (REQ-T3, REQ-T5).

    Supports updating:
    - name, status, access_mode, catalog_label
    - limits (full replacement of limits sub-document)
    """
    updates = body.model_dump(exclude_none=True)
    if not updates:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="No fields to update",
        )

    # Serialize nested models to dicts for MongoDB
    if "limits" in updates and updates["limits"] is not None:
        updates["limits"] = body.limits.model_dump()

    updated = await TenantService.update_tenant(tenant_id, updates)
    if not updated:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Tenant '{tenant_id}' not found or no changes applied",
        )

    # Fetch updated doc
    doc = await TenantService.get_tenant(tenant_id)
    return TenantResponse(
        tenant_id=doc["tenant_id"],
        name=doc["name"],
        slug=doc["slug"],
        status=doc.get("status", "onboarding"),
        access_mode=doc.get("access_mode", "public"),
        catalog_label=doc.get("catalog_label", "Products"),
        limits=doc.get("limits"),
        admins=doc.get("admins"),
        created_at=doc.get("created_at"),
        updated_at=doc.get("updated_at"),
    )


# ---------------------------------------------------------------------------
# T2.4 — POST /tenants/{tenant_id}/admins (invite admin)
# ---------------------------------------------------------------------------

@router.post(
    "/{tenant_id}/admins",
    summary="Invite an admin to this tenant",
    status_code=status.HTTP_201_CREATED,
    response_model=AdminResponse,
    dependencies=[Depends(require_tenant_admin)],
)
async def invite_admin(tenant_id: str, body: InviteAdminRequest) -> AdminResponse:
    """
    Invite an admin to a tenant via Firebase email (REQ-T8).

    - If the email is already a Firebase user, their UID is used.
    - If not, a new Firebase user is created (they'll receive an invite).
    - The user is added to the tenant's admins array with custom claims set.
    """
    # Verify tenant exists
    doc = await TenantService.get_tenant(tenant_id)
    if not doc:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Tenant '{tenant_id}' not found",
        )

    # Check if admin already exists in this tenant
    existing_admins = doc.get("admins", [])
    if any(a.get("email") == body.email for a in existing_admins):
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail=f"Admin '{body.email}' already exists in tenant '{tenant_id}'",
        )

    # Resolve or create Firebase user
    try:
        user = fb_auth.get_user_by_email(body.email)
        uid = user.uid
    except fb_auth.UserNotFoundError:
        # Create user — they'll need to set their password via reset flow
        user = fb_auth.create_user(email=body.email)
        uid = user.uid
        logger.info("Created new Firebase user for admin invite: %s", body.email)

    # Set custom claims (role + tenant_id)
    role_str = f"tenant_{body.role.value}" if body.role != AdminRole.owner else "tenant_admin"
    fb_auth.set_custom_user_claims(uid, {
        "role": role_str,
        "tenant_id": tenant_id,
    })

    # Add admin to tenant's admins array
    now = datetime.utcnow()
    admin_entry = TenantAdmin(
        uid=uid,
        email=body.email,
        role=body.role,
        invited_at=now,
        accepted=False,
    )

    from synaptiq_api.core.mongodb import get_db
    db = get_db()
    await db.tenants.update_one(
        {"tenant_id": tenant_id},
        {"$push": {"admins": admin_entry.model_dump()}},
    )

    # Invalidate cache
    await TenantService._invalidate_cache(tenant_id)
    logger.info("Invited admin %s to tenant %s as %s", body.email, tenant_id, body.role)

    return AdminResponse(
        uid=uid,
        email=body.email,
        role=body.role,
        invited_at=now,
        accepted=False,
    )


# ---------------------------------------------------------------------------
# T2.5 — GET /tenants/{tenant_id}/admins
# ---------------------------------------------------------------------------

@router.get(
    "/{tenant_id}/admins",
    summary="List admins for this tenant",
    response_model=list[AdminResponse],
    dependencies=[Depends(require_tenant_admin)],
)
async def list_admins(tenant_id: str) -> list[AdminResponse]:
    """List all admins for a tenant with roles (REQ-T8)."""
    doc = await TenantService.get_tenant(tenant_id)
    if not doc:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Tenant '{tenant_id}' not found",
        )

    admins = doc.get("admins", [])
    return [
        AdminResponse(
            uid=a["uid"],
            email=a["email"],
            role=a.get("role", "editor"),
            invited_at=a.get("invited_at", datetime.utcnow()),
            accepted=a.get("accepted", False),
        )
        for a in admins
    ]


# ---------------------------------------------------------------------------
# T2.6 — Tenant limit enforcement (utility, used by other routers)
# ---------------------------------------------------------------------------

async def enforce_tenant_limit(
    tenant_id: str,
    resource: str,
    current_count: int,
) -> None:
    """
    Check if a write operation exceeds per-tenant limits (REQ-T5).

    Args:
        tenant_id: Tenant to check limits for
        resource: "catalog_items" | "users"
        current_count: Current number of the resource

    Raises:
        HTTPException 429 if limit exceeded
    """
    doc = await TenantService.get_tenant(tenant_id)
    if not doc:
        return  # Fail open if tenant not found (shouldn't happen)

    limits = doc.get("limits", {})
    limit_map = {
        "catalog_items": limits.get("max_catalog_items", 1_000),
        "users": limits.get("max_users", 50),
    }

    max_allowed = limit_map.get(resource)
    if max_allowed is None:
        return

    if current_count >= max_allowed:
        raise HTTPException(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            detail=f"Tenant limit exceeded for {resource}: {current_count}/{max_allowed}",
        )
