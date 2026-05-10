package com.spectrayan.synaptiq.notification.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Core notification entity.
 * Represents a single notification delivered to a specific user within a tenant context.
 * Pure POJO — no framework annotations.
 */
@Data
@Builder
public class Notification {

    private String id;
    private String userId;
    private String tenantId;
    private String type;                    // e.g. "workflow.completed"
    private String title;                   // "Workflow Completed"
    private String message;                 // "Workflow 'Research Pipeline' completed successfully"
    private String icon;                    // Material icon name
    private Map<String, Object> payload;    // Raw event data
    private boolean read;
    private Instant createdAt;

    public void markAsRead() {
        this.read = true;
    }
}
