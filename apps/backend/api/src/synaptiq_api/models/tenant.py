"""Tenant domain models — REQ-T1 through REQ-T8.

Each tenant is a fully isolated workspace with its own config, catalog,
LLM context, users, and branding.
"""
from datetime import datetime
from enum import StrEnum
from typing import Any

from pydantic import BaseModel, Field, field_validator

from synaptiq_api.models.base import MongoBaseModel, TimestampMixin


# ---------------------------------------------------------------------------
# Enumerations
# ---------------------------------------------------------------------------

class TenantStatus(StrEnum):
    active = "active"
    suspended = "suspended"
    trial = "trial"
    onboarding = "onboarding"


class LLMProviderType(StrEnum):
    platform_managed = "platform_managed"
    openai = "openai"
    gemini = "gemini"
    anthropic = "anthropic"


class PersonalityTone(StrEnum):
    professional = "professional"
    friendly = "friendly"
    concise = "concise"
    enthusiastic = "enthusiastic"
    formal = "formal"


class BackgroundStyle(StrEnum):
    light = "light"
    dark = "dark"
    auto = "auto"


class AccessMode(StrEnum):
    public = "public"
    unlisted = "unlisted"
    protected = "protected"  # Phase 2
    authenticated = "authenticated"  # Phase 2


class ExceedAction(StrEnum):
    """Action taken when tenant exceeds a usage limit."""
    warn_admin = "warn_admin"
    throttle = "throttle"
    block = "block"


class AdminRole(StrEnum):
    """Tenant-level admin roles (REQ-T8)."""
    owner = "owner"
    editor = "editor"
    viewer = "viewer"


# ---------------------------------------------------------------------------
# Embedded sub-documents
# ---------------------------------------------------------------------------

class TenantLimits(BaseModel):
    """Per-tenant resource limits (REQ-T5, REQ-PR4)."""

    max_catalog_items: int = Field(default=1_000, ge=1, le=100_000)
    max_monthly_tokens: int = Field(default=500_000, ge=0)
    max_users: int = Field(default=50, ge=1)
    max_requests_per_minute: int = Field(default=60, ge=1, description="REQ-NF-RL1")
    seat_cap: int = Field(default=100, ge=1, description="REQ-PR4: monthly seat cap")
    token_exceed_action: ExceedAction = ExceedAction.warn_admin
    seat_exceed_action: ExceedAction = ExceedAction.warn_admin


class AIPersonaConfig(BaseModel):
    """LLM persona configuration (REQ-AI1 through REQ-AI5)."""

    display_name: str = Field(default="Synaptiq", max_length=50)
    tone: PersonalityTone = PersonalityTone.professional
    custom_instruction: str = Field(default="", max_length=1000)
    welcome_message: str = Field(
        default="Hi! I'm here to help you explore our catalog. What are you looking for?",
        max_length=500,
    )
    starter_prompts: list[str] = Field(
        default_factory=list,
        max_length=8,
        description="Up to 8 suggested starter prompts (REQ-AI5)",
    )

    @field_validator("starter_prompts")
    @classmethod
    def cap_starter_prompts(cls, v: list[str]) -> list[str]:
        if len(v) > 8:
            raise ValueError("Maximum 8 starter prompts allowed")
        return v


class AIGuardrailsConfig(BaseModel):
    """LLM behavioral guardrails (REQ-AI6 through REQ-AI10)."""

    out_of_scope_message: str = Field(
        default="I'm here to help you explore our catalog. What are you looking for?",
        max_length=500,
    )
    recommendation_mode: bool = Field(default=True, description="REQ-AI9")
    language: str = Field(default="en", max_length=10, description="REQ-AI10: ISO 639-1")


class LLMProviderConfig(BaseModel):
    """LLM provider settings (REQ-AI-P1 through REQ-AI-P8)."""

    provider: LLMProviderType = LLMProviderType.platform_managed
    model_id: str = Field(default="", description="Specific model within the provider")
    byok_encrypted_key: str = Field(
        default="",
        description="AES-256 envelope-encrypted API key (REQ-AI-P4). Never logged.",
    )
    is_byok: bool = False


class BrandingConfig(BaseModel):
    """Branding and theming settings (REQ-B1 through REQ-B12)."""

    logo_url: str = ""
    primary_color: str = Field(default="#6366F1", pattern=r"^#[0-9a-fA-F]{6}$")
    secondary_color: str = Field(default="#8B5CF6", pattern=r"^#[0-9a-fA-F]{6}$")
    background_style: BackgroundStyle = BackgroundStyle.dark
    heading_font: str = Field(default="Inter", max_length=50)
    body_font: str = Field(default="Inter", max_length=50)
    ui_font: str = Field(default="Inter", max_length=50)
    favicon_url: str = ""
    page_title: str = Field(default="", max_length=100)
    show_platform_branding: bool = True


class ThemePreset(BaseModel):
    """Named theme preset (REQ-B6, REQ-B7). Max 5 per tenant."""

    theme_id: str = Field(..., min_length=1, max_length=50, pattern=r"^[a-z0-9_-]+$")
    name: str = Field(..., min_length=1, max_length=100)
    primary_color: str = Field(default="#6366F1", pattern=r"^#[0-9a-fA-F]{6}$")
    secondary_color: str = Field(default="#8B5CF6", pattern=r"^#[0-9a-fA-F]{6}$")
    background_style: BackgroundStyle = BackgroundStyle.dark
    heading_font: str = Field(default="Inter", max_length=50)
    body_font: str = Field(default="Inter", max_length=50)
    is_default: bool = False


class ComponentEnablement(BaseModel):
    """Which DSL component types are enabled for this tenant (REQ-CM1-CM3)."""

    item_card: bool = True
    item_grid: bool = True
    item_detail: bool = True
    comparison_table: bool = True
    filter_summary: bool = True
    result_count: bool = True
    empty_state: bool = True
    action_confirm: bool = True  # Cannot be disabled (REQ-A3)
    info_banner: bool = True


class ActionConfig(BaseModel):
    """Per-action enablement and labels (REQ-A1, REQ-A2)."""

    action_id: str
    enabled: bool = True
    label: str = ""


class ActionsConfig(BaseModel):
    """Global action settings for the tenant (REQ-A1 through REQ-A5)."""

    actions: list[ActionConfig] = Field(
        default_factory=lambda: [
            ActionConfig(action_id="save_item", label="Save"),
            ActionConfig(action_id="share_item", label="Share"),
            ActionConfig(action_id="contact_enquiry", label="Contact Us"),
            ActionConfig(action_id="view_external", label="View"),
            ActionConfig(action_id="show_more", label="More Details", enabled=True),
        ]
    )
    enquiry_webhook_url: str = ""
    enquiry_email: str = ""


class PersonalizationConfig(BaseModel):
    """End-user personalization toggles (REQ-C11 through REQ-C16)."""

    allow_theme_switch: bool = False
    allow_font_switch: bool = False
    allow_bubble_style: bool = False


class TenantAdmin(BaseModel):
    """An admin user mapped to this tenant (REQ-T8)."""

    uid: str = Field(..., description="Firebase UID")
    email: str
    role: AdminRole = AdminRole.editor
    invited_at: datetime = Field(default_factory=datetime.utcnow)
    accepted: bool = False


# ---------------------------------------------------------------------------
# Root document
# ---------------------------------------------------------------------------

class Tenant(MongoBaseModel, TimestampMixin):
    """
    Root tenant document — stored in the ``tenants`` collection.

    Maps to: REQ-T1 (provisioning), REQ-T2 (isolation), REQ-T3 (status),
    REQ-T4 (defaults), REQ-T5 (limits), REQ-T8 (admins).
    """

    tenant_id: str = Field(..., min_length=1, max_length=63, pattern=r"^[a-z0-9-]+$")
    name: str = Field(..., min_length=1, max_length=200)
    slug: str = Field(
        ...,
        min_length=1,
        max_length=63,
        pattern=r"^[a-z0-9-]+$",
        description="Subdomain slug — e.g. 'acme' for acme.spectrayan.com",
    )
    catalog_label: str = Field(
        default="Products",
        max_length=50,
        description="REQ-S2: Business-chosen catalog name",
    )
    status: TenantStatus = TenantStatus.onboarding
    access_mode: AccessMode = AccessMode.public

    # Sub-documents
    limits: TenantLimits = Field(default_factory=TenantLimits)
    ai_persona: AIPersonaConfig = Field(default_factory=AIPersonaConfig)
    ai_guardrails: AIGuardrailsConfig = Field(default_factory=AIGuardrailsConfig)
    llm_provider: LLMProviderConfig = Field(default_factory=LLMProviderConfig)
    branding: BrandingConfig = Field(default_factory=BrandingConfig)
    components: ComponentEnablement = Field(default_factory=ComponentEnablement)
    actions: ActionsConfig = Field(default_factory=ActionsConfig)
    personalization: PersonalizationConfig = Field(default_factory=PersonalizationConfig)
    themes: list[ThemePreset] = Field(default_factory=list, max_length=5)

    # Admin users
    admins: list[TenantAdmin] = Field(default_factory=list)

    class Settings:
        collection = "tenants"
