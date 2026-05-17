package com.spectrayan.synaptiq.agentflow.spi;

import java.time.Instant;
import java.util.Map;

/**
 * Normalized event emitted during flow execution.
 * <p>
 * Every provider maps its native events (e.g. ADK {@code Event}, LangGraph
 * state transitions) into this common type so that the executor and
 * upstream consumers remain provider-agnostic.
 *
 * @param type     the event classification
 * @param stepId   identifier of the agent/step that produced this event (nullable for flow-level events)
 * @param stepName human-readable name of the step (nullable)
 * @param content  textual payload — token delta, error message, tool result, etc.
 * @param metadata arbitrary key-value bag for provider-specific data
 * @param timestamp when the event was produced
 */
public record FlowExecutionEvent(
    EventType type,
    String stepId,
    String stepName,
    String content,
    Map<String, Object> metadata,
    Instant timestamp
) {

    /**
     * Classification of flow execution events.
     */
    public enum EventType {
        /** An agent step has started executing. */
        STEP_STARTED,
        /** An agent step has completed successfully. */
        STEP_COMPLETED,
        /** A partial token from LLM streaming. */
        TOKEN_DELTA,
        /** The agent is invoking a tool. */
        TOOL_CALL,
        /** A tool has returned a result. */
        TOOL_RESULT,
        /** The entire flow has completed successfully. */
        FLOW_COMPLETED,
        /** An error occurred during execution. */
        ERROR
    }

    /**
     * Convenience factory for error events.
     */
    public static FlowExecutionEvent error(String stepId, String message) {
        return new FlowExecutionEvent(
            EventType.ERROR, stepId, null, message, Map.of(), Instant.now()
        );
    }

    /**
     * Convenience factory for flow-completed events.
     */
    public static FlowExecutionEvent completed(Map<String, Object> output) {
        return new FlowExecutionEvent(
            EventType.FLOW_COMPLETED, null, null, null, output, Instant.now()
        );
    }
}
