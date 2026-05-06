package com.synaptiq.catalog.domain.model;

import com.synaptiq.shared.domain.AggregateRoot;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class CatalogSchema extends AggregateRoot {
    private String tenantId;
    @Builder.Default private String name = "Default Schema";
    private List<SchemaField> fields;
    @Builder.Default private int schemaVersion = 1;
    @Builder.Default private boolean active = true;

    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class SchemaField {
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
