package com.spectrayan.synaptiq.shared.domain.event;

import com.spectrayan.synaptiq.shared.domain.DomainEvent;

import java.time.Instant;

/**
 * Published when a data import (CSV, JSON, etc.) completes for a data source.
 */
public record DataImported(
        String tenantId,
        String userId,
        String dataSourceId,
        int recordCount,
        Instant occurredAt
) implements DomainEvent {
    @Override
    public String aggregateId() { return tenantId; }
}
