package com.spectrayan.synaptiq.workflow.infrastructure.llm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Gemini LLM client backed by Spring AI {@link ChatClient}.
 * <p>
 * Uses the auto-configured {@code GoogleGenAiChatModel} which talks to
 * the Gemini Developer API via API-key authentication.
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

    private final ChatClient chatClient;

    public GeminiClient(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        log.info("[GeminiClient] Initialized with Spring AI ChatClient");
    }

    /**
     * Send a prompt to Gemini and return the text response.
     * <p>
     * Includes timeout, exponential-backoff retry for transient failures,
     * and graceful error fallback.
     */
    public Mono<String> generateContent(String systemPrompt, String userPrompt) {
        return Mono.<String>fromCallable(() ->
                chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .options(ChatOptions.builder()
                        .temperature(0.7)
                        .build())
                    .call()
                    .content()
            )
            .timeout(LLM_TIMEOUT)
            .retryWhen(Retry.backoff(MAX_RETRIES, Duration.ofSeconds(1))
                .filter(this::isRetryable)
                .doBeforeRetry(signal -> log.warn("[GeminiClient] Retrying (attempt {}): {}",
                    signal.totalRetries() + 1, signal.failure().getMessage())))
            .onErrorResume(e -> {
                log.error("[GeminiClient] API call failed after {} retries: {}", MAX_RETRIES, e.getMessage());
                return Mono.just("Error calling Gemini API: " + e.getMessage());
            });
    }

    // ── Retry filter ──

    private boolean isRetryable(Throwable t) {
        return t instanceof java.io.IOException
            || t instanceof java.net.SocketTimeoutException
            || t instanceof java.util.concurrent.TimeoutException;
    }
}
