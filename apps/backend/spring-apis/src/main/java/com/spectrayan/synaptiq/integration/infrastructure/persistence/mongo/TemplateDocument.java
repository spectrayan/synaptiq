package com.spectrayan.synaptiq.integration.infrastructure.persistence.mongo;

import com.spectrayan.synaptiq.integration.model.TemplateDescriptor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB document for persisted integration templates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "integration_templates")
public class TemplateDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String templateId;

    private String displayName;
    private String description;
    private String icon;
    private String category;
    private String connectorType;

    @Builder.Default
    private List<TemplateDescriptor.ParameterDefinition> parameters = new ArrayList<>();

    @Builder.Default
    private boolean requiresCredential = false;

    @Builder.Default
    private boolean builtIn = false;

    /** Null = global, otherwise tenant-scoped. */
    @Indexed
    private String tenantId;

    /** Camel YAML DSL for this template. */
    private String routeYaml;

    private Instant createdAt;
    private Instant updatedAt;
}
