package com.synaptiq.application.application.service;

import com.synaptiq.application.application.port.in.ApplicationCrudUseCase;
import com.synaptiq.application.application.port.out.ApplicationPersistencePort;
import com.synaptiq.application.domain.model.Application;
import com.synaptiq.application.domain.model.ApplicationStatus;
import com.synaptiq.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Application service for CRUD operations.
 * Delegates persistence to the outbound port.
 */
@Service
@RequiredArgsConstructor
public class ApplicationCrudService implements ApplicationCrudUseCase {

    private final ApplicationPersistencePort persistence;

    @Override
    public Mono<Application> create(CreateApplicationCommand command) {
        var app = Application.builder()
            .appId(UUID.randomUUID().toString())
            .tenantId(command.tenantId())
            .name(command.name())
            .slug(command.slug())
            .description(command.description())
            .icon(command.icon())
            .isDefault(command.isDefault())
            .status(ApplicationStatus.DRAFT)
            .build();
        return persistence.save(app);
    }

    @Override
    public Mono<Application> getByAppId(String appId) {
        return persistence.findByAppId(appId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                "Application '" + appId + "' not found")));
    }

    @Override
    public Flux<Application> listByTenantId(String tenantId) {
        return persistence.findAllByTenantId(tenantId);
    }

    @Override
    public Mono<Void> delete(String appId) {
        return persistence.deleteByAppId(appId);
    }
}
