package com.spectrayan.synaptiq.knowledgebase.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * Aggregate root representing the Knowledge Base configuration for a tenant.
 */
@Getter
@Builder
public class KnowledgeBase {
    @NonNull
    private final String tenantId;
    private final boolean enabled;
    
    public KnowledgeBase enable() {
        return KnowledgeBase.builder()
                .tenantId(this.tenantId)
                .enabled(true)
                .build();
    }
}
