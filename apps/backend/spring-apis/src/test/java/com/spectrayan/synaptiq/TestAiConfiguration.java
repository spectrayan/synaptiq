package com.spectrayan.synaptiq;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * Test configuration providing mock AI model beans.
 * In CI, Vertex AI credentials are not available, so we provide
 * no-op implementations to allow the application context to load.
 */
@TestConfiguration
public class TestAiConfiguration {

    @Bean
    @Primary
    public ChatModel testChatModel() {
        return new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                throw new UnsupportedOperationException("Test stub — no real AI backend");
            }
        };
    }

    @Bean
    @Primary
    public EmbeddingModel testEmbeddingModel() {
        return new EmbeddingModel() {
            @Override
            public EmbeddingResponse call(org.springframework.ai.embedding.EmbeddingRequest request) {
                throw new UnsupportedOperationException("Test stub — no real AI backend");
            }

            @Override
            public float[] embed(Document document) {
                throw new UnsupportedOperationException("Test stub — no real AI backend");
            }
        };
    }
}
