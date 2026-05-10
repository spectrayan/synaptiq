package com.spectrayan.synaptiq.workflow.application.port.in;

import com.spectrayan.synaptiq.workflow.domain.model.Workflow;
import reactor.core.publisher.Mono;

public interface WorkflowSharingUseCase {
    Mono<Workflow> share(String tenantId, String workflowId);
    Mono<Workflow> getShared(String shareToken);
}
