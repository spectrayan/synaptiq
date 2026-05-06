package com.synaptiq.catalog.infrastructure.persistence.mongo.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data @Builder @Document(collection = "catalog_items")
public class CatalogItemDocument {
    @Id private String id;
    @Indexed private String tenantId;
    private String status;
    private Map<String, Object> data;
    private List<Double> embedding;
    private String embeddingModel;
    private Instant embeddedAt;
    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;
}
