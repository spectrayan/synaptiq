package com.spectrayan.synaptiq.agentflow.provider.adk;

import com.spectrayan.synaptiq.agentflow.builder.models.settings.*;
import com.spectrayan.synaptiq.agentflow.spi.CompiledFlow;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AdkFlowProvider}.
 */
class AdkFlowProviderTest {

    private final AdkFlowProvider provider = new AdkFlowProvider(null);

    @Test
    void name_returnsGoogleAdk() {
        assertEquals("google-adk", provider.name());
    }

    @Test
    void compile_singleAgent() {
        FlowSettings settings = FlowSettings.builder()
            .name("single-flow")
            .agents(List.of(
                AgentSettings.builder()
                    .id("agent-1")
                    .name("Agent One")
                    .systemPrompt("Be helpful.")
                    .build()
            ))
            .build();

        CompiledFlow flow = provider.compile(settings);

        assertNotNull(flow);
        assertEquals("single-flow", flow.name());
    }

    @Test
    void compile_sequentialAgents() {
        FlowSettings settings = FlowSettings.builder()
            .name("seq-flow")
            .flowType(FlowSettings.FlowType.STATIC)
            .agents(List.of(
                AgentSettings.builder().id("a1").name("First").build(),
                AgentSettings.builder().id("a2").name("Second").build()
            ))
            .build();

        CompiledFlow flow = provider.compile(settings);

        assertNotNull(flow);
        assertEquals("seq-flow", flow.name());
    }

    @Test
    void compile_parallelAgents() {
        FlowSettings settings = FlowSettings.builder()
            .name("par-flow")
            .flowType(FlowSettings.FlowType.STATIC)
            .agents(List.of(
                AgentSettings.builder().id("a1").name("First").parallelizable(true).build(),
                AgentSettings.builder().id("a2").name("Second").parallelizable(true).build()
            ))
            .build();

        CompiledFlow flow = provider.compile(settings);
        assertNotNull(flow);
    }

    @Test
    void compile_dynamicFlow() {
        FlowSettings settings = FlowSettings.builder()
            .name("dynamic-flow")
            .flowType(FlowSettings.FlowType.DYNAMIC)
            .entrypoint("Root")
            .agents(List.of(
                AgentSettings.builder().id("root").name("Root").build(),
                AgentSettings.builder().id("sub").name("SubAgent").build()
            ))
            .build();

        CompiledFlow flow = provider.compile(settings);
        assertNotNull(flow);
    }

    @Test
    void compile_hybridFlow() {
        FlowSettings settings = FlowSettings.builder()
            .name("hybrid-flow")
            .flowType(FlowSettings.FlowType.HYBRID)
            .agents(List.of(
                AgentSettings.builder().id("gen").name("Generator").build(),
                AgentSettings.builder().id("eval").name("Evaluator").build()
            ))
            .build();

        CompiledFlow flow = provider.compile(settings);
        assertNotNull(flow);
    }

    @Test
    void compile_mixedPipeline() {
        FlowSettings settings = FlowSettings.builder()
            .name("mixed-flow")
            .flowType(FlowSettings.FlowType.STATIC)
            .agents(List.of(
                AgentSettings.builder().id("a1").name("First").parallelizable(false).build(),
                AgentSettings.builder().id("a2").name("Second").parallelizable(true).build(),
                AgentSettings.builder().id("a3").name("Third").parallelizable(true).build(),
                AgentSettings.builder().id("a4").name("Fourth").parallelizable(false).build()
            ))
            .build();

        CompiledFlow flow = provider.compile(settings);
        assertNotNull(flow);
    }
}
