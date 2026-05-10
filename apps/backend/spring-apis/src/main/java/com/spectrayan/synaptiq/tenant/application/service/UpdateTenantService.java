package com.spectrayan.synaptiq.tenant.application.service;

import com.spectrayan.synaptiq.shared.config.CacheNames;
import com.spectrayan.synaptiq.shared.exception.ResourceNotFoundException;
import com.spectrayan.synaptiq.tenant.application.port.in.UpdateTenantUseCase;
import com.spectrayan.synaptiq.tenant.application.port.out.TenantPersistencePort;
import com.spectrayan.synaptiq.tenant.domain.model.AccessMode;
import com.spectrayan.synaptiq.tenant.domain.model.PlanTier;
import com.spectrayan.synaptiq.tenant.domain.model.Tenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateTenantService implements UpdateTenantUseCase {
    private final TenantPersistencePort persistencePort;

    @Override
    @CacheEvict(value = CacheNames.TENANTS, allEntries = true)
    public Mono<Tenant> updateTenant(String tenantId, UpdateTenantCommand command) {
        log.info("Updating tenant '{}'", tenantId);
        return persistencePort.findByTenantId(tenantId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Tenant '" + tenantId + "' not found")))
            .flatMap(tenant -> {
                if (command.name() != null) tenant.setName(command.name());
                if (command.slug() != null) tenant.setSlug(command.slug());
                if (command.displayLabel() != null) tenant.setDisplayLabel(command.displayLabel());
                if (command.accessMode() != null) tenant.setAccessMode(AccessMode.valueOf(command.accessMode()));
                if (command.planTier() != null) tenant.setPlanTier(PlanTier.valueOf(command.planTier()));
                if (command.dbConnectionUri() != null) tenant.setDbConnectionUri(command.dbConnectionUri());
                return persistencePort.save(tenant);
            });
    }
}
