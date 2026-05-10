package com.spectrayan.synaptiq.workflow.application.service;

import com.spectrayan.synaptiq.shared.exception.ResourceNotFoundException;
import com.spectrayan.synaptiq.workflow.application.port.in.WorkflowRunHistoryUseCase;
import com.spectrayan.synaptiq.workflow.application.port.out.WorkflowRunPersistencePort;
import com.spectrayan.synaptiq.workflow.domain.model.WorkflowRun;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class WorkflowRunHistoryService implements WorkflowRunHistoryUseCase {
    private final WorkflowRunPersistencePort runPersistence;

    @Override
    public Flux<WorkflowRun> listRuns(String tenantId, String workflowId, int limit) {
        return runPersistence.findByWorkflowId(workflowId, tenantId, limit);
    }

    @Override
    public Mono<WorkflowRun> getRunDetail(String tenantId, String runId) {
        return runPersistence.findById(runId, tenantId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Workflow run not found: " + runId)));
    }

    @Override
    public Mono<WorkflowRun> saveRun(WorkflowRun run) {
        return runPersistence.save(run);
    }
}
