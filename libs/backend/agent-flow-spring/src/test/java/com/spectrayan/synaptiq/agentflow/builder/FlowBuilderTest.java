package com.spectrayan.synaptiq.agentflow.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spectrayan.synaptiq.agentflow.builder.models.settings.AgentSettings;
import com.spectrayan.synaptiq.agentflow.builder.models.settings.FlowSettings;
import com.spectrayan.synaptiq.agentflow.spi.AgentFlowProvider;
import com.spectrayan.synaptiq.agentflow.spi.CompiledFlow;
import com.spectrayan.synaptiq.agentflow.spi.FlowExecutionContext;
import com.spectrayan.synaptiq.agentflow.spi.FlowExecutionEvent;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FlowBuilder}.
 */
class FlowBuilderTest {

    @Test
    void build_delegatesToProvider() {
        // Mock provider
        AgentFlowProvider mockProvider = new AgentFlowProvider() {
            @Override public String name() { return "mock"; }
            @Override public CompiledFlow compile(FlowSettings settings) {
                return new CompiledFlow() {
                    @Override public String name() { return settings.getName(); }
                    @Override public Flux<FlowExecutionEvent> execute(Object input, FlowExecutionContext ctx) {
                        return Flux.empty();
                    }
                };
            }
        };

        FlowBuilder builder = new FlowBuilder(mockProvider, new ObjectMapper());

        FlowSettings settings = FlowSettings.builder()
            .name("test-flow")
            .agents(List.of(AgentSettings.builder().name("agent").build()))
            .build();

        CompiledFlow result = builder.build(settings);

        assertNotNull(result);
        assertEquals("test-flow", result.name());
    }

    @Test
    void buildFromJson_parsesAndCompiles() throws Exception {
        AgentFlowProvider mockProvider = new AgentFlowProvider() {
            @Override public String name() { return "mock"; }
            @Override public CompiledFlow compile(FlowSettings settings) {
                return new CompiledFlow() {
                    @Override public String name() { return settings.getName(); }
                    @Override public Flux<FlowExecutionEvent> execute(Object input, FlowExecutionContext ctx) {
                        return Flux.empty();
                    }
                };
            }
        };

        FlowBuilder builder = new FlowBuilder(mockProvider, new ObjectMapper());

        String json = """
            {
                "name": "json-flow",
                "agents": [{"name": "agent-from-json"}]
            }
            """;

        CompiledFlow result = builder.buildFromJson(json);

        assertNotNull(result);
        assertEquals("json-flow", result.name());
    }
}
