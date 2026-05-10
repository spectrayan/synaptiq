package com.spectrayan.synaptiq.notification.application.port.in;

import com.spectrayan.synaptiq.notification.domain.model.Notification;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Inbound port for querying and mutating notifications (read, dismiss, count).
 */
public interface QueryNotificationUseCase {

    /**
     * Cursor-based fetch of notifications for a user within a tenant.
     */
    Flux<Notification> getByUserAndTenant(String userId, String tenantId,
                                           boolean unreadOnly, Instant before, int limit);

    /**
     * Count unread notifications for the bell badge.
     */
    Mono<Long> countUnread(String userId, String tenantId);

    Mono<Void> markAsRead(String notificationId, String userId);

    Mono<Void> markAllRead(String userId, String tenantId);

    Mono<Void> dismiss(String notificationId, String userId);

    Mono<Void> clearAll(String userId, String tenantId);
}
