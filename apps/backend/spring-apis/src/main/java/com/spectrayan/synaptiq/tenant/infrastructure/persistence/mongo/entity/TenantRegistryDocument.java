package com.spectrayan.synaptiq.tenant.infrastructure.persistence.mongo.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB document for the Tenant registry.
 * <p>
 * Stored in the <b>platform registry database</b> (shared).
 * Contains only org-level concerns: identity, billing, admins, limits, and
 * the connection URI for the tenant's isolated database.
 * <p>
 * All application-specific config (persona, branding, components, actions)
 * lives in {@code ApplicationDocument} inside the tenant's own database.
 */
@Data
@Builder
@Document(collection = "tenant_registry")
public class TenantRegistryDocument {

    @Id private String id;
    @Indexed(unique = true) private String tenantId;
    private String name;
    @Indexed(unique = true) private String slug;
    @Builder.Default private String status = "ONBOARDING";
    @Builder.Default private String accessMode = "PUBLIC";
    private String displayLabel;
    private String planTier;
    private String dbConnectionUri;

    // ── Typed embedded sub-documents ─────────────────────────────────

    private TenantLimitsEmbed limits;
    private LlmProviderEmbed defaultLlmProvider;
    @Builder.Default private List<TenantAdminEmbed> admins = new ArrayList<>();

    // ── Audit ────────────────────────────────────────────────────────

    @Version private Long version;
    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;

    // ═══════════════════════════════════════════════════════════════
    //  Embedded sub-documents
    // ═══════════════════════════════════════════════════════════════

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class TenantLimitsEmbed {
        @Builder.Default private int maxApplications = 3;
        @Builder.Default private int maxMonthlyTokens = 500_000;
        @Builder.Default private int maxDataSources = 10;
        @Builder.Default private int maxUsers = 50;
        @Builder.Default private int maxRequestsPerMinute = 60;
    }

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class LlmProviderEmbed {
        private String provider;
        private String modelId;
        private String byokEncryptedKey;
        @Builder.Default private boolean isByok = false;
    }

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class TenantAdminEmbed {
        private String uid;
        private String email;
        @Builder.Default private String role = "EDITOR";
        private Instant invitedAt;
        @Builder.Default private boolean accepted = false;
    }
}
