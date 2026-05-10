package com.spectrayan.synaptiq.workflow.application.service;

import com.spectrayan.synaptiq.agentflow.builder.models.settings.FlowSettings;
import com.spectrayan.synaptiq.shared.exception.ResourceNotFoundException;
import com.spectrayan.synaptiq.workflow.application.port.in.WorkflowCommandUseCase;
import com.spectrayan.synaptiq.workflow.application.port.in.WorkflowQueryUseCase;
import com.spectrayan.synaptiq.workflow.application.port.out.WorkflowPersistencePort;
import com.spectrayan.synaptiq.workflow.domain.model.Workflow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Handles Workflow write operations (save, update, delete, duplicate).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowCommandService implements WorkflowCommandUseCase {

    private final WorkflowPersistencePort persistence;
    private final WorkflowQueryUseCase workflowQuery;

    @Override
    public Mono<Workflow> save(String tenantId, FlowSettings spec) {
        log.info("Saving workflow for tenant '{}'", tenantId);
        return persistence.save(Workflow.builder().tenantId(tenantId).spec(spec).build());
    }

    @Override
    public Mono<Workflow> update(String tenantId, String wid, FlowSettings spec) {
        return workflowQuery.get(tenantId, wid).flatMap(w -> {
            w.setSpec(spec);
            return persistence.save(w);
        });
    }

    @Override
    public Mono<Void> delete(String tenantId, String wid) {
        log.info("Deleting workflow '{}' for tenant '{}'", wid, tenantId);
        return workflowQuery.get(tenantId, wid).flatMap(w -> persistence.deleteById(w.getId()));
    }

    @Override
    public Mono<Workflow> duplicate(String tenantId, String wid) {
        return workflowQuery.get(tenantId, wid).flatMap(w ->
            persistence.save(Workflow.builder().tenantId(tenantId).spec(w.getSpec()).build()));
    }
}
