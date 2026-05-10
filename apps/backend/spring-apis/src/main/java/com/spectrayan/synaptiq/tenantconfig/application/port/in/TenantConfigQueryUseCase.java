package com.spectrayan.synaptiq.tenantconfig.application.port.in;

import com.spectrayan.synaptiq.application.domain.model.Application;
import reactor.core.publisher.Mono;

public interface TenantConfigQueryUseCase {
    Mono<Application.AIPersona> getAiConfig(String tenantId, String appId);
    Mono<Application.Guardrails> getGuardrails(String tenantId, String appId);
    Mono<Application.LlmProviderConfig> getLlmProvider(String tenantId, String appId);
    Mono<Application.ComponentSet> getComponents(String tenantId, String appId);
    Mono<Application.ActionsConfig> getActions(String tenantId, String appId);
}
