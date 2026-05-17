package com.spectrayan.synaptiq.agentflow.provider.adk;

import com.google.adk.agents.LlmAgent;
import com.spectrayan.synaptiq.agentflow.builder.models.settings.AgentSettings;
import com.spectrayan.synaptiq.agentflow.builder.models.settings.LLMSettings;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AdkAgentFactory}.
 */
class AdkAgentFactoryTest {

    @Nested
    class AgentCreation {

        @Test
        void createLlmAgent_withAllSettings() {
            AgentSettings settings = AgentSettings.builder()
                .name("test-agent")
                .systemPrompt("You are a test assistant.")
                .instructions("Follow these instructions carefully.")
                .llm(LLMSettings.builder()
                    .provider(LLMSettings.Provider.GOOGLE_AI)
                    .model("gemini-2.5-flash")
                    .temperature(0.5)
                    .maxTokens(1024)
                    .build())
                .build();

            LlmAgent agent = AdkAgentFactory.createLlmAgent(settings, List.of());

            assertNotNull(agent);
            assertEquals("test-agent", agent.name());
            assertEquals("Agent using GOOGLE_AI/gemini-2.5-flash", agent.description());
        }

        @Test
        void createLlmAgent_withDefaults() {
            AgentSettings settings = AgentSettings.builder()
                .name("default-agent")
                .build();

            LlmAgent agent = AdkAgentFactory.createLlmAgent(settings, List.of());

            assertNotNull(agent);
            assertEquals("default-agent", agent.name());
        }

        @Test
        void createLlmAgent_withOllamaProvider() {
            AgentSettings settings = AgentSettings.builder()
                .name("local-agent")
                .systemPrompt("You are a local Ollama agent.")
                .llm(LLMSettings.builder()
                    .provider(LLMSettings.Provider.OLLAMA)
                    .model("llama3.2")
                    .temperature(0.7)
                    .build())
                .build();

            LlmAgent agent = AdkAgentFactory.createLlmAgent(settings, List.of());

            assertNotNull(agent);
            assertEquals("local-agent", agent.name());
            assertEquals("Agent using OLLAMA/llama3.2", agent.description());
        }
    }

    @Nested
    class ModelResolution {

        @Test
        void resolveModel_nullLlm_returnsDefault() {
            assertEquals("gemini-2.0-flash", AdkAgentFactory.resolveModel(null));
        }

        @Test
        void resolveModel_blankModel_returnsDefault() {
            LLMSettings llm = LLMSettings.builder().model("").build();
            assertEquals("gemini-2.0-flash", AdkAgentFactory.resolveModel(llm));
        }

        @Test
        void resolveModel_noProvider_returnsModelAsIs() {
            LLMSettings llm = LLMSettings.builder().model("gemini-2.5-pro").build();
            assertEquals("gemini-2.5-pro", AdkAgentFactory.resolveModel(llm));
        }

        @Test
        void resolveModel_googleAi_noPrefix() {
            LLMSettings llm = LLMSettings.builder()
                .provider(LLMSettings.Provider.GOOGLE_AI)
                .model("gemini-2.0-flash")
                .build();
            assertEquals("gemini-2.0-flash", AdkAgentFactory.resolveModel(llm));
        }

        @Test
        void resolveModel_vertexAi_noPrefix() {
            LLMSettings llm = LLMSettings.builder()
                .provider(LLMSettings.Provider.VERTEXAI)
                .model("gemini-2.5-pro")
                .build();
            assertEquals("gemini-2.5-pro", AdkAgentFactory.resolveModel(llm));
        }

        @ParameterizedTest(name = "{0} → {1}/{2}")
        @MethodSource("externalProviderCases")
        void resolveModel_externalProviders_addPrefix(
                LLMSettings.Provider provider, String expectedPrefix, String model) {
            LLMSettings llm = LLMSettings.builder()
                .provider(provider)
                .model(model)
                .build();

            String resolved = AdkAgentFactory.resolveModel(llm);
            assertEquals(expectedPrefix + "/" + model, resolved);
        }

        static Stream<Arguments> externalProviderCases() {
            return Stream.of(
                Arguments.of(LLMSettings.Provider.OLLAMA, "ollama", "llama3.2"),
                Arguments.of(LLMSettings.Provider.OLLAMA, "ollama", "mistral"),
                Arguments.of(LLMSettings.Provider.OLLAMA, "ollama", "phi3"),
                Arguments.of(LLMSettings.Provider.OPENAI, "openai", "gpt-4o"),
                Arguments.of(LLMSettings.Provider.OPENAI, "openai", "o1"),
                Arguments.of(LLMSettings.Provider.ANTHROPIC, "anthropic", "claude-4-sonnet"),
                Arguments.of(LLMSettings.Provider.GROQ, "groq", "llama-3.3-70b-versatile"),
                Arguments.of(LLMSettings.Provider.GROQ, "groq", "mixtral-8x7b-32768"),
                Arguments.of(LLMSettings.Provider.MISTRAL, "mistral", "mistral-large-latest"),
                Arguments.of(LLMSettings.Provider.COHERE, "cohere", "command-r-plus"),
                Arguments.of(LLMSettings.Provider.TOGETHER, "together_ai", "meta-llama/Llama-3-70b"),
                Arguments.of(LLMSettings.Provider.BEDROCK, "bedrock", "anthropic.claude-v2"),
                Arguments.of(LLMSettings.Provider.AZURE_OPENAI, "azure", "gpt-4o-deployment"),
                Arguments.of(LLMSettings.Provider.HUGGINGFACE, "huggingface", "meta-llama/Meta-Llama-3-8B")
            );
        }

        @Test
        void resolveModel_litellm_noPrefix() {
            LLMSettings llm = LLMSettings.builder()
                .provider(LLMSettings.Provider.LITELLM)
                .model("anthropic/claude-4-sonnet")
                .build();
            // LiteLLM handles routing itself, model passed as-is
            assertEquals("anthropic/claude-4-sonnet", AdkAgentFactory.resolveModel(llm));
        }

        @Test
        void resolveModel_alreadyPrefixed_noDuplicate() {
            LLMSettings llm = LLMSettings.builder()
                .provider(LLMSettings.Provider.OLLAMA)
                .model("ollama/llama3.2")
                .build();
            // Should NOT double-prefix
            assertEquals("ollama/llama3.2", AdkAgentFactory.resolveModel(llm));
        }

        @ParameterizedTest(name = "All providers resolve without error: {0}")
        @EnumSource(LLMSettings.Provider.class)
        void resolveModel_allProviders_noException(LLMSettings.Provider p) {
            LLMSettings llm = LLMSettings.builder()
                .provider(p)
                .model("test-model")
                .build();

            assertDoesNotThrow(() -> AdkAgentFactory.resolveModel(llm));
        }
    }

    @Nested
    class InstructionAssembly {

        @Test
        void buildInstruction_combinesPromptAndInstructions() {
            AgentSettings settings = AgentSettings.builder()
                .systemPrompt("System prompt")
                .instructions("User instructions")
                .build();

            String result = AdkAgentFactory.buildInstruction(settings);
            assertTrue(result.contains("System prompt"));
            assertTrue(result.contains("User instructions"));
            assertTrue(result.contains("\n\n"), "Should separate with double newline");
        }

        @Test
        void buildInstruction_promptOnly() {
            AgentSettings settings = AgentSettings.builder()
                .systemPrompt("Only system prompt")
                .build();

            assertEquals("Only system prompt", AdkAgentFactory.buildInstruction(settings));
        }

        @Test
        void buildInstruction_instructionsOnly() {
            AgentSettings settings = AgentSettings.builder()
                .instructions("Only instructions")
                .build();

            assertEquals("Only instructions", AdkAgentFactory.buildInstruction(settings));
        }

        @Test
        void buildInstruction_fallsBackToDefault() {
            AgentSettings settings = AgentSettings.builder().name("empty").build();
            assertEquals("You are a helpful assistant.", AdkAgentFactory.buildInstruction(settings));
        }
    }
}
