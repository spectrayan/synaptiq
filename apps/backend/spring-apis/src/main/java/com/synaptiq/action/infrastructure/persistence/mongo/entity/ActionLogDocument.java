package com.synaptiq.action.infrastructure.persistence.mongo.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document for the ActionLog aggregate.
 * Framework annotations live HERE — never on domain models.
 */
@Data
@Builder
@Document(collection = "action_logs")
public class ActionLogDocument {

    @Id
    private String id;
    private String tenantId;
    private String appId;
    private String actionId;
    private String sessionId;
    private ActionInputEmbed inputSnapshot;
    private String outcome;
    private String error;
    private int retryCount;
    private int durationMs;
    @CreatedDate
    private Instant createdAt;

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class ActionInputEmbed {
        private String action;
        private String itemId;
        private String userUid;
        private java.util.List<String> fieldNames;
        private String message;
    }
}
