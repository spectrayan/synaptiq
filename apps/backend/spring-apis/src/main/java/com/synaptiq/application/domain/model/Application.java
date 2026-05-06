package com.synaptiq.application.domain.model;

import com.synaptiq.shared.domain.AggregateRoot;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate root for the Application bounded context.
 * <p>
 * An Application is a deployable conversational experience — a tenant
 * can have N applications (sales dashboard, HR onboarding, product finder, etc.),
 * each with its own persona, branding, data sources, and component set.
 * <p>
 * Pure POJO — NO framework annotations.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Application extends AggregateRoot {

    private String appId;
    private String tenantId;
    private String slug;
    private String name;
    private String description;
    private String icon;

    @Builder.Default private boolean isDefault = false;
    @Builder.Default private ApplicationStatus status = ApplicationStatus.DRAFT;

    // ── Config value objects ──────────────────────────────────────────

    @Builder.Default private AIPersona aiPersona = new AIPersona();
    @Builder.Default private Guardrails guardrails = new Guardrails();
    @Builder.Default private Branding branding = new Branding();
    @Builder.Default private ComponentSet components = new ComponentSet();
    @Builder.Default private ActionsConfig actions = new ActionsConfig();
    @Builder.Default private Personalization personalization = new Personalization();
    @Builder.Default private List<ThemePreset> themes = new ArrayList<>();

    // ── Data source references ───────────────────────────────────────

    /** IDs of DataSource entities this application reads from. */
    @Builder.Default private List<String> dataSourceIds = new ArrayList<>();

    // ── LLM override (optional — falls back to tenant default) ───────

    private LlmProviderConfig llmOverride;

    // ═══════════════════════════════════════════════════════════════
    //  Embedded value objects
    // ═══════════════════════════════════════════════════════════════

    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class AIPersona {
        @Builder.Default private String displayName = "Synaptiq";
        @Builder.Default private String tone = "professional";
        @Builder.Default private String customInstruction = "";
        @Builder.Default private String welcomeMessage = "Hi! How can I help you today?";
        @Builder.Default private List<String> starterPrompts = new ArrayList<>();
    }

    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class Guardrails {
        @Builder.Default private String outOfScopeMessage = "I can only help with topics related to this application.";
        @Builder.Default private boolean recommendationMode = true;
        @Builder.Default private String language = "en";
    }

    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class Branding {
        @Builder.Default private String logoUrl = "";
        @Builder.Default private String primaryColor = "#6366F1";
        @Builder.Default private String secondaryColor = "#8B5CF6";
        @Builder.Default private String backgroundStyle = "dark";
        @Builder.Default private String headingFont = "Inter";
        @Builder.Default private String bodyFont = "Inter";
        @Builder.Default private String faviconUrl = "";
        @Builder.Default private String pageTitle = "";
        @Builder.Default private boolean showPlatformBranding = true;
    }

    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class ComponentSet {
        // ── Catalog-specific components ──
        @Builder.Default private boolean itemCard = true;
        @Builder.Default private boolean itemGrid = true;
        @Builder.Default private boolean itemDetail = true;
        @Builder.Default private boolean comparisonTable = true;
        @Builder.Default private boolean filterSummary = true;
        @Builder.Default private boolean resultCount = true;
        @Builder.Default private boolean emptyState = true;
        @Builder.Default private boolean actionConfirm = true;
        @Builder.Default private boolean infoBanner = true;
        @Builder.Default private boolean formInput = true;
        @Builder.Default private boolean dataTable = true;
        // ── Universal/platform components ──
        @Builder.Default private boolean kpiCard = true;
        @Builder.Default private boolean chart = true;
        @Builder.Default private boolean statGrid = true;
        @Builder.Default private boolean kanban = true;
        @Builder.Default private boolean timeline = true;
        @Builder.Default private boolean metricTable = true;
        @Builder.Default private boolean progressTracker = true;
        @Builder.Default private boolean launchpad = true;
    }

    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class ActionsConfig {
        @Builder.Default private List<ActionEntry> actions = new ArrayList<>();
        @Builder.Default private String enquiryWebhookUrl = "";
        @Builder.Default private String enquiryEmail = "";
    }

    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class ActionEntry {
        private String actionId;
        @Builder.Default private boolean enabled = true;
        @Builder.Default private String label = "";
    }

    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class Personalization {
        @Builder.Default private boolean allowThemeSwitch = false;
        @Builder.Default private boolean allowFontSwitch = false;
        @Builder.Default private boolean allowBubbleStyle = false;
    }

    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class ThemePreset {
        private String themeId;
        private String name;
        @Builder.Default private String primaryColor = "#6366F1";
        @Builder.Default private String secondaryColor = "#8B5CF6";
        @Builder.Default private String backgroundStyle = "dark";
        @Builder.Default private boolean isDefault = false;
    }

    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class LlmProviderConfig {
        @Builder.Default private String provider = "platform_managed";
        @Builder.Default private String modelId = "";
        @Builder.Default private String byokEncryptedKey = "";
        @Builder.Default private boolean isByok = false;
    }
}
