package com.spectrayan.synaptiq.knowledgebase.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import java.util.UUID;

@Getter
@Builder
public class DocumentChunk {
    @Builder.Default
    private final String id = UUID.randomUUID().toString();
    
    @NonNull
    private final String documentId;
    
    @NonNull
    private final String tenantId;
    
    private final String categoryId;
    
    @NonNull
    private final String content;
    
    private final Double similarityScore;
}
