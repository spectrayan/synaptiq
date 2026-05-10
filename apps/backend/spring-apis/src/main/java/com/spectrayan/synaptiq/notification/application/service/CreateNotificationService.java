package com.spectrayan.synaptiq.notification.application.service;

import com.spectrayan.synaptiq.notification.application.port.in.CreateNotificationUseCase;
import com.spectrayan.synaptiq.notification.application.port.out.NotificationPersistencePort;
import com.spectrayan.synaptiq.notification.domain.model.Notification;
import com.spectrayan.synaptiq.notification.domain.model.NotificationEventType;
import com.spectrayan.synaptiq.notification.infrastructure.delivery.SseNotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Application service for notification creation.
 * <p>
 * Creates, persists, and pushes notifications for individual users.
 * After saving to MongoDB, the notification is pushed via SSE for
 * real-time delivery to the frontend bell icon.
 * <p>
 * All operations are fire-and-forget — failures never impact the originating business logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateNotificationService implements CreateNotificationUseCase {

    private final NotificationPersistencePort notifRepo;
    private final SseNotificationPort sse;

    @Override
    public Mono<Void> createForUser(String tenantId, String userId, NotificationEventType eventType,
                                     String message, Map<String, Object> payload) {
        Notification notif = Notification.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .tenantId(tenantId)
                .type(eventType.getKey())
                .title(eventType.getTitle())
                .message(message)
                .icon(eventType.getIcon())
                .payload(payload)
                .read(false)
                .createdAt(Instant.now())
                .build();

        return notifRepo.save(notif)
                .doOnSuccess(saved -> {
                    log.info("Notification created for user={}, tenant={}, event={}",
                            userId, tenantId, eventType.getKey());
                    // Push via SSE for real-time delivery to the UI
                    pushSse(tenantId, eventType, message, payload);
                })
                .then()
                .onErrorResume(e -> {
                    log.error("Failed to create notification for user={}, event={}: {}",
                            userId, eventType.getKey(), e.getMessage());
                    return Mono.empty();
                });
    }

    // ── SSE push ─────────────────────────────────────────────────────

    /**
     * Best-effort SSE push after notification is persisted.
     * If no subscriber is connected to the topic, the event is silently dropped.
     * The frontend's initial-load-on-connect guarantees eventual delivery.
     */
    private void pushSse(String tenantId, NotificationEventType eventType,
                         String message, Map<String, Object> payload) {
        try {
            var ssePayload = new HashMap<>(payload);
            ssePayload.put("eventType", eventType.getKey());
            ssePayload.put("_title", eventType.getTitle());
            ssePayload.put("_message", message);
            ssePayload.put("_icon", eventType.getIcon());
            sse.emit("tenant-" + tenantId, eventType.getKey(), ssePayload);
        } catch (Exception e) {
            log.debug("SSE push skipped (no subscribers): {}", e.getMessage());
        }
    }
}
