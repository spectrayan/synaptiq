package com.synaptiq.action.domain.model;

import com.synaptiq.shared.domain.AggregateRoot;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain entity for an executed action (audit log entry).
 * Pure POJO — no framework annotations.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ActionLog extends AggregateRoot {

    private String tenantId;
    private String appId;
    private String actionId;
    private String sessionId;
    private ActionInput inputSnapshot;
    private ActionOutcome outcome;
    private String error;
    private int retryCount;
    private int durationMs;

    /**
     * Typed action input snapshot — replaces raw Map.
     */
    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class ActionInput {
        private String action;
        private String itemId;
        private String userUid;
        @Builder.Default private List<String> fieldNames = new ArrayList<>();
        private String message;
    }
}
