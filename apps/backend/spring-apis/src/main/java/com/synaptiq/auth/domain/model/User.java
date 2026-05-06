package com.synaptiq.auth.domain.model;

import com.synaptiq.shared.domain.AggregateRoot;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * User aggregate root — pure POJO, no framework annotations.
 */
@Getter @Setter @SuperBuilder @NoArgsConstructor
public class User extends AggregateRoot {
    private String email;
    private String passwordHash;
    private String displayName;
    @lombok.Builder.Default private String role = "tenant_viewer";
    @lombok.Builder.Default private String tenantId = "";
    @lombok.Builder.Default private boolean mustChangePassword = false;
    @lombok.Builder.Default private boolean emailVerified = false;
    @lombok.Builder.Default private boolean disabled = false;
}
