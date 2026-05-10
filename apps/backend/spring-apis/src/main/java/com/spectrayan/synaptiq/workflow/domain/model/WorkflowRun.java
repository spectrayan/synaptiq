package com.spectrayan.synaptiq.workflow.domain.model;

import com.spectrayan.synaptiq.shared.domain.AggregateRoot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Aggregate root for a single workflow execution run.
 * Tracks per-node status, outputs, timing, and final result.
 */
@Getter @Setter @SuperBuilder @NoArgsConstructor
public class WorkflowRun extends AggregateRoot {
    private String workflowId;
    private String tenantId;

    @Builder.Default
    private String status = "PENDING"; // PENDING, RUNNING, COMPLETED, ERROR

    private Instant startedAt;
    private Instant completedAt;
    private Long totalDurationMs;

    /** Per-node execution details keyed by node ID. */
    @Builder.Default
    private Map<String, NodeRunDetail> nodes = new HashMap<>();

    private String result;
    private String error;
    private String failedNodeId;

    @Data
    @lombok.Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeRunDetail {
        private String nodeId;
        private String nodeName;

        @lombok.Builder.Default
        private String status = "PENDING";

        private Instant startedAt;
        private Instant completedAt;
        private Long durationMs;
        private String output;
        private String error;
    }
}
