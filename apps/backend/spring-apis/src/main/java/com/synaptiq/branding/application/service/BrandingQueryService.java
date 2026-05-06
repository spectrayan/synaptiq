package com.synaptiq.branding.application.service;

import com.synaptiq.application.application.port.out.ApplicationPersistencePort;
import com.synaptiq.application.domain.model.Application;
import com.synaptiq.branding.application.port.in.BrandingQueryUseCase;
import com.synaptiq.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Reads branding facets from the resolved Application entity.
 */
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
            return applicationPersistence.findByAppId(appId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                    "Application '" + appId + "' not found")));
        }
        return applicationPersistence.findDefaultByTenantId(tenantId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                "No default application found for tenant '" + tenantId + "'")));
    }
}
