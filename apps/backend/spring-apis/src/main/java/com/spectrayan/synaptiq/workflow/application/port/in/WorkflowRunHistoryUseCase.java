package com.spectrayan.synaptiq.workflow.application.port.in;

import com.spectrayan.synaptiq.workflow.domain.model.WorkflowRun;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WorkflowRunHistoryUseCase {
    Flux<WorkflowRun> listRuns(String tenantId, String workflowId, int limit);
    Mono<WorkflowRun> getRunDetail(String tenantId, String runId);
    Mono<WorkflowRun> saveRun(WorkflowRun run);
}
