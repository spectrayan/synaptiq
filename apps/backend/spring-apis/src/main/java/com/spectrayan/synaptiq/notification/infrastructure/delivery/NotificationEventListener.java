package com.spectrayan.synaptiq.notification.infrastructure.delivery;

import com.spectrayan.synaptiq.notification.application.port.in.CreateNotificationUseCase;
import com.spectrayan.synaptiq.notification.domain.model.NotificationEventType;
import com.spectrayan.synaptiq.shared.domain.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Central notification event listener.
 * <p>
 * Subscribes to domain events from all modules and delegates to
 * {@link CreateNotificationUseCase} which handles:
 * - Persist to MongoDB
 * - Future: SSE real-time push / email
 * <p>
 * Message content is derived from {@link NotificationEventType#resolveMessage(Map)}
 * templates — no hardcoded strings in this listener.
 * <p>
 * All operations are fire-and-forget — failures never impact the originating business logic.
 * {@code @Async} ensures listeners execute on a separate thread pool.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventListener {

    private final CreateNotificationUseCase createNotificationUseCase;

    @Async
    @EventListener
    void on(WorkflowGenerated event) {
        var payload = payload("workflowId", event.workflowId(), "workflowName", event.workflowName());
        fireNotification(event.tenantId(), event.userId(), NotificationEventType.WORKFLOW_GENERATED, payload);
    }

    @Async
    @EventListener
    void on(WorkflowCompleted event) {
        var payload = payload("workflowId", event.workflowId(), "workflowName", event.workflowName(),
                "executionTimeMs", event.executionTimeMs());
        fireNotification(event.tenantId(), event.userId(), NotificationEventType.WORKFLOW_COMPLETED, payload);
    }

    @Async
    @EventListener
    void on(WorkflowFailed event) {
        var payload = payload("workflowId", event.workflowId(), "workflowName", event.workflowName(),
                "error", event.error());
        fireNotification(event.tenantId(), event.userId(), NotificationEventType.WORKFLOW_FAILED, payload);
    }

    @Async
    @EventListener
    void on(DataImported event) {
        var payload = payload("count", event.recordCount(), "dataSourceId", event.dataSourceId());
        fireNotification(event.tenantId(), event.userId(), NotificationEventType.DATA_IMPORTED, payload);
    }

    @Async
    @EventListener
    void on(LlmCallFailed event) {
        var payload = payload("operation", event.operation(), "error", event.error());
        fireNotification(event.tenantId(), event.userId(), NotificationEventType.LLM_ERROR, payload);
    }

    // ── Internal ──

    /**
     * Fire-and-forget notification creation.
     * Message is resolved from the event type's template.
     */
    private void fireNotification(String tenantId, String userId, NotificationEventType type,
                                   Map<String, Object> payload) {
        String message = type.resolveMessage(payload);
        createNotificationUseCase.createForUser(tenantId, userId, type, message, payload)
                .doOnError(e -> log.warn("Notification fan-out failed: {}", e.getMessage()))
                .onErrorComplete()
                .subscribe();
    }

    /**
     * Build a null-safe payload map.
     */
    private Map<String, Object> payload(Object... kvPairs) {
        var map = new HashMap<String, Object>();
        for (int i = 0; i < kvPairs.length - 1; i += 2) {
            String key = String.valueOf(kvPairs[i]);
            Object val = kvPairs[i + 1];
            map.put(key, val != null ? val : "");
        }
        return map;
    }
}
