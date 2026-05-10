package com.spectrayan.synaptiq.integration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Records the outcome of a single route execution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResult {

    private String executionId;
    private String routeConfigId;
    private String tenantId;
    private String camelRouteId;

    @Builder.Default
    private ExecutionStatus status = ExecutionStatus.SUCCESS;

    private long durationMs;
    private String errorMessage;
    private Map<String, Object> output;
    private Instant executedAt;

    public enum ExecutionStatus {
        SUCCESS,
        FAILURE,
        TIMEOUT,
        RATE_LIMITED
    }

    public static ExecutionResult success(String routeConfigId, String tenantId,
                                          String camelRouteId, long durationMs,
                                          Map<String, Object> output) {
        return ExecutionResult.builder()
                .routeConfigId(routeConfigId)
                .tenantId(tenantId)
                .camelRouteId(camelRouteId)
                .status(ExecutionStatus.SUCCESS)
                .durationMs(durationMs)
                .output(output)
                .executedAt(Instant.now())
                .build();
    }

    public static ExecutionResult failure(String routeConfigId, String tenantId,
                                          String camelRouteId, long durationMs,
                                          String error) {
        return ExecutionResult.builder()
                .routeConfigId(routeConfigId)
                .tenantId(tenantId)
                .camelRouteId(camelRouteId)
                .status(ExecutionStatus.FAILURE)
                .durationMs(durationMs)
                .errorMessage(error)
                .executedAt(Instant.now())
                .build();
    }
}
