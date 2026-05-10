package com.synaptiq.workflow.infrastructure.persistence.mongo.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data @Builder @Document(collection = "workflow_runs")
public class WorkflowRunDocument {
    @Id private String id;
    @Indexed private String workflowId;
    @Indexed private String tenantId;
    private String status;
    private Instant startedAt;
    private Instant completedAt;
    private Long totalDurationMs;
    @Builder.Default
    private Map<String, NodeRunDetailDoc> nodes = new HashMap<>();
    private String result;
    private String error;
    private String failedNodeId;
    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;

    @Data @Builder
    public static class NodeRunDetailDoc {
        private String nodeId;
        private String nodeName;
        private String status;
        private Instant startedAt;
        private Instant completedAt;
        private Long durationMs;
        private String output;
        private String error;
    }
}
