package com.synaptiq.tenant.application.service;

import com.synaptiq.application.application.port.out.ApplicationPersistencePort;
import com.synaptiq.application.domain.model.Application;
import com.synaptiq.application.domain.model.ApplicationStatus;
import com.synaptiq.shared.exception.DuplicateResourceException;
import com.synaptiq.tenant.application.port.in.CreateTenantUseCase;
import com.synaptiq.tenant.application.port.out.TenantPersistencePort;
import com.synaptiq.tenant.domain.model.AccessMode;
import com.synaptiq.tenant.domain.model.Tenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Creates a tenant and auto-provisions a default Application.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateTenantService implements CreateTenantUseCase {
    private final TenantPersistencePort persistencePort;
    private final ApplicationPersistencePort applicationPersistence;

    @Override
    public Mono<Tenant> createTenant(CreateTenantCommand command) {
        return persistencePort.findByTenantId(command.tenantId())
            .flatMap(existing -> Mono.<Tenant>error(
                new DuplicateResourceException("Tenant '" + command.tenantId() + "' already exists")
            ))
            .switchIfEmpty(Mono.defer(() -> {
                var tenant = Tenant.builder()
                    .tenantId(command.tenantId())
                    .name(command.name())
                    .slug(command.slug())
                    .catalogLabel(command.catalogLabel() != null ? command.catalogLabel() : "Products")
                    .accessMode(command.accessMode() != null ? AccessMode.valueOf(command.accessMode()) : AccessMode.PUBLIC)
                    .build();
                return persistencePort.save(tenant)
                    .flatMap(savedTenant -> provisionDefaultApplication(savedTenant)
                        .thenReturn(savedTenant));
            }))
            .doOnSuccess(t -> log.info("Created tenant '{}' with default application", t.getTenantId()));
    }

    /**
     * Auto-provisions a default Application for a newly created tenant.
     * The default app inherits the tenant's name and gets a slug derived from the tenant slug.
     */
    private Mono<Application> provisionDefaultApplication(Tenant tenant) {
        var defaultApp = Application.builder()
            .appId(UUID.randomUUID().toString())
            .tenantId(tenant.getTenantId())
            .slug(tenant.getSlug() + "-default")
            .name(tenant.getName())
            .description("Default application for " + tenant.getName())
            .isDefault(true)
            .status(ApplicationStatus.ACTIVE)
            .build();
        return applicationPersistence.save(defaultApp)
            .doOnSuccess(app -> log.info("Provisioned default application '{}' for tenant '{}'",
                app.getAppId(), tenant.getTenantId()));
    }
}
