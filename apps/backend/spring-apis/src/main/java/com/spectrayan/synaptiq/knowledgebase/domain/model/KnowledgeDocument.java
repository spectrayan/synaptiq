package com.spectrayan.synaptiq.knowledgebase.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class KnowledgeDocument {
    @Builder.Default
    private final String id = UUID.randomUUID().toString();
    
    @NonNull
    private final String tenantId;
    
    @NonNull
    private final String fileName;
    
    private final String categoryId;
    
    private final List<String> tags;
    
    @Builder.Default
    private final DocumentStatus status = DocumentStatus.PENDING;
    
    @NonNull
    private final DocumentSourceType sourceType;
    
    private final long sizeBytes;
    
    @Builder.Default
    private final Instant createdAt = Instant.now();
    
    public KnowledgeDocument markProcessing() {
        return this.toBuilder().status(DocumentStatus.PROCESSING).build();
    }
    
    public KnowledgeDocument markReady() {
        return this.toBuilder().status(DocumentStatus.READY).build();
    }
    
    public KnowledgeDocument markFailed() {
        return this.toBuilder().status(DocumentStatus.FAILED).build();
    }
}
