package com.synaptiq.workflow.domain.model;

import java.time.Instant;
import java.util.Map;

/**
 * Typed flow execution event — replaces {@code Map<String, Object>}
 * for workflow execution streaming.
 */
public sealed interface FlowEvent {

    /** Flow execution has started. */
    record Started(String runId, Instant timestamp) implements FlowEvent {}

    /** A step in the flow has completed. */
    record StepCompleted(
        String runId,
        String stepId,
        String stepName,
        String status,
        Map<String, Object> output,
        Instant timestamp
    ) implements FlowEvent {}

    /** LLM is streaming a partial token. */
    record TokenDelta(String runId, String stepId, String delta) implements FlowEvent {}

    /** Flow execution has completed successfully. */
    record Completed(
        String runId,
        Map<String, Object> finalOutput,
        long durationMs,
        Instant timestamp
    ) implements FlowEvent {}

    /** Flow execution has failed. */
    record Failed(
        String runId,
        String error,
        String failedStepId,
        Instant timestamp
    ) implements FlowEvent {}
}
