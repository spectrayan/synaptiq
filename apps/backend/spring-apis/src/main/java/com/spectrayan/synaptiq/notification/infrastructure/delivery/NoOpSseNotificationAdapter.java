package com.spectrayan.synaptiq.notification.infrastructure.delivery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * No-op SSE notification adapter — used until the SSE server library is integrated.
 * <p>
 * Logs emissions at DEBUG level. When the Spectrayan SSE Server library
 * ({@code com.spectrayan.sse:sse-server}) is on the classpath, the real
 * {@link SseNotificationAdapter} will replace this via {@code @Primary}.
 * <p>
 * This ensures domain services can emit events now without waiting
 * for SSE infrastructure.
 */
@Slf4j
@Component
public class NoOpSseNotificationAdapter implements SseNotificationPort {

    @Override
    public <T> void emit(String topic, String eventName, T payload) {
        log.debug("SSE emit (no-op): topic={}, event={}, payload={}", topic, eventName, payload);
    }

    @Override
    public <T> void emit(String topic, T payload) {
        log.debug("SSE emit (no-op): topic={}, payload={}", topic, payload);
    }

    @Override
    public <T> void broadcast(String eventName, T payload) {
        log.debug("SSE broadcast (no-op): event={}, payload={}", eventName, payload);
    }
}
