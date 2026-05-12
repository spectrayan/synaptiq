package com.spectrayan.synaptiq.tenant.application.port.out;

import com.spectrayan.synaptiq.tenant.domain.model.Tenant;
import reactor.core.publisher.Mono;

/**
 * Outbound port for tenant persistence.
 * Returns domain models — never MongoDB documents.
 */
public interface TenantPersistencePort {

    Mono<Tenant> save(Tenant tenant);

    Mono<Tenant> findByTenantId(String tenantId);

    Mono<Tenant> findBySlug(String slug);

    Mono<Long> count();
}
