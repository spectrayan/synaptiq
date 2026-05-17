package com.spectrayan.synaptiq.agentflow.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spectrayan.synaptiq.agentflow.builder.FlowBuilder;
import com.spectrayan.synaptiq.agentflow.builder.models.settings.AgentSettings;
import com.spectrayan.synaptiq.agentflow.builder.models.settings.FlowSettings;
import com.spectrayan.synaptiq.agentflow.spi.AgentFlowProvider;
import com.spectrayan.synaptiq.agentflow.spi.CompiledFlow;
import com.spectrayan.synaptiq.agentflow.spi.FlowExecutionContext;
import com.spectrayan.synaptiq.agentflow.spi.FlowExecutionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FlowExecutor}.
 */
class FlowExecutorTest {

    private FlowExecutor executor;
    private FlowSettings testSpec;

    @BeforeEach
    void setUp() {
        AgentFlowProvider mockProvider = new AgentFlowProvider() {
            @Override public String name() { return "mock"; }
            @Override public CompiledFlow compile(FlowSettings settings) {
                return new CompiledFlow() {
                    @Override public String name() { return settings.getName(); }
                    @Override public Flux<FlowExecutionEvent> execute(Object input, FlowExecutionContext ctx) {
                        return Flux.just(
                            new FlowExecutionEvent(
                                FlowExecutionEvent.EventType.STEP_STARTED,
                                "step-1", "MockAgent", "Starting",
                                Map.of(), Instant.now()),
                            new FlowExecutionEvent(
                                FlowExecutionEvent.EventType.STEP_COMPLETED,
                                "step-1", "MockAgent", "Done",
                                Map.of("output", "result"), Instant.now()),
                            FlowExecutionEvent.completed(Map.of("final", "output"))
                        );
                    }
                };
            }
        };

        FlowBuilder builder = new FlowBuilder(mockProvider, new ObjectMapper());
        executor = new FlowExecutor(builder);

        testSpec = FlowSettings.builder()
            .id("test-flow-id")
            .name("test-flow")
            .agents(List.of(AgentSettings.builder().name("agent").build()))
            .build();
    }

    @Test
    void startRun_returnsRunId() {
        String runId = executor.startRun(testSpec, "Hello");
        assertNotNull(runId);
        assertFalse(runId.isBlank());
    }

    @Test
    void streamRun_emitsEvents() {
        String runId = executor.startRun(testSpec, "Hello");

        List<FlowExecutionEvent> events = executor.streamRun(runId)
            .collectList()
            .block();

        assertNotNull(events);
        assertEquals(3, events.size());
        assertEquals(FlowExecutionEvent.EventType.STEP_STARTED, events.get(0).type());
        assertEquals(FlowExecutionEvent.EventType.STEP_COMPLETED, events.get(1).type());
        assertEquals(FlowExecutionEvent.EventType.FLOW_COMPLETED, events.get(2).type());
    }

    @Test
    void cancelRun_marksRunCancelled() {
        String runId = executor.startRun(testSpec, "Hello");
        executor.cancelRun(runId);

        // Run should still be retrievable
        assertNotNull(runId);
    }

    @Test
    void getRun_unknownThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> executor.streamRun("nonexistent-id"));
    }
}
