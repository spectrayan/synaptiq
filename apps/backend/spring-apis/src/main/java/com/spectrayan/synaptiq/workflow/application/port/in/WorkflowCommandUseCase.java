package com.spectrayan.synaptiq.workflow.application.port.in;

import com.spectrayan.synaptiq.agentflow.builder.models.settings.FlowSettings;
import com.spectrayan.synaptiq.workflow.domain.model.Workflow;
import reactor.core.publisher.Mono;

/**
 * Inbound port for Workflow write operations (save, update, delete, duplicate).
 */
public interface WorkflowCommandUseCase {

    Mono<Workflow> save(String tenantId, FlowSettings spec);

    Mono<Workflow> update(String tenantId, String workflowId, FlowSettings spec);

    Mono<Void> delete(String tenantId, String workflowId);

    Mono<Workflow> duplicate(String tenantId, String workflowId);
}
