package com.spectrayan.synaptiq.workflow.application.port.out;

import com.spectrayan.synaptiq.workflow.domain.model.Workflow;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WorkflowPersistencePort {
    Mono<Workflow> save(Workflow workflow);
    Mono<Workflow> findById(String id, String tenantId);
    Flux<Workflow> findByTenantId(String tenantId, int limit);
    Mono<Void> deleteById(String id);
    Mono<Workflow> findByShareToken(String shareToken);
}
