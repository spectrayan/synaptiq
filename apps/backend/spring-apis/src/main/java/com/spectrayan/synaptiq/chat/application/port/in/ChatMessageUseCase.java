package com.spectrayan.synaptiq.chat.application.port.in;

import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatMessageUseCase {
    Flux<String> streamMessage(String tenantId, String sessionId, String message, String modelOverride);

    /**
     * Stream a chat response with optional Knowledge Base category filtering for RAG grounding.
     *
     * @param tenantId         tenant identifier
     * @param sessionId        chat session identifier
     * @param message          user message
     * @param modelOverride    optional LLM model override
     * @param knowledgeBaseIds optional list of KB category IDs to scope vector search
     */
    default Flux<String> streamMessage(String tenantId, String sessionId, String message,
                                        String modelOverride, List<String> knowledgeBaseIds) {
        // Default implementation delegates to the original method for backward compatibility
        return streamMessage(tenantId, sessionId, message, modelOverride);
    }
}
