package com.spectrayan.synaptiq.integration.core;

/**
 * Naming convention for tenant-scoped Camel route IDs.
 * <p>
 * Format: {@code {tenantId}__{routeConfigId}}
 * <p>
 * This enables:
 * <ul>
 *   <li>Per-tenant route listing via prefix scan</li>
 *   <li>Tenant context extraction from any exchange</li>
 *   <li>Collision-free route IDs across tenants</li>
 * </ul>
 */
public final class TenantRouteNamingStrategy {

    public static final String SEPARATOR = "__";

    private TenantRouteNamingStrategy() {
    }

    /**
     * Build a Camel route ID from tenant and route config IDs.
     */
    public static String buildRouteId(String tenantId, String routeConfigId) {
        return tenantId + SEPARATOR + routeConfigId;
    }

    /**
     * Extract tenant ID from a Camel route ID.
     */
    public static String extractTenantId(String camelRouteId) {
        if (camelRouteId == null || !camelRouteId.contains(SEPARATOR)) {
            return null;
        }
        return camelRouteId.substring(0, camelRouteId.indexOf(SEPARATOR));
    }

    /**
     * Extract route config ID from a Camel route ID.
     */
    public static String extractRouteConfigId(String camelRouteId) {
        if (camelRouteId == null || !camelRouteId.contains(SEPARATOR)) {
            return null;
        }
        return camelRouteId.substring(camelRouteId.indexOf(SEPARATOR) + SEPARATOR.length());
    }

    /**
     * Build a direct endpoint name scoped to a tenant.
     */
    public static String directEndpoint(String tenantId, String name) {
        return "direct:" + tenantId + SEPARATOR + name;
    }
}
