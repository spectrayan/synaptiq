package com.spectrayan.synaptiq.branding.application.service;

import com.spectrayan.synaptiq.application.application.port.out.ApplicationPersistencePort;
import com.spectrayan.synaptiq.application.domain.model.Application;
import com.spectrayan.synaptiq.branding.application.port.in.BrandingCommandUseCase;
import com.spectrayan.synaptiq.shared.config.CacheNames;
import com.spectrayan.synaptiq.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Updates branding facets on the resolved Application entity.
 * Evicts caches on mutation to ensure consistency.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BrandingCommandService implements BrandingCommandUseCase {
    private final ApplicationPersistencePort applicationPersistence;

    @Override
    @Caching(evict = {
        @CacheEvict(value = CacheNames.APPLICATIONS, allEntries = true),
        @CacheEvict(value = CacheNames.DEFAULT_APPLICATION, allEntries = true)
    })
    public Mono<Application.Branding> updateBranding(String tenantId, String appId, Application.Branding branding) {
        log.info("Updating branding for tenant='{}', appId='{}'", tenantId, appId);
        return resolveApp(tenantId, appId).flatMap(app -> {
            app.setBranding(branding);
            return applicationPersistence.save(app).map(Application::getBranding);
        });
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = CacheNames.APPLICATIONS, allEntries = true),
        @CacheEvict(value = CacheNames.DEFAULT_APPLICATION, allEntries = true)
    })
    public Mono<Application.Personalization> updatePersonalization(String tenantId, String appId, Application.Personalization personalization) {
        log.info("Updating personalization for tenant='{}', appId='{}'", tenantId, appId);
        return resolveApp(tenantId, appId).flatMap(app -> {
            app.setPersonalization(personalization);
            return applicationPersistence.save(app).map(Application::getPersonalization);
        });
    }

    private Mono<Application> resolveApp(String tenantId, String appId) {
        if (appId != null && !appId.isBlank()) {
            return applicationPersistence.findByAppId(appId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                    "Application '" + appId + "' not found")));
        }
        return applicationPersistence.findDefaultByTenantId(tenantId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                "No default application found for tenant '" + tenantId + "'")));
    }
}
