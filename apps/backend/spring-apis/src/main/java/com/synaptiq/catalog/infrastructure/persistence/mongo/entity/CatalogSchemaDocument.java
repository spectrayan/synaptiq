package com.synaptiq.catalog.infrastructure.persistence.mongo.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data @Builder @Document(collection = "catalog_schemas")
public class CatalogSchemaDocument {
    @Id private String id;
    @Indexed private String tenantId;
    private String name;
    @Builder.Default private List<SchemaFieldEmbed> fields = new ArrayList<>();
    private int schemaVersion;
    private boolean active;
    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class SchemaFieldEmbed {
        private String fieldId;
        private String label;
        private String type;
        @Builder.Default private boolean required = false;
        @Builder.Default private boolean searchable = true;
        @Builder.Default private boolean displayable = true;
        @Builder.Default private boolean filterable = false;
        @Builder.Default private int displayOrder = 0;
        @Builder.Default private List<String> enumValues = new ArrayList<>();
    }
}
