package com.spectrayan.synaptiq.notification.application.port.in;

import com.spectrayan.synaptiq.notification.domain.model.NotificationEventType;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Inbound port for fan-out notification creation.
 * Called by event listeners when domain events occur.
 */
public interface CreateNotificationUseCase {

    /**
     * Create a notification for a specific user in a tenant.
     */
    Mono<Void> createForUser(String tenantId, String userId, NotificationEventType eventType,
                              String message, Map<String, Object> payload);
}
