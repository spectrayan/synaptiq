package com.spectrayan.synaptiq.shared.domain.event;

import com.spectrayan.synaptiq.shared.domain.DomainEvent;

import java.time.Instant;

/**
 * Published when an LLM call fails (for alerting/notification purposes).
 */
public record LlmCallFailed(
        String tenantId,
        String userId,
        String operation,
        String error,
        Instant occurredAt
) implements DomainEvent {
    @Override
    public String aggregateId() { return tenantId; }
}
