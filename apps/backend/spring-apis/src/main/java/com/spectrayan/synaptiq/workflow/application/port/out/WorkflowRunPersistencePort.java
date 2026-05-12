package com.spectrayan.synaptiq.workflow.application.port.out;

import com.spectrayan.synaptiq.workflow.domain.model.WorkflowRun;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WorkflowRunPersistencePort {
    Mono<WorkflowRun> save(WorkflowRun run);
    Mono<WorkflowRun> findById(String runId, String tenantId);
    Flux<WorkflowRun> findByWorkflowId(String workflowId, String tenantId, int limit);
}
