package com.spectrayan.synaptiq.workflow.application.port.in;

import com.spectrayan.synaptiq.workflow.domain.model.ToolDefinition;
import reactor.core.publisher.Flux;

public interface WorkflowToolRegistryUseCase {
    Flux<ToolDefinition> listAvailableTools();
}
