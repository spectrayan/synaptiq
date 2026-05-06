package com.synaptiq.application.infrastructure.persistence.mongo.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB document for the Application aggregate.
 * <p>
 * Stored in the per-tenant database. All config facets are typed
 * embedded documents — no {@code Map<String, Object>}.
 */
@Data
@Builder
@Document(collection = "applications")
@CompoundIndex(name = "idx_slug", def = "{'slug': 1}", unique = true)
public class ApplicationDocument {

    @Id private String id;
    @Indexed(unique = true) private String appId;
    @Indexed private String tenantId;
    private String slug;
    private String name;
    private String description;
    private String icon;
    @Builder.Default private boolean isDefault = false;
    @Builder.Default private String status = "DRAFT";

    // ── Typed embedded config documents ──────────────────────────────

    private AIPersonaEmbed aiPersona;
    private GuardrailsEmbed guardrails;
    private BrandingEmbed branding;
    private ComponentSetEmbed components;
    private ActionsConfigEmbed actions;
    private PersonalizationEmbed personalization;
    @Builder.Default private List<ThemePresetEmbed> themes = new ArrayList<>();

    // ── Data source references ───────────────────────────────────────

    @Builder.Default private List<String> dataSourceIds = new ArrayList<>();

    // ── LLM override ─────────────────────────────────────────────────

    private LlmProviderEmbed llmOverride;

    // ── Audit ────────────────────────────────────────────────────────

    @Version private Long version;
    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;

    // ═══════════════════════════════════════════════════════════════
    //  Embedded sub-documents
    // ═══════════════════════════════════════════════════════════════

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class AIPersonaEmbed {
        private String displayName;
        private String tone;
        private String customInstruction;
        private String welcomeMessage;
        @Builder.Default private List<String> starterPrompts = new ArrayList<>();
    }

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class GuardrailsEmbed {
        private String outOfScopeMessage;
        @Builder.Default private boolean recommendationMode = true;
        private String language;
    }

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class BrandingEmbed {
        private String logoUrl;
        private String primaryColor;
        private String secondaryColor;
        private String backgroundStyle;
        private String headingFont;
        private String bodyFont;
        private String faviconUrl;
        private String pageTitle;
        @Builder.Default private boolean showPlatformBranding = true;
    }

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class ComponentSetEmbed {
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
        @Builder.Default private boolean kpiCard = true;
        @Builder.Default private boolean chart = true;
        @Builder.Default private boolean statGrid = true;
        @Builder.Default private boolean kanban = true;
        @Builder.Default private boolean timeline = true;
        @Builder.Default private boolean metricTable = true;
        @Builder.Default private boolean progressTracker = true;
        @Builder.Default private boolean launchpad = true;
    }

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class ActionsConfigEmbed {
        @Builder.Default private List<ActionEntryEmbed> actions = new ArrayList<>();
        private String enquiryWebhookUrl;
        private String enquiryEmail;
    }

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class ActionEntryEmbed {
        private String actionId;
        @Builder.Default private boolean enabled = true;
        private String label;
    }

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class PersonalizationEmbed {
        @Builder.Default private boolean allowThemeSwitch = false;
        @Builder.Default private boolean allowFontSwitch = false;
        @Builder.Default private boolean allowBubbleStyle = false;
    }

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class ThemePresetEmbed {
        private String themeId;
        private String name;
        private String primaryColor;
        private String secondaryColor;
        private String backgroundStyle;
        @Builder.Default private boolean isDefault = false;
    }

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class LlmProviderEmbed {
        private String provider;
        private String modelId;
        private String byokEncryptedKey;
        @Builder.Default private boolean isByok = false;
    }
}
