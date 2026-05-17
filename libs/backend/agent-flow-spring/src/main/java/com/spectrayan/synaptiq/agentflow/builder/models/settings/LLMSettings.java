package com.spectrayan.synaptiq.agentflow.builder.models.settings;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMSettings {
    
    private Provider provider;
    private String model;
    
    @Builder.Default
    private double temperature = 0.0;
    
    private Integer maxTokens;
    
    @Builder.Default
    private boolean streaming = true;
    
    @Builder.Default
    private Map<String, Object> params = new HashMap<>();

    /**
     * Supported LLM providers.
     * <p>
     * Each provider maps to a specific API endpoint or SDK integration.
     * The ADK provider uses the provider + model to construct the full
     * model identifier string.
     */
    public enum Provider {
        /** Google Vertex AI (Gemini models via Vertex AI API). */
        VERTEXAI,

        /** Google AI Studio / Gemini Developer API. */
        GOOGLE_AI,

        /** OpenAI API (GPT-4o, o1, o3, etc.). */
        OPENAI,

        /** Anthropic API (Claude 4, Sonnet, Haiku). */
        ANTHROPIC,

        /** Ollama — local self-hosted models (Llama 3, Mistral, Phi, etc.). */
        OLLAMA,

        /** Groq — ultra-fast inference (Llama 3, Mixtral, Gemma). */
        GROQ,

        /** Mistral AI API (Mistral Large, Codestral). */
        MISTRAL,

        /** Cohere API (Command R+, Embed). */
        COHERE,

        /** Together AI — open-source model hosting. */
        TOGETHER,

        /** Amazon Bedrock (Claude, Titan, Llama via AWS). */
        BEDROCK,

        /** Azure OpenAI Service. */
        AZURE_OPENAI,

        /** HuggingFace Inference API / TGI endpoints. */
        HUGGINGFACE,

        /** LiteLLM proxy — unified interface to 100+ providers. */
        LITELLM
    }
}
