package com.spectrayan.synaptiq.knowledgebase.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import java.util.UUID;

@Getter
@Builder
public class KnowledgeCategory {
    @Builder.Default
    private final String id = UUID.randomUUID().toString();
    
    @NonNull
    private final String tenantId;
    
    @NonNull
    private final String name;
    
    private final String description;
}
