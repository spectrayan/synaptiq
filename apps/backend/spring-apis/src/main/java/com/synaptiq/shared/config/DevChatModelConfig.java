package com.synaptiq.shared.config;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Provides a no-op ChatModel for local development when no LLM credentials
 * are configured. This prevents the Spring AI autoconfiguration from failing
 * and lets all non-AI endpoints work normally.
 */
@Configuration
@Profile("dev")
public class DevChatModelConfig {

    @Bean
    @ConditionalOnMissingBean(ChatModel.class)
    public ChatModel devChatModel() {
        return new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                return new ChatResponse(
                    java.util.List.of(new Generation(
                        new AssistantMessage("⚠️ LLM not configured — running in dev mode without AI credentials.")
                    ))
                );
            }
        };
    }
}
