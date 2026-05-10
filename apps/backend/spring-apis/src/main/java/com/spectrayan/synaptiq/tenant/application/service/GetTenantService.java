package com.spectrayan.synaptiq.tenant.application.service;

import com.spectrayan.synaptiq.shared.config.CacheNames;
import com.spectrayan.synaptiq.shared.exception.ResourceNotFoundException;
import com.spectrayan.synaptiq.tenant.application.port.in.GetTenantUseCase;
import com.spectrayan.synaptiq.tenant.application.port.out.TenantPersistencePort;
import com.spectrayan.synaptiq.tenant.domain.model.Tenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetTenantService implements GetTenantUseCase {
    private final TenantPersistencePort persistencePort;

    @Override
    @Cacheable(value = CacheNames.TENANTS, key = "#tenantId")
    public Mono<Tenant> getByTenantId(String tenantId) {
        return persistencePort.findByTenantId(tenantId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Tenant '" + tenantId + "' not found")));
    }

    @Override
    @Cacheable(value = CacheNames.TENANTS, key = "'slug:' + #slug")
    public Mono<Tenant> getBySlug(String slug) {
        return persistencePort.findBySlug(slug)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Tenant with slug '" + slug + "' not found")));
    }
}
