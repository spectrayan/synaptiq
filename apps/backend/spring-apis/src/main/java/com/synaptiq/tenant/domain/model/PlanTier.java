package com.synaptiq.tenant.domain.model;

/**
 * Subscription tier for a tenant — controls limits on applications,
 * data sources, tokens, and features.
 */
public enum PlanTier {
    FREE,
    GROWTH,
    ENTERPRISE
}
