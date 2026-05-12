package com.spectrayan.synaptiq.notification.application.port.out;

import com.spectrayan.synaptiq.notification.domain.model.Notification;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Outbound persistence port for notifications.
 */
public interface NotificationPersistencePort {

    Mono<Notification> save(Notification notification);

    Flux<Notification> findByUserAndTenant(String userId, String tenantId,
                                           boolean unreadOnly, Instant before, int limit);

    Mono<Long> countByUserIdAndTenantIdAndReadFalse(String userId, String tenantId);

    Mono<Void> markAsRead(String id, String userId);

    Mono<Void> markAllReadByUserAndTenant(String userId, String tenantId);

    Mono<Void> deleteByIdAndUserId(String id, String userId);

    Mono<Void> deleteAllByUserAndTenant(String userId, String tenantId);
}
