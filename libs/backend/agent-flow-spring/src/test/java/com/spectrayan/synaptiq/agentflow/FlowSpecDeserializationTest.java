package com.spectrayan.synaptiq.agentflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spectrayan.synaptiq.agentflow.builder.models.settings.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that verify all JSON flow specs can be correctly deserialized
 * into {@link FlowSettings} with all fields intact.
 */
class FlowSpecDeserializationTest {

    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper();
    }

    @ParameterizedTest(name = "Load and parse: {0}")
    @ValueSource(strings = {
        "flows/single-agent-flow.json",
        "flows/sequential-pipeline-flow.json",
        "flows/parallel-analysis-flow.json",
        "flows/dynamic-orchestrator-flow.json",
        "flows/loop-refine-flow.json",
        "flows/multi-provider-flow.json"
    })
    void allSpecs_deserializeSuccessfully(String specPath) throws Exception {
        FlowSettings settings = loadSpec(specPath);

        assertNotNull(settings, "Settings should not be null for: " + specPath);
        assertNotNull(settings.getId(), "Flow ID should not be null");
        assertNotNull(settings.getName(), "Flow name should not be null");
        assertFalse(settings.getAgents().isEmpty(), "Flow should have at least one agent");
    }

    @Test
    void singleAgentFlow_hasCorrectStructure() throws Exception {
        FlowSettings s = loadSpec("flows/single-agent-flow.json");

        assertEquals("single-agent-flow", s.getId());
        assertEquals("Single Agent Flow", s.getName());
        assertEquals("researcher", s.getEntrypoint());
        assertEquals(FlowSettings.FlowType.STATIC, s.getFlowType());
        assertEquals(1, s.getAgents().size());

        AgentSettings agent = s.getAgents().getFirst();
        assertEquals("researcher", agent.getId());
        assertEquals("Researcher", agent.getName());
        assertNotNull(agent.getSystemPrompt());
        assertNotNull(agent.getInstructions());

        LLMSettings llm = agent.getLlm();
        assertNotNull(llm);
        assertEquals(LLMSettings.Provider.GOOGLE_AI, llm.getProvider());
        assertEquals("gemini-2.0-flash", llm.getModel());
        assertEquals(0.1, llm.getTemperature(), 0.001);
        assertEquals(256, llm.getMaxTokens());

        // Policy
        assertTrue(s.getPolicy().isDeterministic());
        assertEquals(42, s.getPolicy().getSeed());
        assertEquals(30000, s.getPolicy().getResources().getTotalTimeoutMs());

        // Session
        assertEquals(SessionConfig.SessionType.IN_MEMORY, s.getSessionConfig().getType());

        // Cache
        assertFalse(s.getCacheConfig().isEnabled());
    }

    @Test
    void sequentialPipelineFlow_hasEdges() throws Exception {
        FlowSettings s = loadSpec("flows/sequential-pipeline-flow.json");

        assertEquals(FlowSettings.FlowType.STATIC, s.getFlowType());
        assertEquals(2, s.getAgents().size());
        assertEquals(1, s.getEdges().size());

        EdgeSpec edge = s.getEdges().getFirst();
        assertEquals("researcher", edge.getSource());
        assertEquals("writer", edge.getTarget());
        assertEquals(EdgeSpec.EdgeCondition.ALWAYS, edge.getCondition());

        // Both agents should NOT be parallelizable
        assertFalse(s.getAgents().get(0).isParallelizable());
        assertFalse(s.getAgents().get(1).isParallelizable());
    }

    @Test
    void parallelAnalysisFlow_allAgentsParallelizable() throws Exception {
        FlowSettings s = loadSpec("flows/parallel-analysis-flow.json");

        assertEquals(3, s.getAgents().size());
        assertTrue(s.getAgents().stream().allMatch(AgentSettings::isParallelizable),
            "All agents should be marked parallelizable");

        // Verify parallel config
        ParallelConfig pc = s.getPolicy().getParallel();
        assertTrue(pc.isVirtualThreads());
        assertEquals(3, pc.getMaxConcurrency());
    }

    @Test
    void dynamicOrchestratorFlow_isDynamic() throws Exception {
        FlowSettings s = loadSpec("flows/dynamic-orchestrator-flow.json");

        assertEquals(FlowSettings.FlowType.DYNAMIC, s.getFlowType());
        assertEquals("Orchestrator", s.getEntrypoint());
        assertEquals(3, s.getAgents().size());
    }

    @Test
    void loopRefineFlow_isHybridWithCaching() throws Exception {
        FlowSettings s = loadSpec("flows/loop-refine-flow.json");

        assertEquals(FlowSettings.FlowType.HYBRID, s.getFlowType());
        assertEquals(2, s.getAgents().size());

        // Context caching should be enabled
        CacheConfig cache = s.getCacheConfig();
        assertTrue(cache.isEnabled());
        assertEquals(3, cache.getCacheIntervals());
        assertEquals(5, cache.getTtlMinutes());
        assertEquals(1024, cache.getMinTokens());
    }

    @Test
    void multiProviderFlow_correctProviders() throws Exception {
        FlowSettings s = loadSpec("flows/multi-provider-flow.json");

        assertEquals(3, s.getAgents().size());

        // Agent 0: Google AI
        assertEquals(LLMSettings.Provider.GOOGLE_AI, s.getAgents().get(0).getLlm().getProvider());
        assertEquals("gemini-2.0-flash", s.getAgents().get(0).getLlm().getModel());

        // Agent 1: Ollama
        assertEquals(LLMSettings.Provider.OLLAMA, s.getAgents().get(1).getLlm().getProvider());
        assertEquals("llama3.2", s.getAgents().get(1).getLlm().getModel());

        // Agent 2: Groq
        assertEquals(LLMSettings.Provider.GROQ, s.getAgents().get(2).getLlm().getProvider());
        assertEquals("llama-3.3-70b-versatile", s.getAgents().get(2).getLlm().getModel());

        // Verify provider-specific params
        assertNotNull(s.getAgents().get(1).getLlm().getParams().get("base_url"));
        assertNotNull(s.getAgents().get(2).getLlm().getParams().get("api_key_env"));
    }

    private FlowSettings loadSpec(String path) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(is, "Could not find spec file: " + path);
            return objectMapper.readValue(is, FlowSettings.class);
        }
    }
}
