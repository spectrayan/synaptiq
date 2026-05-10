package com.spectrayan.synaptiq.application.application.service;

import com.spectrayan.synaptiq.application.application.port.in.ApplicationCommandUseCase;
import com.spectrayan.synaptiq.application.application.port.out.ApplicationPersistencePort;
import com.spectrayan.synaptiq.application.domain.model.Application;
import com.spectrayan.synaptiq.application.domain.model.ApplicationStatus;
import com.spectrayan.synaptiq.shared.config.CacheNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Handles Application write operations (create, delete).
 * Evicts relevant caches on mutation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationCommandService implements ApplicationCommandUseCase {

    private final ApplicationPersistencePort persistence;

    @Override
    @Caching(evict = {
        @CacheEvict(value = CacheNames.APPLICATIONS_BY_TENANT, key = "#command.tenantId()"),
        @CacheEvict(value = CacheNames.DEFAULT_APPLICATION, key = "#command.tenantId()")
    })
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
        log.info("Creating application '{}' for tenant '{}'", app.getSlug(), command.tenantId());
        return persistence.save(app);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = CacheNames.APPLICATIONS, key = "#appId"),
        @CacheEvict(value = CacheNames.APPLICATIONS_BY_TENANT, allEntries = true),
        @CacheEvict(value = CacheNames.DEFAULT_APPLICATION, allEntries = true)
    })
    public Mono<Void> delete(String appId) {
        log.info("Deleting application '{}'", appId);
        return persistence.deleteByAppId(appId);
    }
}
