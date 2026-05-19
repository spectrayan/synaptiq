package com.spectrayan.synaptiq.knowledgebase.infrastructure.persistence.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "knowledge_categories")
public class KnowledgeCategoryEntity {
    @Id
    private String id;
    
    @Indexed
    private String tenantId;
    
    private String name;
    private String description;
}
