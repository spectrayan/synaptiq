package com.spectrayan.synaptiq.tenant.application.port.in;

import com.spectrayan.synaptiq.tenant.domain.model.Tenant;
import reactor.core.publisher.Mono;

/**
 * Inbound port for tenant queries.
 */
public interface GetTenantUseCase {

    Mono<Tenant> getByTenantId(String tenantId);

    Mono<Tenant> getBySlug(String slug);
}
