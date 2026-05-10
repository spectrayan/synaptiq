package com.spectrayan.synaptiq.shared.config;

/**
 * Cache name constants used by {@code @Cacheable} / {@code @CacheEvict} annotations.
 * <p>
 * Centralizes cache names so they can be referenced type-safely across modules.
 * Cache configurations (TTL, max size) are defined in {@link CacheConfig}.
 */
public final class CacheNames {

    private CacheNames() {}

    /** Tenant data by tenantId — evicted on update. */
    public static final String TENANTS = "tenants";

    /** Application data by appId — evicted on update/delete. */
    public static final String APPLICATIONS = "applications";

    /** Application list by tenantId — evicted on create/delete. */
    public static final String APPLICATIONS_BY_TENANT = "applicationsByTenant";

    /** Default application per tenant — evicted on create/update. */
    public static final String DEFAULT_APPLICATION = "defaultApplication";

    /** Schema registry entries — rarely mutated. */
    public static final String SCHEMAS = "schemas";

    /** Role → scope slug resolution — cached to avoid DB hit per request. */
    public static final String ROLES = "roles";
}
