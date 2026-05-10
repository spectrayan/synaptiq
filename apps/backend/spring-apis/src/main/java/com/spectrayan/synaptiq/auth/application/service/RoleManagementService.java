package com.spectrayan.synaptiq.auth.application.service;

import com.spectrayan.synaptiq.auth.application.port.in.RoleManagementUseCase;
import com.spectrayan.synaptiq.auth.application.port.out.RolePersistencePort;
import com.spectrayan.synaptiq.auth.application.port.out.ScopePersistencePort;
import com.spectrayan.synaptiq.auth.domain.model.Role;
import com.spectrayan.synaptiq.auth.domain.model.Scope;
import com.spectrayan.synaptiq.auth.infrastructure.audit.RbacAuditLogger;
import com.spectrayan.synaptiq.auth.infrastructure.security.ScopeAuthorizationManager;
import com.spectrayan.synaptiq.shared.config.CacheNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Service handling RBAC role CRUD operations.
 * <p>
 * Enforces business rules:
 * <ul>
 *   <li>System roles cannot be updated or deleted</li>
 *   <li>Duplicate slugs are rejected</li>
 *   <li>Role changes evict the ROLES cache</li>
 *   <li>All mutations are audit-logged</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleManagementService implements RoleManagementUseCase {

    private final RolePersistencePort rolePersistence;
    private final ScopePersistencePort scopePersistence;
    private final ScopeAuthorizationManager authorizationManager;
    private final RbacAuditLogger auditLogger;

    @Override
    public Flux<Role> listRoles(String tenantId) {
        return rolePersistence.findByTenantIdOrGlobal(tenantId);
    }

    @Override
    public Mono<Role> getRole(String slug) {
        return rolePersistence.findBySlug(slug)
            .switchIfEmpty(Mono.error(
                new IllegalArgumentException("Role not found: " + slug)));
    }

    @Override
    public Mono<Role> createRole(String tenantId, String slug, String displayName,
                                  String description, Set<String> scopeSlugs) {
        return rolePersistence.existsBySlug(slug)
            .flatMap(exists -> {
                if (exists) {
                    return Mono.error(
                        new IllegalStateException("Role already exists: " + slug));
                }
                Role role = Role.builder()
                    .slug(slug)
                    .tenantId(tenantId)
                    .displayName(displayName)
                    .description(description)
                    .systemRole(false)
                    .roleType("tenant")
                    .scopeSlugs(scopeSlugs)
                    .build();
                return rolePersistence.save(role);
            })
            .doOnSuccess(r -> {
                auditLogger.logRoleMutation("CREATE", slug, "system", tenantId);
                log.info("✅ Created custom role: {}", slug);
            });
    }

    @Override
    @CacheEvict(value = CacheNames.ROLES, key = "#slug")
    public Mono<Role> updateRole(String slug, String displayName,
                                  String description, Set<String> scopeSlugs) {
        return rolePersistence.findBySlug(slug)
            .switchIfEmpty(Mono.error(
                new IllegalArgumentException("Role not found: " + slug)))
            .flatMap(existing -> {
                if (existing.isSystemRole()) {
                    return Mono.error(
                        new IllegalStateException("System roles cannot be modified"));
                }
                if (displayName != null) existing.setDisplayName(displayName);
                if (description != null) existing.setDescription(description);
                if (scopeSlugs != null && !scopeSlugs.isEmpty()) existing.setScopeSlugs(scopeSlugs);
                return rolePersistence.save(existing);
            })
            .doOnSuccess(r -> {
                auditLogger.logRoleMutation("UPDATE", slug, "system", r.getTenantId());
                log.info("✅ Updated role: {}", slug);
            });
    }

    @Override
    @CacheEvict(value = CacheNames.ROLES, key = "#slug")
    public Mono<Void> deleteRole(String slug) {
        return rolePersistence.findBySlug(slug)
            .switchIfEmpty(Mono.error(
                new IllegalArgumentException("Role not found: " + slug)))
            .flatMap(existing -> {
                if (existing.isSystemRole()) {
                    return Mono.<Void>error(
                        new IllegalStateException("System roles cannot be deleted"));
                }
                auditLogger.logRoleMutation("DELETE", slug, "system", existing.getTenantId());
                return rolePersistence.deleteBySlug(slug);
            })
            .doOnSuccess(v -> log.info("✅ Deleted role: {}", slug));
    }

    @Override
    public Flux<Scope> listScopes() {
        return scopePersistence.findAll();
    }
}
