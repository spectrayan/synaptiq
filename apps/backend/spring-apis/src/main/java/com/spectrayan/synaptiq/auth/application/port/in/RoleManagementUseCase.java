package com.spectrayan.synaptiq.auth.application.port.in;

import com.spectrayan.synaptiq.auth.domain.model.Role;
import com.spectrayan.synaptiq.auth.domain.model.Scope;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Input port for RBAC role management.
 */
public interface RoleManagementUseCase {

    Flux<Role> listRoles(String tenantId);

    Mono<Role> getRole(String slug);

    Mono<Role> createRole(String tenantId, String slug, String displayName,
                           String description, java.util.Set<String> scopeSlugs);

    Mono<Role> updateRole(String slug, String displayName,
                           String description, java.util.Set<String> scopeSlugs);

    Mono<Void> deleteRole(String slug);

    Flux<Scope> listScopes();
}
