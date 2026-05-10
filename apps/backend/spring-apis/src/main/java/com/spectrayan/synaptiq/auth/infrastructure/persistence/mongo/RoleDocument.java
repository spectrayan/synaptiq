package com.spectrayan.synaptiq.auth.infrastructure.persistence.mongo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Getter @Setter
@Document(collection = "roles")
public class RoleDocument {
    @Id
    private String id;
    @Indexed(unique = true)
    private String slug;
    private String tenantId;
    private String displayName;
    private String description;
    private boolean systemRole;
    private String roleType;
    private Set<String> scopeSlugs;
}
