package com.spectrayan.synaptiq.integration.tenant;

import com.spectrayan.synaptiq.integration.autoconfigure.CamelIntegrationProperties;
import com.spectrayan.synaptiq.integration.core.CamelEngineManager;
import com.spectrayan.synaptiq.integration.core.TenantRouteNamingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Enforces per-tenant limits on active routes and execution quotas.
 */
@Slf4j
@RequiredArgsConstructor
public class TenantRateLimitPolicy {

    private final CamelEngineManager engineManager;
    private final CamelIntegrationProperties properties;

    /**
     * Check whether a tenant can add another route.
     */
    public boolean canAddRoute(String tenantId) {
        int current = engineManager.getRouteIdsForTenant(tenantId).size();
        int max = properties.getTenant().getMaxRoutesPerTenant();
        if (current >= max) {
            log.warn("Tenant {} has reached max route limit ({}/{})", tenantId, current, max);
            return false;
        }
        return true;
    }

    /**
     * Get current route count for a tenant.
     */
    public int getActiveRouteCount(String tenantId) {
        return engineManager.getRouteIdsForTenant(tenantId).size();
    }
}
