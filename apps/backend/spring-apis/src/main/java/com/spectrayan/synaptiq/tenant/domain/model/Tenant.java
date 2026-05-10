package com.spectrayan.synaptiq.tenant.domain.model;

import com.spectrayan.synaptiq.shared.domain.AggregateRoot;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate root for the Tenant bounded context.
 * <p>
 * A Tenant is the organizational/billing unit. It knows nothing about
 * application experiences — those live in {@code Application} entities
 * within the tenant's isolated database.
 * <p>
 * Pure POJO — NO framework annotations.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Tenant extends AggregateRoot {

    private String tenantId;
    private String name;
    private String slug;

    @Builder.Default private String displayLabel = "Data";
    @Builder.Default private TenantStatus status = TenantStatus.ONBOARDING;
    @Builder.Default private AccessMode accessMode = AccessMode.PUBLIC;
    @Builder.Default private PlanTier planTier = PlanTier.FREE;
    private String dbConnectionUri;

    // ── Org-level config ─────────────────────────────────────────────

    @Builder.Default private TenantLimits limits = new TenantLimits();
    @Builder.Default private LlmProvider defaultLlmProvider = new LlmProvider();
    @Builder.Default private List<TenantAdmin> admins = new ArrayList<>();

    // ═══════════════════════════════════════════════════════════════
    //  Embedded value objects (org-level only)
    // ═══════════════════════════════════════════════════════════════

    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class TenantLimits {
        @Builder.Default private int maxApplications = 3;
        @Builder.Default private int maxMonthlyTokens = 500_000;
        @Builder.Default private int maxDataSources = 10;
        @Builder.Default private int maxUsers = 50;
        @Builder.Default private int maxRequestsPerMinute = 60;
    }

    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class LlmProvider {
        @Builder.Default private String provider = "platform_managed";
        @Builder.Default private String modelId = "";
        @Builder.Default private String byokEncryptedKey = "";
        @Builder.Default private boolean isByok = false;
    }

    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class TenantAdmin {
        private String uid;
        private String email;
        @Builder.Default private AdminRole role = AdminRole.EDITOR;
        @Builder.Default private Instant invitedAt = Instant.now();
        @Builder.Default private boolean accepted = false;
    }
}
