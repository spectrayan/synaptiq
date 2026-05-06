package com.synaptiq.shared.domain;

import java.time.Instant;

/**
 * Marker interface for domain events.
 */
public interface DomainEvent {
    Instant occurredAt();
}
