package com.synaptiq.tenant.application.port.in;

import com.synaptiq.tenant.domain.model.Tenant;
import reactor.core.publisher.Mono;

/**
 * Inbound port for creating a tenant.
 */
public interface CreateTenantUseCase {

    Mono<Tenant> createTenant(CreateTenantCommand command);

    record CreateTenantCommand(
        String tenantId,
        String name,
        String slug,
        String catalogLabel,
        String accessMode
    ) {}
}
