package com.spectrayan.synaptiq.auth.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Validates that the authenticated user's tenant matches the requested resource's tenant.
 * <p>
 * Used by domain services to enforce data isolation. The ScopeAuthorizationManager
 * handles <em>permission</em> checks; this handles <em>ownership</em> checks.
 * <p>
 * Usage in a service:
 * <pre>{@code
 * tenantGuard.assertOwnership(authentication, resource.getTenantId())
 *     .then(doBusinessLogic());
 * }</pre>
 */
@Slf4j
@Component
public class TenantGuard {

    /**
     * Checks if the authenticated user belongs to the given tenant.
     * Super-admins (with "*" scope) bypass tenant checks.
     *
     * @param auth     the current authentication
     * @param tenantId the tenant ID to validate against
     * @return true if the user owns the resource or is super-admin
     */
    public boolean isOwner(Authentication auth, String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return true; // Global resource — no tenant restriction
        }

        // Super-admin bypasses tenant checks
        if (hasAuthority(auth, "*")) {
            return true;
        }

        // Check if user has the matching TENANT_ authority
        String required = "TENANT_" + tenantId;
        if (hasAuthority(auth, required)) {
            return true;
        }

        // Fallback: check JWT tenantId claim directly
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String userTenantId = jwt.getClaimAsString("tenantId");
            if (tenantId.equals(userTenantId)) {
                return true;
            }
        }

        log.warn("🚫 Tenant ownership check failed — user does not belong to tenant: {}", tenantId);
        return false;
    }

    /**
     * Extracts the user's tenant ID from the authentication token.
     */
    public String extractTenantId(Authentication auth) {
        // First check TENANT_ authority
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if (ga.getAuthority().startsWith("TENANT_")) {
                return ga.getAuthority().substring("TENANT_".length());
            }
        }

        // Fallback to JWT claim
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getClaimAsString("tenantId");
        }

        return null;
    }

    private boolean hasAuthority(Authentication auth, String authority) {
        return auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals(authority));
    }
}
