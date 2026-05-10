package com.spectrayan.synaptiq.workflow.infrastructure.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spectrayan.synaptiq.shared.config.SynaptiqProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Lightweight Gemini API client using REST calls.
 * <p>
 * Resilience is handled via Reactor operators:
 * <ul>
 *   <li>{@code .timeout()} — 2-minute timeout per call</li>
 *   <li>{@code .retryWhen()} — exponential backoff on transient failures</li>
 *   <li>{@code .onErrorResume()} — graceful fallback on exhausted retries</li>
 * </ul>
 */
@Component
@Slf4j
public class GeminiClient {

    private static final Duration LLM_TIMEOUT = Duration.ofMinutes(2);
    private static final int MAX_RETRIES = 2;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public GeminiClient(
        SynaptiqProperties properties,
        ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;

        var llmConfig = properties.getLlm();
        String apiKey = llmConfig.getGeminiApiKey();
        this.model = llmConfig.getGeminiModel();

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
     * <p>
     * Includes timeout, exponential-backoff retry for transient failures,
     * and graceful error fallback.
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
            .timeout(LLM_TIMEOUT)
            .map(this::extractTextFromResponse)
            .retryWhen(Retry.backoff(MAX_RETRIES, Duration.ofSeconds(1))
                    .filter(this::isRetryable)
                    .doBeforeRetry(signal -> log.warn("[GeminiClient] Retrying (attempt {}): {}",
                            signal.totalRetries() + 1, signal.failure().getMessage())))
            .onErrorResume(e -> {
                log.error("[GeminiClient] API call failed after {} retries: {}", MAX_RETRIES, e.getMessage());
                return Mono.just("Error calling Gemini API: " + e.getMessage());
            });
    }

    // ── Response parsing ──

    private String extractTextFromResponse(String responseBody) {
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
    }

    // ── Retry filter ──

    private boolean isRetryable(Throwable t) {
        return t instanceof java.io.IOException
                || t instanceof java.net.SocketTimeoutException
                || t instanceof java.util.concurrent.TimeoutException
                || (t instanceof org.springframework.web.reactive.function.client.WebClientResponseException ex
                    && ex.getStatusCode().is5xxServerError());
    }
}
