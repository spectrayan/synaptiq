package com.spectrayan.synaptiq.agentflow.provider.adk;

import com.google.adk.agents.BaseAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.Runner;
import com.google.adk.sessions.BaseSessionService;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.spectrayan.synaptiq.agentflow.builder.models.settings.FlowSettings;
import com.spectrayan.synaptiq.agentflow.spi.CompiledFlow;
import com.spectrayan.synaptiq.agentflow.spi.FlowExecutionContext;
import com.spectrayan.synaptiq.agentflow.spi.FlowExecutionEvent;
import com.spectrayan.synaptiq.agentflow.spi.FlowExecutionEvent.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Google ADK implementation of {@link CompiledFlow}.
 * <p>
 * Wraps an ADK {@link BaseAgent} and executes it via {@link Runner},
 * streaming events as normalized {@link FlowExecutionEvent} instances.
 * <p>
 * Features:
 * <ul>
 *   <li>Configurable session persistence (InMemory, Firestore, VertexAI)</li>
 *   <li>Context caching via ADK's ContextCacheConfig</li>
 *   <li>LLM token streaming interception → TOKEN_DELTA events</li>
 *   <li>Cooperative cancellation via {@link FlowExecutionContext}</li>
 * </ul>
 */
public class AdkCompiledFlow implements CompiledFlow {

    private static final Logger log = LoggerFactory.getLogger(AdkCompiledFlow.class);

    private final String flowName;
    private final BaseAgent rootAgent;
    private final FlowSettings settings;

    public AdkCompiledFlow(String flowName, BaseAgent rootAgent, FlowSettings settings) {
        this.flowName = flowName;
        this.rootAgent = rootAgent;
        this.settings = settings;
    }

    @Override
    public String name() {
        return flowName;
    }

    @Override
    public Flux<FlowExecutionEvent> execute(Object input, FlowExecutionContext context) {
        return Flux.<FlowExecutionEvent>create(sink -> {
            try {
                // 1. Create session service based on configuration
                BaseSessionService sessionService = AdkSessionServiceFactory.create(settings.getSessionConfig());

                // 2. Create runner
                Runner runner = new Runner(rootAgent, flowName, null, sessionService);

                // 3. Create session
                String userId = "synaptiq-" + context.runId();
                String sessionId = UUID.randomUUID().toString();
                Session session = sessionService.createSession(flowName, userId, null, sessionId).blockingGet();

                // 4. Emit start event
                sink.next(new FlowExecutionEvent(
                    EventType.STEP_STARTED, null, flowName,
                    "Flow execution started", Map.of(), Instant.now()
                ));

                // 5. Prepare user input
                Content userMessage = Content.fromParts(
                    Part.fromText(input != null ? input.toString() : "")
                );

                // 6. Execute and stream events via RxJava Flowable → Reactor Flux bridge
                Map<String, Object> finalOutput = new HashMap<>();

                runner.runAsync(userId, session.id(), userMessage)
                    .doOnNext(adkEvent -> {
                        if (context.isCancelled()) {
                            return;
                        }

                        FlowExecutionEvent mapped = mapAdkEvent(adkEvent);
                        if (mapped != null) {
                            sink.next(mapped);

                            if (mapped.type() == EventType.STEP_COMPLETED && mapped.metadata() != null) {
                                finalOutput.putAll(mapped.metadata());
                            }
                        }
                    })
                    .doOnComplete(() -> {
                        sink.next(FlowExecutionEvent.completed(finalOutput));
                        sink.complete();
                    })
                    .doOnError(err -> {
                        sink.next(FlowExecutionEvent.error(null, err.getMessage()));
                        sink.complete();
                    })
                    .subscribe();

            } catch (Exception e) {
                log.error("Failed to execute flow '{}': {}", flowName, e.getMessage(), e);
                sink.next(FlowExecutionEvent.error(null, e.getMessage()));
                sink.complete();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Map an ADK {@link Event} to a normalized {@link FlowExecutionEvent}.
     * Intercepts different event types including token deltas for streaming.
     */
    private FlowExecutionEvent mapAdkEvent(Event adkEvent) {
        if (adkEvent == null) {
            return null;
        }

        String agentName = adkEvent.author() != null ? adkEvent.author() : "unknown";
        String eventId = adkEvent.id() != null ? adkEvent.id() : UUID.randomUUID().toString();
        Map<String, Object> metadata = new HashMap<>();

        // Extract content from the event (returns Optional<Content>)
        Optional<Content> contentOpt = adkEvent.content();
        if (contentOpt.isPresent()) {
            Content content = contentOpt.get();
            if (content.parts().isPresent() && !content.parts().get().isEmpty()) {
                StringBuilder textBuilder = new StringBuilder();
                for (Part part : content.parts().get()) {
                    // Check for text
                    if (part.text().isPresent()) {
                        textBuilder.append(part.text().get());
                    }
                    // Check for function calls (tool invocations)
                    if (part.functionCall().isPresent()) {
                        var fc = part.functionCall().get();
                        metadata.put("functionName", fc.name().orElse("unknown"));
                        metadata.put("functionArgs", fc.args().orElse(Map.of()));
                        return new FlowExecutionEvent(
                            EventType.TOOL_CALL, eventId, agentName,
                            "Calling tool: " + fc.name().orElse("unknown"),
                            metadata, Instant.now()
                        );
                    }
                    // Check for function responses (tool results)
                    if (part.functionResponse().isPresent()) {
                        var fr = part.functionResponse().get();
                        metadata.put("functionName", fr.name().orElse("unknown"));
                        metadata.put("functionResponse", fr.response().orElse(Map.of()));
                        return new FlowExecutionEvent(
                            EventType.TOOL_RESULT, eventId, agentName,
                            "Tool result from: " + fr.name().orElse("unknown"),
                            metadata, Instant.now()
                        );
                    }
                }

                String text = textBuilder.toString();
                if (!text.isEmpty()) {
                    // Check if this is a partial/streaming response
                    boolean isPartial = adkEvent.partial().orElse(false);
                    if (isPartial) {
                        return new FlowExecutionEvent(
                            EventType.TOKEN_DELTA, eventId, agentName,
                            text, metadata, Instant.now()
                        );
                    }

                    // Full step completion
                    metadata.put("output", text);
                    return new FlowExecutionEvent(
                        EventType.STEP_COMPLETED, eventId, agentName,
                        text, metadata, Instant.now()
                    );
                }
            }
        }

        // Fallback: emit as step started for events without parseable content
        return new FlowExecutionEvent(
            EventType.STEP_STARTED, eventId, agentName,
            "Agent step: " + agentName, metadata, Instant.now()
        );
    }
}
