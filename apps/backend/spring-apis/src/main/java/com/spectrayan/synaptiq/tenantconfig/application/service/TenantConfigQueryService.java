package com.spectrayan.synaptiq.tenantconfig.application.service;

import com.spectrayan.synaptiq.application.application.port.out.ApplicationPersistencePort;
import com.spectrayan.synaptiq.application.domain.model.Application;
import com.spectrayan.synaptiq.shared.config.CacheNames;
import com.spectrayan.synaptiq.shared.exception.ResourceNotFoundException;
import com.spectrayan.synaptiq.tenantconfig.application.port.in.TenantConfigQueryUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Reads config facets from the resolved Application entity.
 * When appId is provided, looks up by appId. Otherwise falls back to tenant's default app.
 * <p>
 * Results are cached at the Application level — config reads are very hot paths
 * (called on every chat session, every page load).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantConfigQueryService implements TenantConfigQueryUseCase {
    private final ApplicationPersistencePort applicationPersistence;

    @Override public Mono<Application.AIPersona> getAiConfig(String tid, String appId) {
        return resolveApp(tid, appId).map(Application::getAiPersona);
    }
    @Override public Mono<Application.Guardrails> getGuardrails(String tid, String appId) {
        return resolveApp(tid, appId).map(Application::getGuardrails);
    }
    @Override public Mono<Application.LlmProviderConfig> getLlmProvider(String tid, String appId) {
        return resolveApp(tid, appId).map(Application::getLlmOverride);
    }
    @Override public Mono<Application.ComponentSet> getComponents(String tid, String appId) {
        return resolveApp(tid, appId).map(Application::getComponents);
    }
    @Override public Mono<Application.ActionsConfig> getActions(String tid, String appId) {
        return resolveApp(tid, appId).map(Application::getActions);
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
