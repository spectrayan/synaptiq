package com.synaptiq.branding.application.service;

import com.synaptiq.application.application.port.out.ApplicationPersistencePort;
import com.synaptiq.application.domain.model.Application;
import com.synaptiq.branding.application.port.in.BrandingCommandUseCase;
import com.synaptiq.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Updates branding facets on the resolved Application entity.
 */
@Service
@RequiredArgsConstructor
public class BrandingCommandService implements BrandingCommandUseCase {
    private final ApplicationPersistencePort applicationPersistence;

    @Override
    public Mono<Application.Branding> updateBranding(String tenantId, String appId, Application.Branding branding) {
        return resolveApp(tenantId, appId).flatMap(app -> {
            app.setBranding(branding);
            return applicationPersistence.save(app).map(Application::getBranding);
        });
    }

    @Override
    public Mono<Application.Personalization> updatePersonalization(String tenantId, String appId, Application.Personalization personalization) {
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
