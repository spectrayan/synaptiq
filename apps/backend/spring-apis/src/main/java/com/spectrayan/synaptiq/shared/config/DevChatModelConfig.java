package com.spectrayan.synaptiq.shared.config;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Dev-profile fallback beans — only active when Ollama is NOT configured.
 *
 * <p>When {@code spring.ai.ollama.embedding.options.model} is set (the default in
 * application-dev.yml), Ollama auto-configuration provides real ChatModel and
 * EmbeddingModel beans, so this config is skipped entirely.</p>
 *
 * <p>To run without Ollama, set {@code spring.ai.ollama.embedding.options.model=}
 * (empty) in your local config, and this no-op fallback will activate.</p>
 */
@Configuration
@Profile("dev")
@ConditionalOnProperty(
    name = "spring.ai.ollama.embedding.options.model",
    havingValue = "none",
    matchIfMissing = false
)
public class DevChatModelConfig {

    @Bean
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

            @Override
            public Flux<ChatResponse> stream(Prompt prompt) {
                return Flux.just(call(prompt));
            }
        };
    }

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(
        org.springframework.ai.embedding.EmbeddingModel.class
    )
    public org.springframework.ai.embedding.EmbeddingModel devEmbeddingModel() {
        return new org.springframework.ai.embedding.AbstractEmbeddingModel() {
            @Override
            public org.springframework.ai.embedding.EmbeddingResponse call(
                    org.springframework.ai.embedding.EmbeddingRequest request) {
                var results = new java.util.ArrayList<org.springframework.ai.embedding.Embedding>();
                for (int i = 0; i < request.getInstructions().size(); i++) {
                    results.add(new org.springframework.ai.embedding.Embedding(new float[768], i));
                }
                return new org.springframework.ai.embedding.EmbeddingResponse(results);
            }

            @Override
            public float[] embed(org.springframework.ai.document.Document document) {
                return new float[768];
            }
        };
    }

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(
        org.springframework.web.reactive.function.client.WebClient.Builder.class
    )
    public org.springframework.web.reactive.function.client.WebClient.Builder webClientBuilder() {
        return org.springframework.web.reactive.function.client.WebClient.builder();
    }
}
