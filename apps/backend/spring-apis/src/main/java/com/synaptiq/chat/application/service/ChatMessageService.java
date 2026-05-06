package com.synaptiq.chat.application.service;

import com.synaptiq.chat.application.port.in.ChatMessageUseCase;
import com.synaptiq.shared.event.TokenUsageEvent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Instant;

@Service
public class ChatMessageService implements ChatMessageUseCase {
    private final ChatClient chatClient;
    private final ApplicationEventPublisher eventPublisher;

    public ChatMessageService(ChatClient.Builder chatClientBuilder, ApplicationEventPublisher eventPublisher) {
        this.chatClient = chatClientBuilder.build();
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Flux<String> streamMessage(String tenantId, String sessionId, String message, String modelOverride) {
        return chatClient.prompt()
                .user(message)
                .stream()
                .chatResponse()
                .doOnNext(response -> {
                    if (response.getMetadata() != null && response.getMetadata().getUsage() != null) {
                        var usage = response.getMetadata().getUsage();
                        if (usage.getPromptTokens() > 0 || usage.getCompletionTokens() > 0) {
                            eventPublisher.publishEvent(new TokenUsageEvent(
                                tenantId, sessionId,
                                "vertex-gemini",
                                usage.getPromptTokens(),
                                usage.getCompletionTokens(),
                                Instant.now()
                            ));
                        }
                    }
                })
                .map(response -> {
                    if (response.getResult() != null && response.getResult().getOutput() != null && response.getResult().getOutput().getText() != null) {
                        return response.getResult().getOutput().getText();
                    }
                    return "";
                })
                .filter(content -> !content.isEmpty());
    }
}
