package com.spectrayan.synaptiq.agentflow.provider.adk;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.adk.models.BaseLlm;
import com.google.adk.models.BaseLlmConnection;
import com.google.adk.models.LlmRequest;
import com.google.adk.models.LlmResponse;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ADK {@link BaseLlm} implementation backed by a local Ollama instance.
 * <p>
 * Calls Ollama's {@code /api/chat} endpoint with OpenAI-style message
 * format and maps the response back to ADK's {@link LlmResponse}.
 * <p>
 * Usage:
 * <pre>
 * LlmRegistry.registerLlm("ollama", OllamaLlm::new);
 * LlmAgent.builder().model("ollama/llama3.1").build();
 * </pre>
 */
public class OllamaLlm extends BaseLlm {

    private static final Logger log = LoggerFactory.getLogger(OllamaLlm.class);
    private static final String DEFAULT_BASE_URL = "http://localhost:11434";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(5);

    private final String ollamaModel;
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Create an OllamaLlm for the given model string.
     * The model string is the full ADK model identifier (e.g., "ollama/llama3.1").
     * The "ollama/" prefix is stripped to get the Ollama-native model name.
     *
     * @param model full model string (e.g., "ollama/llama3.1")
     */
    public OllamaLlm(String model) {
        this(model, DEFAULT_BASE_URL);
    }

    /**
     * Create an OllamaLlm with a custom base URL.
     *
     * @param model   full model string
     * @param baseUrl Ollama server URL (e.g., "http://localhost:11434")
     */
    public OllamaLlm(String model, String baseUrl) {
        super(model);
        this.ollamaModel = model.startsWith("ollama/") ? model.substring("ollama/".length()) : model;
        this.baseUrl = baseUrl != null ? baseUrl : DEFAULT_BASE_URL;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        this.objectMapper = new ObjectMapper();
        log.info("Initialized OllamaLlm: model={}, baseUrl={}", this.ollamaModel, this.baseUrl);
    }

    @Override
    public Flowable<LlmResponse> generateContent(LlmRequest llmRequest, boolean stream) {
        return Flowable.fromCallable(() -> {
            try {
                // 1. Convert ADK LlmRequest contents to Ollama chat messages
                List<Map<String, String>> messages = convertToOllamaMessages(llmRequest);

                // 2. Build Ollama /api/chat request body
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", ollamaModel);
                requestBody.put("messages", messages);
                requestBody.put("stream", false); // Non-streaming for simplicity

                // Apply config if present
                llmRequest.config().ifPresent(config -> {
                    Map<String, Object> options = new HashMap<>();
                    if (config.temperature().isPresent()) {
                        options.put("temperature", config.temperature().get());
                    }
                    if (config.maxOutputTokens().isPresent()) {
                        options.put("num_predict", config.maxOutputTokens().get());
                    }
                    if (!options.isEmpty()) {
                        requestBody.put("options", options);
                    }
                });

                String jsonBody = objectMapper.writeValueAsString(requestBody);
                log.debug("Ollama request: model={}, messages={}", ollamaModel, messages.size());

                // 3. Send HTTP request
                HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/chat"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(DEFAULT_TIMEOUT)
                    .build();

                HttpResponse<String> httpResponse = httpClient.send(
                    httpRequest, HttpResponse.BodyHandlers.ofString());

                if (httpResponse.statusCode() != 200) {
                    log.error("Ollama returned HTTP {}: {}", httpResponse.statusCode(), httpResponse.body());
                    return LlmResponse.builder()
                        .errorMessage("Ollama returned HTTP " + httpResponse.statusCode())
                        .turnComplete(true)
                        .build();
                }

                // 4. Parse Ollama response
                JsonNode responseJson = objectMapper.readTree(httpResponse.body());
                String responseText = responseJson.path("message").path("content").asText("");

                log.debug("Ollama response: {} chars", responseText.length());

                // 5. Build ADK LlmResponse
                Content responseContent = Content.fromParts(Part.fromText(responseText));

                return LlmResponse.builder()
                    .content(responseContent)
                    .turnComplete(true)
                    .partial(false)
                    .build();

            } catch (Exception e) {
                log.error("Error calling Ollama: {}", e.getMessage(), e);
                return LlmResponse.builder()
                    .errorMessage("Ollama error: " + e.getMessage())
                    .turnComplete(true)
                    .build();
            }
        });
    }

    @Override
    public BaseLlmConnection connect(LlmRequest llmRequest) {
        throw new UnsupportedOperationException(
            "OllamaLlm does not support live/streaming connections. Use generateContent() instead.");
    }

    /**
     * Convert ADK LlmRequest contents to Ollama-compatible chat messages.
     */
    private List<Map<String, String>> convertToOllamaMessages(LlmRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();

        // Extract system instruction if present
        request.getFirstSystemInstruction().ifPresent(instruction -> {
            Map<String, String> sysMsg = new HashMap<>();
            sysMsg.put("role", "system");
            sysMsg.put("content", instruction);
            messages.add(sysMsg);
        });

        // Convert contents to messages
        for (Content content : request.contents()) {
            String role = content.role().orElse("user");
            StringBuilder text = new StringBuilder();

            if (content.parts().isPresent()) {
                for (Part part : content.parts().get()) {
                    part.text().ifPresent(text::append);
                }
            }

            if (!text.isEmpty()) {
                Map<String, String> msg = new HashMap<>();
                msg.put("role", mapRole(role));
                msg.put("content", text.toString());
                messages.add(msg);
            }
        }

        return messages;
    }

    /**
     * Map ADK/Gemini roles to Ollama (OpenAI-compatible) roles.
     */
    private static String mapRole(String adkRole) {
        return switch (adkRole.toLowerCase()) {
            case "model" -> "assistant";
            case "user" -> "user";
            case "system" -> "system";
            default -> "user";
        };
    }
}
