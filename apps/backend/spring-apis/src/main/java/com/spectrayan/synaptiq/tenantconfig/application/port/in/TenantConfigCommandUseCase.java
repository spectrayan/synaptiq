package com.spectrayan.synaptiq.tenantconfig.application.port.in;

import com.spectrayan.synaptiq.application.domain.model.Application;
import reactor.core.publisher.Mono;

public interface TenantConfigCommandUseCase {
    Mono<Application.AIPersona> updateAiConfig(String tenantId, String appId, Application.AIPersona persona);
    Mono<Application.Guardrails> updateGuardrails(String tenantId, String appId, Application.Guardrails guardrails);
    Mono<Application.LlmProviderConfig> updateLlmProvider(String tenantId, String appId, Application.LlmProviderConfig provider);
    Mono<Application.ComponentSet> updateComponents(String tenantId, String appId, Application.ComponentSet components);
    Mono<Application.ActionsConfig> updateActions(String tenantId, String appId, Application.ActionsConfig actions);
}
