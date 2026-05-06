package com.synaptiq.tenantconfig.application.service;

import com.synaptiq.application.application.port.out.ApplicationPersistencePort;
import com.synaptiq.application.domain.model.Application;
import com.synaptiq.shared.exception.ResourceNotFoundException;
import com.synaptiq.tenantconfig.application.port.in.TenantConfigQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Reads config facets from the resolved Application entity.
 * When appId is provided, looks up by appId. Otherwise falls back to tenant's default app.
 */
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
            return applicationPersistence.findByAppId(appId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                    "Application '" + appId + "' not found")));
        }
        return applicationPersistence.findDefaultByTenantId(tenantId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                "No default application found for tenant '" + tenantId + "'")));
    }
}
