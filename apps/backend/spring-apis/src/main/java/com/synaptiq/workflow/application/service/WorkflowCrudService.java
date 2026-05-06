package com.synaptiq.workflow.application.service;

import com.synaptiq.agentflow.builder.models.settings.FlowSettings;
import com.synaptiq.shared.exception.ResourceNotFoundException;
import com.synaptiq.workflow.application.port.in.WorkflowCrudUseCase;
import com.synaptiq.workflow.application.port.out.WorkflowPersistencePort;
import com.synaptiq.workflow.domain.model.Workflow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class WorkflowCrudService implements WorkflowCrudUseCase {
    private final WorkflowPersistencePort persistence;

    @Override
    public Mono<Workflow> save(String tenantId, FlowSettings spec) {
        return persistence.save(Workflow.builder().tenantId(tenantId).spec(spec).build());
    }

    @Override
    public Flux<Workflow> list(String tenantId, int limit) {
        return persistence.findByTenantId(tenantId, limit);
    }

    @Override
    public Mono<Workflow> get(String tenantId, String wid) {
        return persistence.findById(wid, tenantId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Workflow not found")));
    }

    @Override
    public Mono<Workflow> update(String tenantId, String wid, FlowSettings spec) {
        return get(tenantId, wid).flatMap(w -> { w.setSpec(spec); return persistence.save(w); });
    }

    @Override
    public Mono<Void> delete(String tenantId, String wid) {
        return get(tenantId, wid).flatMap(w -> persistence.deleteById(w.getId()));
    }

    @Override
    public Mono<Workflow> duplicate(String tenantId, String wid) {
        return get(tenantId, wid).flatMap(w ->
            persistence.save(Workflow.builder().tenantId(tenantId).spec(w.getSpec()).build()));
    }
}
