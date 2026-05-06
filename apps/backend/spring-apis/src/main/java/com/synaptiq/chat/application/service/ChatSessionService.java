package com.synaptiq.chat.application.service;

import com.synaptiq.chat.application.port.in.ChatSessionUseCase;
import com.synaptiq.chat.application.port.out.SessionPersistencePort;
import com.synaptiq.chat.domain.model.Session;
import com.synaptiq.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ChatSessionService implements ChatSessionUseCase {
    private final SessionPersistencePort persistence;

    @Override
    public Mono<Session> createOrGetSession(String tenantId, String sessionId, Session.SessionMetadata meta) {
        return persistence.findBySessionIdAndTenantId(sessionId, tenantId)
            .switchIfEmpty(Mono.defer(() -> {
                var session = Session.builder().sessionId(sessionId).tenantId(tenantId).metadata(meta).build();
                return persistence.save(session);
            }));
    }

    @Override
    public Flux<Session> listSessions(String tenantId, int limit, int offset) {
        return persistence.findByTenantId(tenantId, limit, offset);
    }

    @Override
    public Mono<Session> getSession(String tenantId, String sessionId) {
        return persistence.findBySessionIdAndTenantId(sessionId, tenantId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Session not found")));
    }

    @Override
    public Mono<Session> updateSession(String tenantId, String sessionId, String title) {
        return getSession(tenantId, sessionId).flatMap(s -> {
            if (title != null) s.setTitle(title);
            s.setUpdatedAt(Instant.now());
            return persistence.save(s);
        });
    }

    @Override
    public Mono<Void> deleteSession(String tenantId, String sessionId) {
        return persistence.deleteBySessionIdAndTenantId(sessionId, tenantId);
    }

    @Override
    public Mono<Void> resetSession(String tenantId, String sessionId) {
        return getSession(tenantId, sessionId).flatMap(s -> {
            s.setTurns(new ArrayList<>());
            s.setActiveFilters(new ArrayList<>());
            s.setUpdatedAt(Instant.now());
            return persistence.save(s);
        }).then();
    }

    @Override
    public Mono<Long> countSessions(String tenantId) {
        return persistence.countByTenantId(tenantId);
    }
}
