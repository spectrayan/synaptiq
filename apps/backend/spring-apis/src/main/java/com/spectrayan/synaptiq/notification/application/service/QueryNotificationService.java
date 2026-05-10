package com.spectrayan.synaptiq.notification.application.service;

import com.spectrayan.synaptiq.notification.application.port.in.QueryNotificationUseCase;
import com.spectrayan.synaptiq.notification.application.port.out.NotificationPersistencePort;
import com.spectrayan.synaptiq.notification.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Query service for notification retrieval and mutation.
 */
@Service
@RequiredArgsConstructor
public class QueryNotificationService implements QueryNotificationUseCase {

    private final NotificationPersistencePort persistence;

    @Override
    public Flux<Notification> getByUserAndTenant(String userId, String tenantId,
                                                  boolean unreadOnly, Instant before, int limit) {
        return persistence.findByUserAndTenant(userId, tenantId, unreadOnly, before, limit);
    }

    @Override
    public Mono<Long> countUnread(String userId, String tenantId) {
        return persistence.countByUserIdAndTenantIdAndReadFalse(userId, tenantId);
    }

    @Override
    public Mono<Void> markAsRead(String notificationId, String userId) {
        return persistence.markAsRead(notificationId, userId);
    }

    @Override
    public Mono<Void> markAllRead(String userId, String tenantId) {
        return persistence.markAllReadByUserAndTenant(userId, tenantId);
    }

    @Override
    public Mono<Void> dismiss(String notificationId, String userId) {
        return persistence.deleteByIdAndUserId(notificationId, userId);
    }

    @Override
    public Mono<Void> clearAll(String userId, String tenantId) {
        return persistence.deleteAllByUserAndTenant(userId, tenantId);
    }
}
