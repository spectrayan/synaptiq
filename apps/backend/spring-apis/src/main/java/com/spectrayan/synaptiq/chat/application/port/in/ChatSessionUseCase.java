package com.spectrayan.synaptiq.chat.application.port.in;

import com.spectrayan.synaptiq.chat.domain.model.Session;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatSessionUseCase {
    Mono<Session> createOrGetSession(String tenantId, String sessionId, Session.SessionMetadata metadata);
    Flux<Session> listSessions(String tenantId, int limit, int offset);
    Mono<Session> getSession(String tenantId, String sessionId);
    Mono<Session> updateSession(String tenantId, String sessionId, String title);
    Mono<Void> deleteSession(String tenantId, String sessionId);
    Mono<Void> resetSession(String tenantId, String sessionId);
    Mono<Long> countSessions(String tenantId);
}
