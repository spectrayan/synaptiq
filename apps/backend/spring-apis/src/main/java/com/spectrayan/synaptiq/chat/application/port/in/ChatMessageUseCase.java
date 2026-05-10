package com.spectrayan.synaptiq.chat.application.port.in;

import reactor.core.publisher.Flux;

public interface ChatMessageUseCase {
    Flux<String> streamMessage(String tenantId, String sessionId, String message, String modelOverride);
}
