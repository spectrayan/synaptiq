package com.synaptiq.auth.infrastructure.persistence.mongo.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data @NoArgsConstructor
@Document(collection = "users")
public class UserDocument {
    @Id private String id;
    @Indexed(unique = true) private String email;
    private String passwordHash;
    private String displayName;
    private String role;
    private String tenantId;
    private boolean mustChangePassword;
    private boolean emailVerified;
    private boolean disabled;
    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;
}
