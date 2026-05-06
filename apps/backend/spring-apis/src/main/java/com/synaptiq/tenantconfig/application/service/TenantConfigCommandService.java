package com.synaptiq.tenantconfig.application.service;

import com.synaptiq.application.application.port.out.ApplicationPersistencePort;
import com.synaptiq.application.domain.model.Application;
import com.synaptiq.shared.exception.ResourceNotFoundException;
import com.synaptiq.tenantconfig.application.port.in.TenantConfigCommandUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Updates config facets on the resolved Application entity.
 * Persists changes via ApplicationPersistencePort — no Map<String,Object> bridge.
 */
@Service
@RequiredArgsConstructor
public class TenantConfigCommandService implements TenantConfigCommandUseCase {
    private final ApplicationPersistencePort applicationPersistence;

    @Override
    public Mono<Application.AIPersona> updateAiConfig(String tid, String appId, Application.AIPersona persona) {
        return resolveApp(tid, appId).flatMap(app -> {
            app.setAiPersona(persona);
            return applicationPersistence.save(app).map(Application::getAiPersona);
        });
    }

    @Override
    public Mono<Application.Guardrails> updateGuardrails(String tid, String appId, Application.Guardrails guardrails) {
        return resolveApp(tid, appId).flatMap(app -> {
            app.setGuardrails(guardrails);
            return applicationPersistence.save(app).map(Application::getGuardrails);
        });
    }

    @Override
    public Mono<Application.LlmProviderConfig> updateLlmProvider(String tid, String appId, Application.LlmProviderConfig provider) {
        return resolveApp(tid, appId).flatMap(app -> {
            app.setLlmOverride(provider);
            return applicationPersistence.save(app).map(Application::getLlmOverride);
        });
    }

    @Override
    public Mono<Application.ComponentSet> updateComponents(String tid, String appId, Application.ComponentSet components) {
        return resolveApp(tid, appId).flatMap(app -> {
            app.setComponents(components);
            return applicationPersistence.save(app).map(Application::getComponents);
        });
    }

    @Override
    public Mono<Application.ActionsConfig> updateActions(String tid, String appId, Application.ActionsConfig actions) {
        return resolveApp(tid, appId).flatMap(app -> {
            app.setActions(actions);
            return applicationPersistence.save(app).map(Application::getActions);
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
