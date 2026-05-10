package com.spectrayan.synaptiq.shared.domain.event;

import com.spectrayan.synaptiq.shared.domain.DomainEvent;

import java.time.Instant;

/**
 * Published when a workflow execution fails.
 */
public record WorkflowFailed(
        String workflowId,
        String workflowName,
        String tenantId,
        String userId,
        String error,
        Instant occurredAt
) implements DomainEvent {
    @Override
    public String aggregateId() { return workflowId; }
}
