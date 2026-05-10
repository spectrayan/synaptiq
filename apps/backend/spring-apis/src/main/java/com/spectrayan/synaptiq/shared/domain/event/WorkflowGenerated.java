package com.spectrayan.synaptiq.shared.domain.event;

import com.spectrayan.synaptiq.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.Map;

/**
 * Published when a workflow is generated from a natural language prompt.
 */
public record WorkflowGenerated(
        String workflowId,
        String workflowName,
        String tenantId,
        String userId,
        Instant occurredAt
) implements DomainEvent {
    @Override
    public String aggregateId() { return workflowId; }
}
