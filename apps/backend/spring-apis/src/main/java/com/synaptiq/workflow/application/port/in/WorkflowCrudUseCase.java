package com.synaptiq.workflow.application.port.in;

import com.synaptiq.agentflow.builder.models.settings.FlowSettings;
import com.synaptiq.workflow.domain.model.Workflow;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WorkflowCrudUseCase {
    Mono<Workflow> save(String tenantId, FlowSettings spec);
    Flux<Workflow> list(String tenantId, int limit);
    Mono<Workflow> get(String tenantId, String workflowId);
    Mono<Workflow> update(String tenantId, String workflowId, FlowSettings spec);
    Mono<Void> delete(String tenantId, String workflowId);
    Mono<Workflow> duplicate(String tenantId, String workflowId);
}
