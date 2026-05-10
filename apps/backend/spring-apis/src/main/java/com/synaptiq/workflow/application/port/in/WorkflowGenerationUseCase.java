package com.synaptiq.workflow.application.port.in;

import com.synaptiq.workflow.domain.model.FlowEvent;
import reactor.core.publisher.Flux;

/**
 * Generates a workflow spec from a natural-language prompt.
 */
public interface WorkflowGenerationUseCase {
    Flux<FlowEvent> generateWorkflow(String tenantId, String prompt);
}
