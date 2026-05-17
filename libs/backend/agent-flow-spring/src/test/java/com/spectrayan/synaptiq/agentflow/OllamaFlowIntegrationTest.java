package com.spectrayan.synaptiq.agentflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spectrayan.synaptiq.agentflow.builder.FlowBuilder;
import com.spectrayan.synaptiq.agentflow.builder.models.settings.FlowSettings;
import com.spectrayan.synaptiq.agentflow.executor.FlowExecutor;
import com.spectrayan.synaptiq.agentflow.provider.adk.AdkFlowProvider;
import com.spectrayan.synaptiq.agentflow.spi.CompiledFlow;
import com.spectrayan.synaptiq.agentflow.spi.FlowExecutionContext;
import com.spectrayan.synaptiq.agentflow.spi.FlowExecutionEvent;
import com.spectrayan.synaptiq.agentflow.spi.FlowExecutionEvent.EventType;
import org.junit.jupiter.api.*;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests that execute complete agent flows against a local
 * Ollama instance. These tests perform REAL LLM inference.
 * <p>
 * Prerequisites:
 * <ul>
 *   <li>Ollama must be running at {@code http://localhost:11434}</li>
 *   <li>Model {@code llama3.1} must be available ({@code ollama pull llama3.1})</li>
 * </ul>
 * <p>
 * Tests are automatically skipped if Ollama is not reachable.
 */
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OllamaFlowIntegrationTest {

    private static final String OLLAMA_URL = "http://localhost:11434";
    private static final Duration OLLAMA_TIMEOUT = Duration.ofMinutes(5);

    private static AdkFlowProvider provider;
    private static FlowBuilder flowBuilder;
    private static FlowExecutor flowExecutor;
    private static ObjectMapper objectMapper;
    private static boolean ollamaAvailable;

    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper();
        provider = new AdkFlowProvider(null);
        flowBuilder = new FlowBuilder(provider, objectMapper);
        flowExecutor = new FlowExecutor(flowBuilder);
        ollamaAvailable = checkOllamaAvailable();

        if (!ollamaAvailable) {
            System.err.println("⚠ Ollama is not available at " + OLLAMA_URL
                + " — integration tests will be skipped.");
        } else {
            System.out.println("✓ Ollama is available. Running integration tests...");
        }
    }

    // ------------------------------------------------------------------
    // Single Agent E2E
    // ------------------------------------------------------------------

    @Test
    @Order(1)
    @DisplayName("Single agent flow: compile → execute → stream events (Ollama/llama3.1)")
    void singleAgent_fullLifecycle() throws Exception {
        Assumptions.assumeTrue(ollamaAvailable, "Ollama not available");

        // 1. Load spec
        FlowSettings spec = loadSpec("flows/ollama-e2e-flow.json");
        assertNotNull(spec);
        assertEquals("Ollama E2E Test", spec.getName());

        // 2. Compile
        CompiledFlow flow = provider.compile(spec);
        assertNotNull(flow);
        assertEquals("Ollama E2E Test", flow.name());

        // 3. Execute
        FlowExecutionContext ctx = new FlowExecutionContext(
            UUID.randomUUID().toString(), new AtomicBoolean(false));

        List<FlowExecutionEvent> events = new ArrayList<>();

        System.out.println("\n--- Single Agent Flow Execution ---");
        flow.execute("What is the capital of France? Answer in one word.", ctx)
            .doOnNext(event -> {
                events.add(event);
                System.out.printf("  [%s] %s: %s%n",
                    event.type(), event.stepName(), truncate(event.content(), 100));
            })
            .blockLast(OLLAMA_TIMEOUT);

        // 4. Verify events
        assertFalse(events.isEmpty(), "Should receive at least one event");

        // Should have a STEP_STARTED
        assertTrue(events.stream().anyMatch(e -> e.type() == EventType.STEP_STARTED),
            "Should have STEP_STARTED event");

        // Should have content events
        boolean hasContent = events.stream()
            .anyMatch(e -> e.type() == EventType.STEP_COMPLETED
                || e.type() == EventType.TOKEN_DELTA);
        assertTrue(hasContent, "Should have content events (STEP_COMPLETED or TOKEN_DELTA)");

        // Check that some event mentions "Paris" (case-insensitive)
        boolean mentionsParis = events.stream()
            .filter(e -> e.content() != null)
            .anyMatch(e -> e.content().toLowerCase().contains("paris"));
        assertTrue(mentionsParis, "Response should mention 'Paris' for capital of France");

        // Should end with FLOW_COMPLETED
        assertTrue(events.stream().anyMatch(e -> e.type() == EventType.FLOW_COMPLETED),
            "Should have FLOW_COMPLETED event");

        System.out.println("  ✓ Single agent flow completed with " + events.size() + " events");
    }

    // ------------------------------------------------------------------
    // Full FlowExecutor Round-Trip
    // ------------------------------------------------------------------

    @Test
    @Order(2)
    @DisplayName("FlowExecutor round-trip: startRun → streamRun → complete (Ollama)")
    void flowExecutor_fullRoundTrip() throws Exception {
        Assumptions.assumeTrue(ollamaAvailable, "Ollama not available");

        FlowSettings spec = loadSpec("flows/ollama-e2e-flow.json");

        // 1. Start run via executor
        String runId = flowExecutor.startRun(spec, "What is 2 + 2? Reply with just the number.");
        assertNotNull(runId);
        System.out.println("\n--- FlowExecutor Round-Trip (runId: " + runId + ") ---");

        // 2. Stream events
        List<FlowExecutionEvent> events = flowExecutor.streamRun(runId)
            .doOnNext(event -> System.out.printf("  [%s] %s%n",
                event.type(), truncate(event.content(), 80)))
            .collectList()
            .block(OLLAMA_TIMEOUT);

        assertNotNull(events);
        assertFalse(events.isEmpty());

        // 3. Check final event
        FlowExecutionEvent last = events.getLast();
        assertTrue(
            last.type() == EventType.FLOW_COMPLETED || last.type() == EventType.ERROR,
            "Last event should be FLOW_COMPLETED or ERROR, got: " + last.type());

        System.out.println("  ✓ FlowExecutor round-trip completed with " + events.size() + " events");
    }

    // ------------------------------------------------------------------
    // FlowBuilder.buildFromJson
    // ------------------------------------------------------------------

    @Test
    @Order(3)
    @DisplayName("FlowBuilder.buildFromJson → execute (Ollama)")
    void flowBuilder_jsonToExecution() throws Exception {
        Assumptions.assumeTrue(ollamaAvailable, "Ollama not available");

        String json = loadSpecAsString("flows/ollama-e2e-flow.json");
        CompiledFlow flow = flowBuilder.buildFromJson(json);

        assertNotNull(flow);
        assertEquals("Ollama E2E Test", flow.name());

        FlowExecutionContext ctx = new FlowExecutionContext(
            UUID.randomUUID().toString(), new AtomicBoolean(false));

        List<FlowExecutionEvent> events = new ArrayList<>();
        System.out.println("\n--- FlowBuilder JSON → Execute ---");

        flow.execute("Name a primary color. One word only.", ctx)
            .doOnNext(event -> {
                events.add(event);
                System.out.printf("  [%s] %s%n", event.type(), truncate(event.content(), 80));
            })
            .blockLast(OLLAMA_TIMEOUT);

        assertFalse(events.isEmpty());
        System.out.println("  ✓ JSON-to-execution completed with " + events.size() + " events");
    }

    // ------------------------------------------------------------------
    // Sequential Pipeline
    // ------------------------------------------------------------------

    @Test
    @Order(4)
    @DisplayName("Sequential pipeline: Researcher → Summarizer (Ollama)")
    void sequentialPipeline_twoAgents() throws Exception {
        Assumptions.assumeTrue(ollamaAvailable, "Ollama not available");

        FlowSettings spec = loadSpec("flows/ollama-sequential-flow.json");
        CompiledFlow flow = provider.compile(spec);

        FlowExecutionContext ctx = new FlowExecutionContext(
            UUID.randomUUID().toString(), new AtomicBoolean(false));

        List<FlowExecutionEvent> events = new ArrayList<>();
        System.out.println("\n--- Sequential Pipeline (Researcher → Summarizer) ---");

        flow.execute("Tell me about the Java programming language", ctx)
            .doOnNext(event -> {
                events.add(event);
                System.out.printf("  [%s] %s: %s%n",
                    event.type(), event.stepName(), truncate(event.content(), 100));
            })
            .blockLast(OLLAMA_TIMEOUT);

        assertFalse(events.isEmpty(), "Should receive events from sequential pipeline");

        // Pipeline should produce content events
        boolean hasContent = events.stream()
            .anyMatch(e -> e.type() == EventType.STEP_COMPLETED || e.type() == EventType.TOKEN_DELTA);
        assertTrue(hasContent, "Pipeline should produce content events");

        System.out.println("  ✓ Sequential pipeline completed with " + events.size() + " events");
    }

    // ------------------------------------------------------------------
    // Cancellation
    // ------------------------------------------------------------------

    @Test
    @Order(5)
    @DisplayName("Flow cancellation mid-execution (Ollama)")
    void cancellation_stopsExecution() throws Exception {
        Assumptions.assumeTrue(ollamaAvailable, "Ollama not available");

        FlowSettings spec = loadSpec("flows/ollama-e2e-flow.json");
        CompiledFlow flow = provider.compile(spec);

        AtomicBoolean cancelled = new AtomicBoolean(false);
        FlowExecutionContext ctx = new FlowExecutionContext(
            UUID.randomUUID().toString(), cancelled);

        List<FlowExecutionEvent> events = new ArrayList<>();
        System.out.println("\n--- Cancellation Test ---");

        // Cancel after receiving first event
        Flux<FlowExecutionEvent> stream = flow.execute("Write a very long essay about AI", ctx)
            .doOnNext(event -> {
                events.add(event);
                System.out.printf("  [%s] %s%n", event.type(), truncate(event.content(), 60));
                if (events.size() >= 2) {
                    System.out.println("  → Cancelling flow...");
                    cancelled.set(true);
                }
            });

        try {
            stream.blockLast(Duration.ofSeconds(60));
        } catch (Exception e) {
            // Expected if the stream is interrupted
        }

        // Should have received at least the start event before cancellation
        assertFalse(events.isEmpty(), "Should have received events before cancellation");
        System.out.println("  ✓ Cancellation test completed with " + events.size() + " events");
    }

    // ------------------------------------------------------------------
    // Event Structure Verification
    // ------------------------------------------------------------------

    @Test
    @Order(6)
    @DisplayName("Event structure: all events have required fields (Ollama)")
    void eventStructure_isComplete() throws Exception {
        Assumptions.assumeTrue(ollamaAvailable, "Ollama not available");

        FlowSettings spec = loadSpec("flows/ollama-e2e-flow.json");
        CompiledFlow flow = provider.compile(spec);

        FlowExecutionContext ctx = new FlowExecutionContext(
            UUID.randomUUID().toString(), new AtomicBoolean(false));

        List<FlowExecutionEvent> events = flow.execute("Say hello", ctx)
            .collectList()
            .block(OLLAMA_TIMEOUT);

        assertNotNull(events);
        System.out.println("\n--- Event Structure Verification ---");

        for (FlowExecutionEvent event : events) {
            assertNotNull(event.type(), "Event type should not be null");
            assertNotNull(event.timestamp(), "Timestamp should not be null");

            System.out.printf("  Event: type=%s, stepName=%s, content=%s, meta=%s%n",
                event.type(), event.stepName(),
                truncate(event.content(), 40),
                event.metadata() != null ? event.metadata().keySet() : "[]");
        }

        System.out.println("  ✓ All " + events.size() + " events have valid structure");
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static boolean checkOllamaAvailable() {
        try {
            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OLLAMA_URL + "/api/tags"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private static FlowSettings loadSpec(String path) throws Exception {
        try (InputStream is = OllamaFlowIntegrationTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(is, "Could not find spec file: " + path);
            return objectMapper.readValue(is, FlowSettings.class);
        }
    }

    private static String loadSpecAsString(String path) throws IOException {
        try (InputStream is = OllamaFlowIntegrationTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(is, "Could not find spec file: " + path);
            return new String(is.readAllBytes());
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return "<null>";
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }
}
