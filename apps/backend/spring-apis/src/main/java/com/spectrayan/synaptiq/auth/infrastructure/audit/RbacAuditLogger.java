package com.spectrayan.synaptiq.auth.infrastructure.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Structured audit logger for RBAC operations.
 * <p>
 * Logs role/scope changes in a structured format for compliance and debugging.
 * Future enhancement: persist audit events to MongoDB for admin dashboard query.
 */
@Slf4j
@Component
public class RbacAuditLogger {

    /**
     * Logs a role lifecycle event (create, update, delete).
     */
    public void logRoleMutation(String action, String roleSlug, String performedBy, String tenantId) {
        log.info("🔐 RBAC_AUDIT | action={} | role={} | by={} | tenant={} | ts={}",
            action, roleSlug, performedBy, tenantId, Instant.now());
    }

    /**
     * Logs an authorization decision (granted or denied).
     */
    public void logAuthzDecision(String method, String path, String requiredScope,
                                  boolean granted, String userSub) {
        if (granted) {
            log.debug("🔓 AUTHZ_GRANTED | {} {} | scope={} | user={}",
                method, path, requiredScope, userSub);
        } else {
            log.warn("🚫 AUTHZ_DENIED  | {} {} | scope={} | user={}",
                method, path, requiredScope, userSub);
        }
    }

    /**
     * Logs tenant ownership check failures.
     */
    public void logTenantViolation(String userSub, String userTenantId, String resourceTenantId, String path) {
        log.warn("🚫 TENANT_VIOLATION | user={} | userTenant={} | resourceTenant={} | path={}",
            userSub, userTenantId, resourceTenantId, path);
    }
}
