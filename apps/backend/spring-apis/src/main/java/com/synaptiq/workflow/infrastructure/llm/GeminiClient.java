package com.synaptiq.workflow.infrastructure.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Lightweight Gemini API client using REST calls.
 * Uses the Gemini API key from configuration (synaptiq.llm.gemini-api-key).
 */
@Component
@Slf4j
public class GeminiClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public GeminiClient(
        @Value("${synaptiq.llm.gemini-api-key:}") String apiKey,
        @Value("${synaptiq.llm.gemini-model:gemini-2.5-flash}") String model,
        ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
        this.model = model;

        this.webClient = WebClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com/v1beta")
            .defaultHeader("x-goog-api-key", apiKey)
            .defaultHeader("Content-Type", "application/json")
            .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
            .build();

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("[GeminiClient] No API key configured — LLM calls will fail. Set synaptiq.llm.gemini-api-key");
        } else {
            log.info("[GeminiClient] Initialized with model={}", model);
        }
    }

    /**
     * Send a prompt to Gemini and return the text response.
     */
    public Mono<String> generateContent(String systemPrompt, String userPrompt) {
        Map<String, Object> body = Map.of(
            "contents", List.of(
                Map.of("role", "user", "parts", List.of(Map.of("text", userPrompt)))
            ),
            "systemInstruction", Map.of(
                "parts", List.of(Map.of("text", systemPrompt))
            ),
            "generationConfig", Map.of(
                "temperature", 0.7,
                "maxOutputTokens", 4096
            )
        );

        return webClient.post()
            .uri("/models/{model}:generateContent", model)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(String.class)
            .map(responseBody -> {
                try {
                    JsonNode response = objectMapper.readTree(responseBody);
                    JsonNode candidates = response.path("candidates");
                    if (candidates.isArray() && !candidates.isEmpty()) {
                        JsonNode parts = candidates.get(0).path("content").path("parts");
                        if (parts.isArray() && !parts.isEmpty()) {
                            return parts.get(0).path("text").asText("");
                        }
                    }
                    log.warn("[GeminiClient] Unexpected response structure: {}", responseBody);
                    return "Error: Unexpected response from Gemini";
                } catch (Exception e) {
                    log.error("[GeminiClient] Failed to parse response", e);
                    return "Error: " + e.getMessage();
                }
            })
            .onErrorResume(e -> {
                log.error("[GeminiClient] API call failed", e);
                return Mono.just("Error calling Gemini API: " + e.getMessage());
            });
    }
}
