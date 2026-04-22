"""Branding router — Phase 10 (T10.1–T10.5, T10.8–T10.9).

Provides per-tenant branding, theming, logo upload, WCAG contrast
validation, and named theme preset management.

All admin endpoints require ``require_tenant_admin``.
Public endpoints (end-user) only need a valid tenant context.

    GET/PATCH  /config/branding          — logo, colors, fonts, bg, favicon
    POST       /config/branding/logo     — upload logo file
    GET/POST   /config/themes            — list / create named theme presets
    PATCH/DEL  /config/themes/{theme_id} — update / delete a preset
    GET/PATCH  /config/personalization   — end-user toggle config
"""
import logging
import math
import re
from typing import Any

from fastapi import (
    APIRouter,
    Depends,
    HTTPException,
    Request,
    UploadFile,
    File,
    status,
)
from pydantic import BaseModel, Field

from synaptiq_api.core.dependencies import require_tenant_admin
from synaptiq_api.models.tenant import (
    BackgroundStyle,
    BrandingConfig,
    PersonalizationConfig,
    ThemePreset,
)
from synaptiq_api.services.tenant_service import TenantService

logger = logging.getLogger(__name__)

router = APIRouter()


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

async def _get_tenant_or_404(request: Request) -> dict:
    tenant_id: str = request.state.tenant_id
    doc = await TenantService.get_tenant(tenant_id)
    if not doc:
        raise HTTPException(status.HTTP_404_NOT_FOUND, f"Tenant '{tenant_id}' not found")
    return doc


async def _patch_tenant_section(
    request: Request,
    section_key: str,
    updates: dict[str, Any],
) -> dict:
    tenant_id: str = request.state.tenant_id
    set_ops: dict[str, Any] = {}
    for key, value in updates.items():
        if value is not None:
            if isinstance(value, BaseModel):
                value = value.model_dump()
            elif isinstance(value, list):
                value = [
                    v.model_dump() if isinstance(v, BaseModel) else v for v in value
                ]
            set_ops[f"{section_key}.{key}"] = value

    if not set_ops:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "No fields to update.")

    updated = await TenantService.update_tenant(tenant_id, set_ops)
    if not updated:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Tenant not found.")

    doc = await TenantService.get_tenant(tenant_id)
    return doc.get(section_key, {})


# ---------------------------------------------------------------------------
# T10.3 — WCAG AA contrast utilities
# ---------------------------------------------------------------------------

def _hex_to_rgb(hex_color: str) -> tuple[int, int, int]:
    """Convert #RRGGBB to (R, G, B) tuple."""
    h = hex_color.lstrip("#")
    return int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16)


def _relative_luminance(r: int, g: int, b: int) -> float:
    """Calculate WCAG 2.1 relative luminance."""
    def _linearize(c: int) -> float:
        s = c / 255.0
        return s / 12.92 if s <= 0.04045 else math.pow((s + 0.055) / 1.055, 2.4)

    return 0.2126 * _linearize(r) + 0.7152 * _linearize(g) + 0.0722 * _linearize(b)


def _contrast_ratio(color1: str, color2: str) -> float:
    """Calculate WCAG 2.1 contrast ratio between two hex colors."""
    l1 = _relative_luminance(*_hex_to_rgb(color1))
    l2 = _relative_luminance(*_hex_to_rgb(color2))
    lighter = max(l1, l2)
    darker = min(l1, l2)
    return (lighter + 0.05) / (darker + 0.05)


def _validate_contrast(
    primary: str,
    background_style: str,
) -> dict[str, Any]:
    """
    Check WCAG AA 4.5:1 contrast ratio (T10.3 — REQ-B10).

    Returns a dict with ``passes``, ``ratio``, and ``message``.
    """
    bg_color = "#FFFFFF" if background_style == "light" else "#13131F"
    ratio = _contrast_ratio(primary, bg_color)
    passes = ratio >= 4.5

    return {
        "passes": passes,
        "ratio": round(ratio, 2),
        "foreground": primary,
        "background": bg_color,
        "message": (
            f"Contrast ratio {ratio:.2f}:1 meets WCAG AA (≥4.5:1)"
            if passes
            else f"Contrast ratio {ratio:.2f}:1 fails WCAG AA (needs ≥4.5:1). Choose a different color."
        ),
    }


# ===========================================================================
# T10.1 — Branding Config (REQ-B1–B9)
# ===========================================================================

class PatchBrandingRequest(BaseModel):
    """Partial update for branding settings."""

    logo_url: str | None = None
    primary_color: str | None = Field(None, pattern=r"^#[0-9a-fA-F]{6}$")
    secondary_color: str | None = Field(None, pattern=r"^#[0-9a-fA-F]{6}$")
    background_style: BackgroundStyle | None = None
    heading_font: str | None = Field(None, max_length=50)
    body_font: str | None = Field(None, max_length=50)
    ui_font: str | None = Field(None, max_length=50)
    favicon_url: str | None = None
    page_title: str | None = Field(None, max_length=100)
    show_platform_branding: bool | None = None


@router.get(
    "/branding",
    summary="Get branding config (REQ-B1–B9)",
    response_model=BrandingConfig,
    dependencies=[Depends(require_tenant_admin)],
)
async def get_branding(request: Request) -> BrandingConfig:
    doc = await _get_tenant_or_404(request)
    raw = doc.get("branding", {})
    return BrandingConfig(**raw) if raw else BrandingConfig()


@router.patch(
    "/branding",
    summary="Update branding config (REQ-B1–B9)",
    dependencies=[Depends(require_tenant_admin)],
)
async def patch_branding(request: Request, body: PatchBrandingRequest) -> dict[str, Any]:
    """
    Partially update branding settings.

    If ``primary_color`` or ``background_style`` change, an inline WCAG AA
    contrast check is performed (REQ-B10). The update still succeeds, but the
    response includes a ``contrast_check`` warning if it fails.
    """
    updates = body.model_dump(exclude_none=True)

    # Perform the update
    result = await _patch_tenant_section(request, "branding", updates)
    config = BrandingConfig(**result)

    # Run contrast check (T10.3)
    contrast = _validate_contrast(config.primary_color, config.background_style.value)

    response: dict[str, Any] = config.model_dump()
    response["contrast_check"] = contrast

    if not contrast["passes"]:
        logger.warning(
            "WCAG AA contrast fail for tenant %s: %s on %s = %s:1",
            request.state.tenant_id,
            config.primary_color,
            config.background_style.value,
            contrast["ratio"],
        )

    return response


# ===========================================================================
# T10.2 — Logo Upload (REQ-B1)
# ===========================================================================

@router.post(
    "/branding/logo",
    summary="Upload logo (REQ-B1)",
    dependencies=[Depends(require_tenant_admin)],
)
async def upload_logo(
    request: Request,
    file: UploadFile = File(...),
) -> dict[str, str]:
    """
    Accept a logo image and store it.

    In production this would upload to Cloud Storage and return a signed URL.
    For now, we store the filename metadata and return a placeholder URL.
    """
    tenant_id: str = request.state.tenant_id

    # Validate content type
    allowed_types = {"image/png", "image/jpeg", "image/svg+xml", "image/webp"}
    if file.content_type not in allowed_types:
        raise HTTPException(
            status.HTTP_415_UNSUPPORTED_MEDIA_TYPE,
            f"Unsupported type '{file.content_type}'. Allowed: {', '.join(allowed_types)}",
        )

    # Validate file size (max 2MB)
    content = await file.read()
    if len(content) > 2 * 1024 * 1024:
        raise HTTPException(status.HTTP_413_REQUEST_ENTITY_TOO_LARGE, "Logo must be under 2MB.")

    # TODO: Upload to Cloud Storage in production
    # For now, construct a placeholder URL
    ext = file.filename.rsplit(".", 1)[-1] if file.filename else "png"
    logo_url = f"/assets/logos/{tenant_id}/logo.{ext}"

    # Update the branding document with the logo URL
    await TenantService.update_tenant(tenant_id, {"branding.logo_url": logo_url})

    logger.info("Logo uploaded for tenant %s: %s (%d bytes)", tenant_id, file.filename, len(content))

    return {"logo_url": logo_url, "filename": file.filename or "", "size_bytes": str(len(content))}


# ===========================================================================
# T10.3 — Standalone WCAG contrast check endpoint
# ===========================================================================

@router.get(
    "/branding/contrast-check",
    summary="WCAG AA contrast check (REQ-B10)",
    dependencies=[Depends(require_tenant_admin)],
)
async def contrast_check(
    foreground: str,
    background: str = "dark",
) -> dict[str, Any]:
    """
    Standalone contrast check: pass ``foreground`` hex color and optional
    ``background`` ('dark' | 'light' | hex).
    """
    hex_pattern = re.compile(r"^#[0-9a-fA-F]{6}$")
    if not hex_pattern.match(foreground):
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "foreground must be #RRGGBB")

    if background in ("dark", "light"):
        return _validate_contrast(foreground, background)

    if not hex_pattern.match(background):
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "background must be #RRGGBB or 'dark'/'light'")

    ratio = _contrast_ratio(foreground, background)
    passes = ratio >= 4.5
    return {
        "passes": passes,
        "ratio": round(ratio, 2),
        "foreground": foreground,
        "background": background,
        "message": (
            f"Contrast ratio {ratio:.2f}:1 meets WCAG AA (≥4.5:1)"
            if passes
            else f"Contrast ratio {ratio:.2f}:1 fails WCAG AA (needs ≥4.5:1)."
        ),
    }


# ===========================================================================
# T10.5 — Named Theme CRUD (REQ-B6, REQ-B7)
# ===========================================================================

class CreateThemeRequest(BaseModel):
    theme_id: str = Field(..., min_length=1, max_length=50, pattern=r"^[a-z0-9_-]+$")
    name: str = Field(..., min_length=1, max_length=100)
    primary_color: str = Field(default="#6366F1", pattern=r"^#[0-9a-fA-F]{6}$")
    secondary_color: str = Field(default="#8B5CF6", pattern=r"^#[0-9a-fA-F]{6}$")
    background_style: BackgroundStyle = BackgroundStyle.dark
    heading_font: str = Field(default="Inter", max_length=50)
    body_font: str = Field(default="Inter", max_length=50)
    is_default: bool = False


class PatchThemeRequest(BaseModel):
    name: str | None = Field(None, min_length=1, max_length=100)
    primary_color: str | None = Field(None, pattern=r"^#[0-9a-fA-F]{6}$")
    secondary_color: str | None = Field(None, pattern=r"^#[0-9a-fA-F]{6}$")
    background_style: BackgroundStyle | None = None
    heading_font: str | None = Field(None, max_length=50)
    body_font: str | None = Field(None, max_length=50)
    is_default: bool | None = None


@router.get(
    "/themes",
    summary="List named theme presets (REQ-B6)",
    response_model=list[ThemePreset],
    dependencies=[Depends(require_tenant_admin)],
)
async def list_themes(request: Request) -> list[ThemePreset]:
    doc = await _get_tenant_or_404(request)
    raw_list = doc.get("themes", [])
    return [ThemePreset(**t) for t in raw_list]


@router.post(
    "/themes",
    summary="Create named theme (REQ-B7)",
    response_model=ThemePreset,
    status_code=status.HTTP_201_CREATED,
    dependencies=[Depends(require_tenant_admin)],
)
async def create_theme(request: Request, body: CreateThemeRequest) -> ThemePreset:
    tenant_id: str = request.state.tenant_id
    doc = await _get_tenant_or_404(request)
    themes = doc.get("themes", [])

    # Enforce max 5 themes
    if len(themes) >= 5:
        raise HTTPException(
            status.HTTP_409_CONFLICT,
            "Maximum 5 theme presets allowed. Delete one before creating a new theme.",
        )

    # Check for duplicate theme_id
    if any(t.get("theme_id") == body.theme_id for t in themes):
        raise HTTPException(status.HTTP_409_CONFLICT, f"Theme '{body.theme_id}' already exists.")

    theme = ThemePreset(**body.model_dump())

    # If marking as default, unset the previous default
    if theme.is_default:
        for t in themes:
            t["is_default"] = False

    themes.append(theme.model_dump())
    await TenantService.update_tenant(tenant_id, {"themes": themes})

    logger.info("Created theme '%s' for tenant %s", body.theme_id, tenant_id)
    return theme


@router.patch(
    "/themes/{theme_id}",
    summary="Update named theme (REQ-B7)",
    response_model=ThemePreset,
    dependencies=[Depends(require_tenant_admin)],
)
async def patch_theme(
    request: Request,
    theme_id: str,
    body: PatchThemeRequest,
) -> ThemePreset:
    tenant_id: str = request.state.tenant_id
    doc = await _get_tenant_or_404(request)
    themes = doc.get("themes", [])

    idx = next((i for i, t in enumerate(themes) if t.get("theme_id") == theme_id), None)
    if idx is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, f"Theme '{theme_id}' not found.")

    updates = body.model_dump(exclude_none=True)

    # If setting as default, unset others
    if updates.get("is_default"):
        for t in themes:
            t["is_default"] = False

    themes[idx].update(updates)
    await TenantService.update_tenant(tenant_id, {"themes": themes})

    return ThemePreset(**themes[idx])


@router.delete(
    "/themes/{theme_id}",
    summary="Delete named theme (REQ-B7)",
    status_code=status.HTTP_204_NO_CONTENT,
    dependencies=[Depends(require_tenant_admin)],
)
async def delete_theme(request: Request, theme_id: str) -> None:
    tenant_id: str = request.state.tenant_id
    doc = await _get_tenant_or_404(request)
    themes = doc.get("themes", [])

    new_themes = [t for t in themes if t.get("theme_id") != theme_id]
    if len(new_themes) == len(themes):
        raise HTTPException(status.HTTP_404_NOT_FOUND, f"Theme '{theme_id}' not found.")

    await TenantService.update_tenant(tenant_id, {"themes": new_themes})
    logger.info("Deleted theme '%s' for tenant %s", theme_id, tenant_id)


# ===========================================================================
# T10.8/T10.9 — End-user personalization config
# ===========================================================================

class PatchPersonalizationRequest(BaseModel):
    allow_theme_switch: bool | None = None
    allow_font_switch: bool | None = None
    allow_bubble_style: bool | None = None


@router.get(
    "/personalization",
    summary="Get personalization config (REQ-C11–C16)",
    response_model=PersonalizationConfig,
    dependencies=[Depends(require_tenant_admin)],
)
async def get_personalization(request: Request) -> PersonalizationConfig:
    doc = await _get_tenant_or_404(request)
    raw = doc.get("personalization", {})
    return PersonalizationConfig(**raw) if raw else PersonalizationConfig()


@router.patch(
    "/personalization",
    summary="Update personalization config (REQ-C11–C16)",
    response_model=PersonalizationConfig,
    dependencies=[Depends(require_tenant_admin)],
)
async def patch_personalization(
    request: Request,
    body: PatchPersonalizationRequest,
) -> PersonalizationConfig:
    updates = body.model_dump(exclude_none=True)
    result = await _patch_tenant_section(request, "personalization", updates)
    return PersonalizationConfig(**result)


# ===========================================================================
# Public endpoint — tenant branding for end-users (no admin auth needed)
# ===========================================================================

@router.get(
    "/branding/public",
    summary="Get public branding (no auth required)",
)
async def get_public_branding(request: Request) -> dict[str, Any]:
    """
    Returns the tenant's branding config and AI persona for end-user rendering.
    This endpoint only requires a valid tenant context (header/subdomain),
    not admin authentication.
    """
    tenant_id: str = request.state.tenant_id
    doc = await TenantService.get_tenant(tenant_id)
    if not doc:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Tenant not found")

    branding = doc.get("branding", {})
    personalization = doc.get("personalization", {})
    themes = doc.get("themes", [])
    ai_persona = doc.get("ai_persona", {})

    # Find default theme if any
    default_theme = next((t for t in themes if t.get("is_default")), None)

    return {
        "branding": branding,
        "personalization": personalization,
        "themes": themes,
        "default_theme": default_theme,
        "ai_persona": {
            "display_name": ai_persona.get("display_name", "Synaptiq"),
            "welcome_message": ai_persona.get("welcome_message", ""),
            "starter_prompts": ai_persona.get("starter_prompts", []),
        },
    }
