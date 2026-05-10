package com.spectrayan.synaptiq.workflow.application.port.in;

import reactor.core.publisher.Mono;

/**
 * Regenerates / improves a single agent node's system prompt via LLM.
 */
public interface PromptRegenerationUseCase {
    Mono<String> regeneratePrompt(
        String tenantId,
        String nodeId,
        String nodeLabel,
        String nodeDescription,
        String currentPrompt,
        String instruction,
        Object workflowContext
    );
}
