package com.spectrayan.synaptiq.shared.domain.event;

import com.spectrayan.synaptiq.shared.domain.DomainEvent;

import java.time.Instant;

/**
 * Published when a workflow execution completes successfully.
 */
public record WorkflowCompleted(
        String workflowId,
        String workflowName,
        String tenantId,
        String userId,
        long executionTimeMs,
        Instant occurredAt
) implements DomainEvent {
    @Override
    public String aggregateId() { return workflowId; }
}
