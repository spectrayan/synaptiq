package com.synaptiq.tenant.application.service;

import com.synaptiq.shared.exception.ResourceNotFoundException;
import com.synaptiq.tenant.application.port.in.UpdateTenantUseCase;
import com.synaptiq.tenant.application.port.out.TenantPersistencePort;
import com.synaptiq.tenant.domain.model.AccessMode;
import com.synaptiq.tenant.domain.model.PlanTier;
import com.synaptiq.tenant.domain.model.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UpdateTenantService implements UpdateTenantUseCase {
    private final TenantPersistencePort persistencePort;

    @Override
    public Mono<Tenant> updateTenant(String tenantId, UpdateTenantCommand command) {
        return persistencePort.findByTenantId(tenantId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Tenant '" + tenantId + "' not found")))
            .flatMap(tenant -> {
                if (command.name() != null) tenant.setName(command.name());
                if (command.slug() != null) tenant.setSlug(command.slug());
                if (command.catalogLabel() != null) tenant.setCatalogLabel(command.catalogLabel());
                if (command.accessMode() != null) tenant.setAccessMode(AccessMode.valueOf(command.accessMode()));
                if (command.planTier() != null) tenant.setPlanTier(PlanTier.valueOf(command.planTier()));
                if (command.dbConnectionUri() != null) tenant.setDbConnectionUri(command.dbConnectionUri());
                return persistencePort.save(tenant);
            });
    }
}
