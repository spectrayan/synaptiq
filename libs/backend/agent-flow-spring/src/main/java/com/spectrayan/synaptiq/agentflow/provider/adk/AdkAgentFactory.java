package com.spectrayan.synaptiq.agentflow.provider.adk;

import com.google.adk.agents.LlmAgent;
import com.google.adk.models.BaseLlm;
import com.google.adk.models.LlmRegistry;
import com.spectrayan.synaptiq.agentflow.builder.models.settings.AgentSettings;
import com.spectrayan.synaptiq.agentflow.builder.models.settings.LLMSettings;
import com.google.genai.types.GenerateContentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Factory for creating Google ADK {@link LlmAgent} instances from
 * {@link AgentSettings} definitions.
 * <p>
 * Handles model resolution (provider-aware), instruction assembly,
 * temperature/token config, and tool attachment.
 * <p>
 * For non-Google providers (Ollama, etc.), custom {@link BaseLlm}
 * implementations are registered via {@link LlmRegistry} and used
 * directly to avoid the default Gemini client.
 */
public final class AdkAgentFactory {

    private static final Logger log = LoggerFactory.getLogger(AdkAgentFactory.class);
    private static final String DEFAULT_MODEL = "gemini-2.0-flash";
    private static final AtomicBoolean ollamaRegistered = new AtomicBoolean(false);

    private AdkAgentFactory() {
        // utility class
    }

    /**
     * Create an ADK {@link LlmAgent} from the given settings, attaching
     * the provided tools (a union of BaseTool and BaseToolset instances).
     *
     * @param settings agent definition from the flow spec
     * @param tools    pre-built ADK tools for this agent
     * @return a fully configured LlmAgent
     */
    public static LlmAgent createLlmAgent(AgentSettings settings, List<Object> tools) {
        String resolvedModel = resolveModel(settings.getLlm());
        log.debug("Creating LlmAgent: name={}, model={}", settings.getName(), resolvedModel);

        LlmAgent.Builder builder = LlmAgent.builder()
            .name(settings.getName())
            .instruction(buildInstruction(settings));

        // Use BaseLlm directly for custom providers, model string for Google
        if (settings.getLlm() != null && isCustomProvider(settings.getLlm().getProvider())) {
            BaseLlm llm = createCustomLlm(settings.getLlm(), resolvedModel);
            builder.model(llm);
        } else {
            builder.model(resolvedModel);
        }

        if (settings.getLlm() != null) {
            applyLlmConfig(builder, settings.getLlm());
        }

        if (!tools.isEmpty()) {
            builder.tools(tools);
        }

        return builder.build();
    }

    /**
     * Resolve the full model identifier from LLM settings.
     * <p>
     * For Google-native providers (VERTEXAI, GOOGLE_AI), the model string
     * is used directly. For external providers accessed through custom
     * BaseLlm implementations, the provider prefix is prepended.
     *
     * @param llm the LLM settings (nullable)
     * @return the resolved model identifier string
     */
    static String resolveModel(LLMSettings llm) {
        if (llm == null || llm.getModel() == null || llm.getModel().isBlank()) {
            return DEFAULT_MODEL;
        }

        String model = llm.getModel();

        if (llm.getProvider() == null) {
            return model;
        }

        return switch (llm.getProvider()) {
            case VERTEXAI, GOOGLE_AI -> model;
            case OLLAMA -> prefixIfNeeded(model, "ollama");
            case OPENAI -> prefixIfNeeded(model, "openai");
            case ANTHROPIC -> prefixIfNeeded(model, "anthropic");
            case GROQ -> prefixIfNeeded(model, "groq");
            case MISTRAL -> prefixIfNeeded(model, "mistral");
            case COHERE -> prefixIfNeeded(model, "cohere");
            case TOGETHER -> prefixIfNeeded(model, "together_ai");
            case BEDROCK -> prefixIfNeeded(model, "bedrock");
            case AZURE_OPENAI -> prefixIfNeeded(model, "azure");
            case HUGGINGFACE -> prefixIfNeeded(model, "huggingface");
            case LITELLM -> model;
        };
    }

    /**
     * Assemble the instruction string from system prompt and instructions.
     */
    static String buildInstruction(AgentSettings settings) {
        StringBuilder sb = new StringBuilder();
        if (settings.getSystemPrompt() != null && !settings.getSystemPrompt().isBlank()) {
            sb.append(settings.getSystemPrompt());
        }
        if (settings.getInstructions() != null && !settings.getInstructions().isBlank()) {
            if (!sb.isEmpty()) {
                sb.append("\n\n");
            }
            sb.append(settings.getInstructions());
        }
        return sb.isEmpty() ? "You are a helpful assistant." : sb.toString();
    }

    /**
     * Check if the provider has a native {@link BaseLlm} implementation
     * that should be used instead of the default model string resolution.
     * <p>
     * Only returns {@code true} for providers with fully implemented
     * BaseLlm adapters. Unimplemented providers fall back to model
     * string resolution (suitable for LiteLLM proxy routing).
     */
    private static boolean isCustomProvider(LLMSettings.Provider provider) {
        if (provider == null) return false;
        return switch (provider) {
            case OLLAMA -> true;
            // Future: OPENAI, ANTHROPIC etc. when BaseLlm adapters are implemented
            default -> false;
        };
    }

    /**
     * Create a custom {@link BaseLlm} implementation for the given provider.
     */
    private static BaseLlm createCustomLlm(LLMSettings llm, String resolvedModel) {
        return switch (llm.getProvider()) {
            case OLLAMA -> {
                String baseUrl = extractParam(llm.getParams(), "base_url", "http://localhost:11434");
                ensureOllamaRegistered(baseUrl);
                yield new OllamaLlm(resolvedModel, baseUrl);
            }
            // Future: add more custom providers here
            default -> {
                log.warn("No custom BaseLlm for provider '{}', falling back to LlmRegistry lookup",
                    llm.getProvider());
                BaseLlm registered = LlmRegistry.getLlm(resolvedModel);
                if (registered != null) {
                    yield registered;
                }
                throw new UnsupportedOperationException(
                    "Provider '" + llm.getProvider() + "' requires a custom BaseLlm implementation "
                    + "or LlmRegistry registration.");
            }
        };
    }

    /**
     * Register the Ollama LLM factory with ADK's LlmRegistry (idempotent).
     */
    private static void ensureOllamaRegistered(String baseUrl) {
        if (ollamaRegistered.compareAndSet(false, true)) {
            LlmRegistry.registerLlm("ollama", model -> new OllamaLlm(model, baseUrl));
            log.info("Registered OllamaLlm with LlmRegistry (baseUrl: {})", baseUrl);
        }
    }

    /**
     * Apply LLM configuration (temperature, max tokens, description) to the builder.
     */
    private static void applyLlmConfig(LlmAgent.Builder builder, LLMSettings llm) {
        String desc = llm.getProvider() != null
            ? "Agent using " + llm.getProvider() + "/" + llm.getModel()
            : "Agent using " + llm.getModel();
        builder.description(desc);

        GenerateContentConfig.Builder configBuilder = GenerateContentConfig.builder();
        boolean hasConfig = false;

        if (llm.getTemperature() > 0.0) {
            configBuilder.temperature((float) llm.getTemperature());
            hasConfig = true;
        }
        if (llm.getMaxTokens() != null && llm.getMaxTokens() > 0) {
            configBuilder.maxOutputTokens(llm.getMaxTokens());
            hasConfig = true;
        }

        if (hasConfig) {
            builder.generateContentConfig(configBuilder.build());
        }
    }

    /**
     * Prefix the model name with the provider slug if not already prefixed.
     */
    private static String prefixIfNeeded(String model, String prefix) {
        if (model.startsWith(prefix + "/")) {
            return model;
        }
        return prefix + "/" + model;
    }

    /**
     * Extract a string parameter from the LLM params map.
     */
    private static String extractParam(Map<String, Object> params, String key, String defaultValue) {
        if (params == null) return defaultValue;
        Object value = params.get(key);
        return value != null ? value.toString() : defaultValue;
    }
}
