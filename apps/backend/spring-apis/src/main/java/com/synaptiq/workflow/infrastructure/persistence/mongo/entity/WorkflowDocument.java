package com.synaptiq.workflow.infrastructure.persistence.mongo.entity;

import com.synaptiq.agentflow.builder.models.settings.FlowSettings;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data @Builder @Document(collection = "workflows")
public class WorkflowDocument {
    @Id private String id;
    @Indexed private String tenantId;
    private String appId;
    private FlowSettings spec;
    private String shareToken;
    private boolean isPublic;
    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;
}
