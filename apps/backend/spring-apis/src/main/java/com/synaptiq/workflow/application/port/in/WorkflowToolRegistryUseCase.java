package com.synaptiq.workflow.application.port.in;

import com.synaptiq.workflow.domain.model.ToolDefinition;
import reactor.core.publisher.Flux;

public interface WorkflowToolRegistryUseCase {
    Flux<ToolDefinition> listAvailableTools();
}
