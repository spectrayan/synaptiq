package com.spectrayan.synaptiq.branding.application.service;

import com.spectrayan.synaptiq.application.application.port.out.ApplicationPersistencePort;
import com.spectrayan.synaptiq.application.domain.model.Application;
import com.spectrayan.synaptiq.branding.application.port.in.BrandingQueryUseCase;
import com.spectrayan.synaptiq.shared.config.CacheNames;
import com.spectrayan.synaptiq.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Reads branding facets from the resolved Application entity.
 * Results are cached at the Application level since branding rarely changes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BrandingQueryService implements BrandingQueryUseCase {
    private final ApplicationPersistencePort applicationPersistence;

    @Override
    public Mono<Application.Branding> getBranding(String tenantId, String appId) {
        return resolveApp(tenantId, appId).map(Application::getBranding);
    }

    @Override
    public Mono<List<Application.ThemePreset>> getThemes(String tenantId, String appId) {
        return resolveApp(tenantId, appId).map(Application::getThemes);
    }

    @Override
    public Mono<Application.Personalization> getPersonalization(String tenantId, String appId) {
        return resolveApp(tenantId, appId).map(Application::getPersonalization);
    }

    private Mono<Application> resolveApp(String tenantId, String appId) {
        if (appId != null && !appId.isBlank()) {
            return findByAppIdCached(appId);
        }
        return findDefaultCached(tenantId);
    }

    @Cacheable(value = CacheNames.APPLICATIONS, key = "#appId")
    public Mono<Application> findByAppIdCached(String appId) {
        return applicationPersistence.findByAppId(appId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                "Application '" + appId + "' not found")));
    }

    @Cacheable(value = CacheNames.DEFAULT_APPLICATION, key = "#tenantId")
    public Mono<Application> findDefaultCached(String tenantId) {
        return applicationPersistence.findDefaultByTenantId(tenantId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                "No default application found for tenant '" + tenantId + "'")));
    }
}
