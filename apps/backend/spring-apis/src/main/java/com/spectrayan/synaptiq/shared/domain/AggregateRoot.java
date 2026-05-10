package com.spectrayan.synaptiq.shared.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for aggregate roots.
 * <p>
 * Pure POJO — NO framework annotations (no @Document, no @Id).
 * MongoDB-specific annotations live on Document classes in the infrastructure layer.
 * <p>
 * Domain events are accumulated via {@link #registerEvent(DomainEvent)} and
 * drained after persistence via {@link #clearDomainEvents()}.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class AggregateRoot {

    private String id;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @lombok.Builder.Default
    private final transient List<DomainEvent> domainEvents = new ArrayList<>();

    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public List<DomainEvent> clearDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }
}
