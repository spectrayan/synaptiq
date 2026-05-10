package com.spectrayan.synaptiq.workflow.application.port.in;

import com.spectrayan.synaptiq.workflow.domain.model.Workflow;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Inbound port for Workflow read operations (get, list).
 */
public interface WorkflowQueryUseCase {

    Mono<Workflow> get(String tenantId, String workflowId);

    Flux<Workflow> list(String tenantId, int limit);
}
