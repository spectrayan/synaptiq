package com.spectrayan.synaptiq.agentflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.LoopAgent;
import com.google.adk.agents.ParallelAgent;
import com.google.adk.agents.SequentialAgent;
import com.spectrayan.synaptiq.agentflow.builder.FlowBuilder;
import com.spectrayan.synaptiq.agentflow.builder.models.settings.FlowSettings;
import com.spectrayan.synaptiq.agentflow.executor.FlowExecutor;
import com.spectrayan.synaptiq.agentflow.provider.adk.AdkCompiledFlow;
import com.spectrayan.synaptiq.agentflow.provider.adk.AdkFlowProvider;
import com.spectrayan.synaptiq.agentflow.spi.CompiledFlow;
import com.spectrayan.synaptiq.agentflow.spi.FlowExecutionEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end tests that load JSON specs, compile them with the real
 * ADK provider, and verify the resulting graph topology.
 * <p>
 * These tests do NOT call an actual LLM — they verify the graph
 * structure (agent types, sub-agents, names) is correctly assembled
 * from the declarative spec.
 */
class AdkFlowCompilationTest {

    private static AdkFlowProvider provider;
    private static FlowBuilder flowBuilder;
    private static FlowExecutor flowExecutor;
    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {
        provider = new AdkFlowProvider(null);
        objectMapper = new ObjectMapper();
        flowBuilder = new FlowBuilder(provider, objectMapper);
        flowExecutor = new FlowExecutor(flowBuilder);
    }

    // ------------------------------------------------------------------
    // Single Agent
    // ------------------------------------------------------------------

    @Nested
    class SingleAgentTests {

        @Test
        void compile_producesLlmAgent() throws Exception {
            FlowSettings spec = loadSpec("flows/single-agent-flow.json");
            CompiledFlow flow = provider.compile(spec);

            assertNotNull(flow);
            assertEquals("Single Agent Flow", flow.name());
            assertInstanceOf(AdkCompiledFlow.class, flow);

            // Extract root agent via reflection to verify topology
            BaseAgent root = extractRootAgent(flow);
            assertInstanceOf(LlmAgent.class, root);
            assertEquals("Researcher", root.name());
        }

        @Test
        void flowBuilder_fromJson() throws Exception {
            String json = loadSpecAsString("flows/single-agent-flow.json");
            CompiledFlow flow = flowBuilder.buildFromJson(json);

            assertNotNull(flow);
            assertEquals("Single Agent Flow", flow.name());
        }

        @Test
        void flowExecutor_startRun() throws Exception {
            FlowSettings spec = loadSpec("flows/single-agent-flow.json");
            String runId = flowExecutor.startRun(spec, "What is Java?");

            assertNotNull(runId);
            assertFalse(runId.isBlank());
            assertFalse(flowExecutor.isComplete(runId));
        }
    }

    // ------------------------------------------------------------------
    // Sequential Pipeline
    // ------------------------------------------------------------------

    @Nested
    class SequentialPipelineTests {

        @Test
        void compile_producesSequentialAgent() throws Exception {
            FlowSettings spec = loadSpec("flows/sequential-pipeline-flow.json");
            CompiledFlow flow = provider.compile(spec);

            assertNotNull(flow);
            BaseAgent root = extractRootAgent(flow);

            // Two non-parallel agents → SequentialAgent
            assertInstanceOf(SequentialAgent.class, root);
            assertTrue(root.name().contains("sequential"),
                "Root agent name should indicate sequential: " + root.name());

            // Should have 2 sub-agents
            var subAgents = root.subAgents();
            assertEquals(2, subAgents.size());
            assertEquals("Researcher", subAgents.get(0).name());
            assertEquals("Writer", subAgents.get(1).name());
        }
    }

    // ------------------------------------------------------------------
    // Parallel Analysis
    // ------------------------------------------------------------------

    @Nested
    class ParallelAnalysisTests {

        @Test
        void compile_producesParallelAgent() throws Exception {
            FlowSettings spec = loadSpec("flows/parallel-analysis-flow.json");
            CompiledFlow flow = provider.compile(spec);

            assertNotNull(flow);
            BaseAgent root = extractRootAgent(flow);

            // All agents parallelizable → ParallelAgent
            assertInstanceOf(ParallelAgent.class, root);
            assertTrue(root.name().contains("parallel"),
                "Root agent name should indicate parallel: " + root.name());

            // Should have 3 sub-agents
            var subAgents = root.subAgents();
            assertEquals(3, subAgents.size());
        }

        @Test
        void allSubAgents_areLlmAgents() throws Exception {
            FlowSettings spec = loadSpec("flows/parallel-analysis-flow.json");
            BaseAgent root = extractRootAgent(provider.compile(spec));

            for (var sub : root.subAgents()) {
                assertInstanceOf(LlmAgent.class, sub,
                    "Sub-agent '" + sub.name() + "' should be LlmAgent");
            }
        }
    }

    // ------------------------------------------------------------------
    // Dynamic Orchestrator
    // ------------------------------------------------------------------

    @Nested
    class DynamicOrchestratorTests {

        @Test
        void compile_rootHasSubAgents() throws Exception {
            FlowSettings spec = loadSpec("flows/dynamic-orchestrator-flow.json");
            CompiledFlow flow = provider.compile(spec);

            assertNotNull(flow);
            BaseAgent root = extractRootAgent(flow);

            // Dynamic flow with entrypoint "Orchestrator" → LlmAgent with sub-agents
            assertInstanceOf(LlmAgent.class, root);
            assertEquals("Orchestrator", root.name());

            // Should have 2 sub-agents (CodeExpert, DataExpert)
            var subAgents = root.subAgents();
            assertEquals(2, subAgents.size());
        }
    }

    // ------------------------------------------------------------------
    // Loop / Hybrid
    // ------------------------------------------------------------------

    @Nested
    class LoopRefineTests {

        @Test
        void compile_producesLoopAgent() throws Exception {
            FlowSettings spec = loadSpec("flows/loop-refine-flow.json");
            CompiledFlow flow = provider.compile(spec);

            assertNotNull(flow);
            BaseAgent root = extractRootAgent(flow);

            // Hybrid → LoopAgent
            assertInstanceOf(LoopAgent.class, root);
            assertTrue(root.name().contains("loop"),
                "Root agent name should indicate loop: " + root.name());

            // LoopAgent wraps a SequentialAgent
            var subAgents = root.subAgents();
            assertEquals(1, subAgents.size());
            assertInstanceOf(SequentialAgent.class, subAgents.getFirst());

            // Inner sequential should have Generator + Critic
            var inner = subAgents.getFirst().subAgents();
            assertEquals(2, inner.size());
            assertEquals("Generator", inner.get(0).name());
            assertEquals("Critic", inner.get(1).name());
        }
    }

    // ------------------------------------------------------------------
    // Multi-Provider Model Resolution
    // ------------------------------------------------------------------

    @Nested
    class MultiProviderTests {

        @Test
        void compile_resolvesDifferentProviders() throws Exception {
            FlowSettings spec = loadSpec("flows/multi-provider-flow.json");
            CompiledFlow flow = provider.compile(spec);

            assertNotNull(flow);
            BaseAgent root = extractRootAgent(flow);

            // 3 non-parallel agents → SequentialAgent
            assertInstanceOf(SequentialAgent.class, root);
            assertEquals(3, root.subAgents().size());

            // All sub-agents should be LlmAgent
            for (var sub : root.subAgents()) {
                assertInstanceOf(LlmAgent.class, sub);
            }
        }

        @Test
        void compile_agentsHaveCorrectNames() throws Exception {
            FlowSettings spec = loadSpec("flows/multi-provider-flow.json");
            BaseAgent root = extractRootAgent(provider.compile(spec));

            List<String> names = root.subAgents().stream()
                .map(BaseAgent::name)
                .toList();

            assertEquals(List.of("GeminiAgent", "OllamaAgent", "GroqAgent"), names);
        }
    }

    // ------------------------------------------------------------------
    // Mixed Pipeline (Parallel groups inside Sequential)
    // ------------------------------------------------------------------

    @Test
    void mixedPipeline_createsParallelGroupsInSequence() throws Exception {
        // Build a spec with: seq → [par, par] → seq
        FlowSettings spec = FlowSettings.builder()
            .id("mixed-test")
            .name("MixedTest")
            .flowType(FlowSettings.FlowType.STATIC)
            .agents(List.of(
                com.spectrayan.synaptiq.agentflow.builder.models.settings.AgentSettings.builder()
                    .id("pre").name("Pre").parallelizable(false).build(),
                com.spectrayan.synaptiq.agentflow.builder.models.settings.AgentSettings.builder()
                    .id("p1").name("Parallel1").parallelizable(true).build(),
                com.spectrayan.synaptiq.agentflow.builder.models.settings.AgentSettings.builder()
                    .id("p2").name("Parallel2").parallelizable(true).build(),
                com.spectrayan.synaptiq.agentflow.builder.models.settings.AgentSettings.builder()
                    .id("post").name("Post").parallelizable(false).build()
            ))
            .build();

        BaseAgent root = extractRootAgent(provider.compile(spec));

        assertInstanceOf(SequentialAgent.class, root);

        var steps = root.subAgents();
        assertEquals(3, steps.size(), "Should be 3 steps: Pre, ParallelGroup, Post");

        assertEquals("Pre", steps.get(0).name());
        assertInstanceOf(LlmAgent.class, steps.get(0));

        // Middle step should be a ParallelAgent grouping Parallel1 + Parallel2
        assertInstanceOf(ParallelAgent.class, steps.get(1));
        assertEquals(2, steps.get(1).subAgents().size());

        assertEquals("Post", steps.get(2).name());
        assertInstanceOf(LlmAgent.class, steps.get(2));
    }

    // ------------------------------------------------------------------
    // Full FlowExecutor round-trip
    // ------------------------------------------------------------------

    @Test
    void fullRoundTrip_jsonToRunId() throws Exception {
        String json = loadSpecAsString("flows/single-agent-flow.json");
        FlowSettings spec = objectMapper.readValue(json, FlowSettings.class);

        String runId = flowExecutor.startRun(spec, "Explain photosynthesis");

        assertNotNull(runId);
        assertFalse(flowExecutor.isComplete(runId));

        // Cancel immediately (we aren't actually calling LLM)
        flowExecutor.cancelRun(runId);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Extract the root {@link BaseAgent} from a {@link CompiledFlow} via reflection.
     * This is necessary because the rootAgent field is private in AdkCompiledFlow.
     */
    private static BaseAgent extractRootAgent(CompiledFlow flow) {
        try {
            Field rootField = AdkCompiledFlow.class.getDeclaredField("rootAgent");
            rootField.setAccessible(true);
            return (BaseAgent) rootField.get(flow);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Could not extract rootAgent from CompiledFlow: " + e.getMessage());
            return null;
        }
    }

    private static FlowSettings loadSpec(String path) throws Exception {
        try (InputStream is = AdkFlowCompilationTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(is, "Could not find spec file: " + path);
            return objectMapper.readValue(is, FlowSettings.class);
        }
    }

    private static String loadSpecAsString(String path) throws Exception {
        try (InputStream is = AdkFlowCompilationTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(is, "Could not find spec file: " + path);
            return new String(is.readAllBytes());
        }
    }
}
