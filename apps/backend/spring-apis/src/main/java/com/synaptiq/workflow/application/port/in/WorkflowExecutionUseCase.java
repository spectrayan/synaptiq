package com.synaptiq.workflow.application.port.in;

import com.synaptiq.workflow.domain.model.FlowEvent;
import reactor.core.publisher.Flux;

public interface WorkflowExecutionUseCase {
    Flux<FlowEvent> executeWorkflow(String tenantId, String workflowId, Object input);
}
