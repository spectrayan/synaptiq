package com.spectrayan.synaptiq.knowledgebase.infrastructure.persistence.mongo;

import com.spectrayan.synaptiq.knowledgebase.domain.model.DocumentSourceType;
import com.spectrayan.synaptiq.knowledgebase.domain.model.DocumentStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "knowledge_documents")
public class KnowledgeDocumentEntity {
    @Id
    private String id;
    
    @Indexed
    private String tenantId;
    
    private String fileName;
    
    @Indexed
    private String categoryId;
    
    private List<String> tags;
    
    private DocumentStatus status;
    private DocumentSourceType sourceType;
    private long sizeBytes;
    private Instant createdAt;
}
