package com.synaptiq.tenant.application.service;

import com.synaptiq.shared.exception.ResourceNotFoundException;
import com.synaptiq.tenant.application.port.in.GetTenantUseCase;
import com.synaptiq.tenant.application.port.out.TenantPersistencePort;
import com.synaptiq.tenant.domain.model.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetTenantService implements GetTenantUseCase {
    private final TenantPersistencePort persistencePort;

    @Override
    public Mono<Tenant> getByTenantId(String tenantId) {
        return persistencePort.findByTenantId(tenantId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Tenant '" + tenantId + "' not found")));
    }

    @Override
    public Mono<Tenant> getBySlug(String slug) {
        return persistencePort.findBySlug(slug)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Tenant with slug '" + slug + "' not found")));
    }
}
