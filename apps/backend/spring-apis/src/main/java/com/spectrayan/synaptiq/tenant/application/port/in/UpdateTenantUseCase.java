package com.spectrayan.synaptiq.tenant.application.port.in;

import com.spectrayan.synaptiq.tenant.domain.model.Tenant;
import reactor.core.publisher.Mono;

/**
 * Inbound port for updating tenant-level (org-level) fields.
 */
public interface UpdateTenantUseCase {

    Mono<Tenant> updateTenant(String tenantId, UpdateTenantCommand command);

    record UpdateTenantCommand(
        String name,
        String slug,
        String displayLabel,
        String accessMode,
        String planTier,
        String dbConnectionUri
    ) {}
}
