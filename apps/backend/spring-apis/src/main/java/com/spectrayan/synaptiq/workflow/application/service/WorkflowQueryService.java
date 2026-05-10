package com.spectrayan.synaptiq.workflow.application.service;

import com.spectrayan.synaptiq.shared.exception.ResourceNotFoundException;
import com.spectrayan.synaptiq.workflow.application.port.in.WorkflowQueryUseCase;
import com.spectrayan.synaptiq.workflow.application.port.out.WorkflowPersistencePort;
import com.spectrayan.synaptiq.workflow.domain.model.Workflow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Handles Workflow read operations (get, list).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowQueryService implements WorkflowQueryUseCase {

    private final WorkflowPersistencePort persistence;

    @Override
    public Mono<Workflow> get(String tenantId, String wid) {
        return persistence.findById(wid, tenantId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Workflow not found")));
    }

    @Override
    public Flux<Workflow> list(String tenantId, int limit) {
        return persistence.findByTenantId(tenantId, limit);
    }
}
