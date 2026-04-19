"""Synaptiq domain models — re-exports for convenience."""

from synaptiq_api.models.action_log import ActionLog, ActionOutcome
from synaptiq_api.models.auth import (
    LoginRequest,
    RefreshTokenRequest,
    SignUpRequest,
    TokenResponse,
    UserClaimsRequest,
    UserResponse,
)
from synaptiq_api.models.base import MongoBaseModel, PyObjectId, TenantScopedMixin, TimestampMixin
from synaptiq_api.models.catalog import (
    CatalogItem,
    CatalogSchema,
    FieldDesignator,
    FieldType,
    FieldVisibility,
    ItemStatus,
    SchemaField,
)
from synaptiq_api.models.saved_item import SavedItem
from synaptiq_api.models.session import ActiveFilter, ConversationTurn, MessageRole, Session
from synaptiq_api.models.tenant import (
    AccessMode,
    ActionsConfig,
    AdminRole,
    AIGuardrailsConfig,
    AIPersonaConfig,
    BackgroundStyle,
    BrandingConfig,
    ComponentEnablement,
    ExceedAction,
    LLMProviderConfig,
    LLMProviderType,
    PersonalityTone,
    PersonalizationConfig,
    Tenant,
    TenantAdmin,
    TenantLimits,
    TenantStatus,
)
from synaptiq_api.models.usage import UsageEventType, UsageLedgerEntry

__all__ = [
    # Base
    "MongoBaseModel",
    "PyObjectId",
    "TenantScopedMixin",
    "TimestampMixin",
    # Auth
    "LoginRequest",
    "RefreshTokenRequest",
    "SignUpRequest",
    "TokenResponse",
    "UserClaimsRequest",
    "UserResponse",
    # Tenant
    "AccessMode",
    "ActionsConfig",
    "AdminRole",
    "AIGuardrailsConfig",
    "AIPersonaConfig",
    "BackgroundStyle",
    "BrandingConfig",
    "ComponentEnablement",
    "ExceedAction",
    "LLMProviderConfig",
    "LLMProviderType",
    "PersonalityTone",
    "PersonalizationConfig",
    "Tenant",
    "TenantAdmin",
    "TenantLimits",
    "TenantStatus",
    # Catalog
    "CatalogItem",
    "CatalogSchema",
    "FieldDesignator",
    "FieldType",
    "FieldVisibility",
    "ItemStatus",
    "SchemaField",
    # Session
    "ActiveFilter",
    "ConversationTurn",
    "MessageRole",
    "Session",
    # Saved Item
    "SavedItem",
    # Action Log
    "ActionLog",
    "ActionOutcome",
    # Usage
    "UsageEventType",
    "UsageLedgerEntry",
]
