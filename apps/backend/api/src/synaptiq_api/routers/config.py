"""Config router — Phase 9 (T9.1–T9.5).

Provides per-tenant configuration endpoints for AI persona, guardrails,
LLM provider (BYOK), component enablement, and action settings.

All endpoints require tenant-admin authentication.

    GET/PATCH  /config/ai           — persona name, tone, welcome, starters
    GET/PATCH  /config/ai/provider  — BYOK toggle, provider, encrypted key
    GET/PATCH  /config/ai/guardrails — out-of-scope msg, recommendation mode
    GET/PATCH  /config/components   — enable/disable DSL component types
    GET/PATCH  /config/actions      — enable/disable actions, labels, enquiry
"""
import logging
from typing import Any

from fastapi import APIRouter, Depends, HTTPException, Request, status
from pydantic import BaseModel, Field

from synaptiq_api.core.dependencies import require_tenant_admin
from synaptiq_api.models.tenant import (
    AIGuardrailsConfig,
    AIPersonaConfig,
    ActionConfig,
    ActionsConfig,
    ComponentEnablement,
    LLMProviderConfig,
    LLMProviderType,
    PersonalityTone,
)
from synaptiq_api.services.tenant_service import TenantService

logger = logging.getLogger(__name__)

router = APIRouter()


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

async def _get_tenant_or_404(request: Request) -> dict:
    """Fetch the tenant document for the authenticated admin, or raise 404."""
    tenant_id: str = request.state.tenant_id
    doc = await TenantService.get_tenant(tenant_id)
    if not doc:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Tenant '{tenant_id}' not found",
        )
    return doc


async def _patch_tenant_section(
    request: Request,
    section_key: str,
    updates: dict[str, Any],
) -> dict:
    """
    Merge partial updates into a nested tenant sub-document.

    Uses MongoDB dot-notation ($set) so only the provided fields are changed.
    """
    tenant_id: str = request.state.tenant_id
    set_ops: dict[str, Any] = {}
    for key, value in updates.items():
        if value is not None:
            # Serialize nested Pydantic models / lists
            if isinstance(value, BaseModel):
                value = value.model_dump()
            elif isinstance(value, list):
                value = [
                    v.model_dump() if isinstance(v, BaseModel) else v
                    for v in value
                ]
            set_ops[f"{section_key}.{key}"] = value

    if not set_ops:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="No fields to update.",
        )

    updated = await TenantService.update_tenant(tenant_id, set_ops)
    if not updated:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Tenant '{tenant_id}' not found or no changes applied.",
        )

    # Return the refreshed section
    doc = await TenantService.get_tenant(tenant_id)
    return doc.get(section_key, {})


# ===========================================================================
# T9.1 — AI Persona Config (REQ-AI1–AI5)
# ===========================================================================

class PatchAIPersonaRequest(BaseModel):
    """Partial update for AI persona settings."""

    display_name: str | None = Field(None, max_length=50)
    tone: PersonalityTone | None = None
    custom_instruction: str | None = Field(None, max_length=1000)
    welcome_message: str | None = Field(None, max_length=500)
    starter_prompts: list[str] | None = Field(None, max_length=8)


@router.get(
    "/ai",
    summary="Get AI persona config (REQ-AI1–AI5)",
    response_model=AIPersonaConfig,
    dependencies=[Depends(require_tenant_admin)],
)
async def get_ai_persona(request: Request) -> AIPersonaConfig:
    """Return the tenant's AI persona configuration."""
    doc = await _get_tenant_or_404(request)
    raw = doc.get("ai_persona", {})
    return AIPersonaConfig(**raw) if raw else AIPersonaConfig()


@router.patch(
    "/ai",
    summary="Update AI persona config (REQ-AI1–AI5)",
    response_model=AIPersonaConfig,
    dependencies=[Depends(require_tenant_admin)],
)
async def patch_ai_persona(
    request: Request,
    body: PatchAIPersonaRequest,
) -> AIPersonaConfig:
    """Partially update the AI persona (display name, tone, welcome, etc.)."""
    updates = body.model_dump(exclude_none=True)
    result = await _patch_tenant_section(request, "ai_persona", updates)
    return AIPersonaConfig(**result)


# ===========================================================================
# T9.2 — LLM Provider / BYOK Config (REQ-AI-P1–AI-P8)
# ===========================================================================

class PatchLLMProviderRequest(BaseModel):
    """Partial update for LLM provider settings."""

    provider: LLMProviderType | None = None
    model_id: str | None = None
    is_byok: bool | None = None
    byok_encrypted_key: str | None = Field(
        None,
        description="AES-256 envelope-encrypted API key. Never logged (REQ-AI-P4).",
    )


@router.get(
    "/ai/provider",
    summary="Get LLM provider config (REQ-AI-P1–AI-P8)",
    dependencies=[Depends(require_tenant_admin)],
)
async def get_llm_provider(request: Request) -> dict[str, Any]:
    """
    Return the tenant's LLM provider configuration.

    The encrypted key is masked — only the last 4 characters are returned.
    """
    doc = await _get_tenant_or_404(request)
    raw = doc.get("llm_provider", {})
    config = LLMProviderConfig(**raw) if raw else LLMProviderConfig()

    # Mask the encrypted key (REQ-AI-P7: never expose full key)
    result = config.model_dump()
    if result.get("byok_encrypted_key"):
        key = result["byok_encrypted_key"]
        result["byok_encrypted_key"] = f"****{key[-4:]}" if len(key) > 4 else "****"
    result["has_key"] = bool(config.byok_encrypted_key)

    return result


@router.patch(
    "/ai/provider",
    summary="Update LLM provider config (REQ-AI-P1–AI-P8)",
    dependencies=[Depends(require_tenant_admin)],
)
async def patch_llm_provider(
    request: Request,
    body: PatchLLMProviderRequest,
) -> dict[str, Any]:
    """
    Partially update the LLM provider config.

    When ``is_byok`` is set to ``false``, the encrypted key is cleared.
    """
    updates = body.model_dump(exclude_none=True)

    # If disabling BYOK, clear the key (REQ-AI-P6)
    if updates.get("is_byok") is False:
        updates["byok_encrypted_key"] = ""

    result = await _patch_tenant_section(request, "llm_provider", updates)
    config = LLMProviderConfig(**result)

    out = config.model_dump()
    if out.get("byok_encrypted_key"):
        key = out["byok_encrypted_key"]
        out["byok_encrypted_key"] = f"****{key[-4:]}" if len(key) > 4 else "****"
    out["has_key"] = bool(config.byok_encrypted_key)

    logger.info(
        "Updated LLM provider for tenant %s: provider=%s, byok=%s",
        request.state.tenant_id,
        config.provider,
        config.is_byok,
    )

    return out


# ===========================================================================
# T9.3 — AI Guardrails Config (REQ-AI6–AI10)
# ===========================================================================

class PatchAIGuardrailsRequest(BaseModel):
    """Partial update for AI guardrails."""

    out_of_scope_message: str | None = Field(None, max_length=500)
    recommendation_mode: bool | None = None
    language: str | None = Field(None, max_length=10)


@router.get(
    "/ai/guardrails",
    summary="Get AI guardrails config (REQ-AI6–AI10)",
    response_model=AIGuardrailsConfig,
    dependencies=[Depends(require_tenant_admin)],
)
async def get_ai_guardrails(request: Request) -> AIGuardrailsConfig:
    """Return the tenant's AI guardrail configuration."""
    doc = await _get_tenant_or_404(request)
    raw = doc.get("ai_guardrails", {})
    return AIGuardrailsConfig(**raw) if raw else AIGuardrailsConfig()


@router.patch(
    "/ai/guardrails",
    summary="Update AI guardrails config (REQ-AI6–AI10)",
    response_model=AIGuardrailsConfig,
    dependencies=[Depends(require_tenant_admin)],
)
async def patch_ai_guardrails(
    request: Request,
    body: PatchAIGuardrailsRequest,
) -> AIGuardrailsConfig:
    """Partially update the AI guardrails."""
    updates = body.model_dump(exclude_none=True)
    result = await _patch_tenant_section(request, "ai_guardrails", updates)
    return AIGuardrailsConfig(**result)


# ===========================================================================
# T9.4 — Component Enablement Config (REQ-CM1–CM3)
# ===========================================================================

class PatchComponentsRequest(BaseModel):
    """Partial update for component enablement toggles."""

    item_card: bool | None = None
    item_grid: bool | None = None
    item_detail: bool | None = None
    comparison_table: bool | None = None
    filter_summary: bool | None = None
    result_count: bool | None = None
    empty_state: bool | None = None
    # action_confirm cannot be disabled (REQ-A3)
    info_banner: bool | None = None


@router.get(
    "/components",
    summary="Get component enablement config (REQ-CM1–CM3)",
    response_model=ComponentEnablement,
    dependencies=[Depends(require_tenant_admin)],
)
async def get_components(request: Request) -> ComponentEnablement:
    """Return which DSL component types are enabled for this tenant."""
    doc = await _get_tenant_or_404(request)
    raw = doc.get("components", {})
    return ComponentEnablement(**raw) if raw else ComponentEnablement()


@router.patch(
    "/components",
    summary="Update component enablement config (REQ-CM1–CM3)",
    response_model=ComponentEnablement,
    dependencies=[Depends(require_tenant_admin)],
)
async def patch_components(
    request: Request,
    body: PatchComponentsRequest,
) -> ComponentEnablement:
    """
    Partially update component enablement toggles.

    ``action_confirm`` cannot be disabled (REQ-A3) — always forced to ``true``.
    """
    updates = body.model_dump(exclude_none=True)

    # REQ-A3: action_confirm can never be disabled
    updates.pop("action_confirm", None)

    result = await _patch_tenant_section(request, "components", updates)
    return ComponentEnablement(**result)


# ===========================================================================
# T9.5 — Actions Config (REQ-A1–A5)
# ===========================================================================

class PatchActionsRequest(BaseModel):
    """Partial update for action settings."""

    actions: list[ActionConfig] | None = None
    enquiry_webhook_url: str | None = None
    enquiry_email: str | None = None


@router.get(
    "/actions",
    summary="Get actions config (REQ-A1–A5)",
    response_model=ActionsConfig,
    dependencies=[Depends(require_tenant_admin)],
)
async def get_actions(request: Request) -> ActionsConfig:
    """Return the tenant's action configuration (labels, enablement, enquiry targets)."""
    doc = await _get_tenant_or_404(request)
    raw = doc.get("actions", {})
    return ActionsConfig(**raw) if raw else ActionsConfig()


@router.patch(
    "/actions",
    summary="Update actions config (REQ-A1–A5)",
    response_model=ActionsConfig,
    dependencies=[Depends(require_tenant_admin)],
)
async def patch_actions(
    request: Request,
    body: PatchActionsRequest,
) -> ActionsConfig:
    """
    Partially update action configuration.

    The ``actions`` array is a full replacement — send the entire list.
    ``enquiry_webhook_url`` and ``enquiry_email`` can be individually patched.
    """
    updates = body.model_dump(exclude_none=True)

    # The actions list must be serialized to dicts for MongoDB
    if "actions" in updates:
        updates["actions"] = [
            a.model_dump() if isinstance(a, BaseModel) else a
            for a in updates["actions"]
        ]

    result = await _patch_tenant_section(request, "actions", updates)
    return ActionsConfig(**result)
