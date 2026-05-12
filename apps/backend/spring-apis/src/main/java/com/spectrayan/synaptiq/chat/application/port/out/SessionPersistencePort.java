package com.spectrayan.synaptiq.chat.application.port.out;

import com.spectrayan.synaptiq.chat.domain.model.Session;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SessionPersistencePort {
    Mono<Session> save(Session session);
    Mono<Session> findBySessionIdAndTenantId(String sessionId, String tenantId);
    Flux<Session> findByTenantId(String tenantId, int limit, int offset);
    Mono<Long> countByTenantId(String tenantId);
    Mono<Void> deleteBySessionIdAndTenantId(String sessionId, String tenantId);
}
