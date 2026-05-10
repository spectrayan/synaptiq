package com.spectrayan.synaptiq.auth.infrastructure.persistence.mongo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter @Setter
@Document(collection = "scopes")
public class ScopeDocument {
    @Id
    private String slug;
    private String displayName;
    private String description;
    private String resource;
    private String action;
}
