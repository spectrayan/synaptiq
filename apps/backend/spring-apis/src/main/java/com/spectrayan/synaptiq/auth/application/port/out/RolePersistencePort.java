package com.spectrayan.synaptiq.auth.application.port.out;

import com.spectrayan.synaptiq.auth.domain.model.Role;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Outbound port for Role persistence.
 */
public interface RolePersistencePort {

    Mono<Role> findBySlug(String slug);

    Flux<Role> findByTenantIdOrGlobal(String tenantId);

    Flux<Role> findAll();

    Mono<Role> save(Role role);

    Mono<Void> deleteBySlug(String slug);

    Mono<Boolean> existsBySlug(String slug);
}
