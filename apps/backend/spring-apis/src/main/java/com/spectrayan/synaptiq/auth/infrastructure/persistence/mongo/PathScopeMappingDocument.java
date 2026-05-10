package com.spectrayan.synaptiq.auth.infrastructure.persistence.mongo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter @Setter
@Document(collection = "path_scope_mappings")
public class PathScopeMappingDocument {
    @Id
    private String id;
    private String httpMethod;
    private String pathPattern;
    private String requiredScope;
}
