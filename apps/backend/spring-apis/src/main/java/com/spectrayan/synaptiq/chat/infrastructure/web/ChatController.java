package com.spectrayan.synaptiq.chat.infrastructure.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spectrayan.synaptiq.chat.application.port.in.ChatMessageUseCase;
import com.spectrayan.synaptiq.chat.application.port.in.ChatSessionUseCase;
import com.spectrayan.synaptiq.infrastructure.in.web.api.ChatApi;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController implements ChatApi {
    private final ChatSessionUseCase sessionUseCase;
    private final ChatMessageUseCase messageUseCase;
    private final ChatDtoMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<ResponseEntity<String>> postChatMessage(Mono<ChatMessageRequest> req, String xTenantID, ServerWebExchange exchange) {
        exchange.getResponse().getHeaders().setContentType(org.springframework.http.MediaType.TEXT_EVENT_STREAM);
        return req.flatMap(r -> 
            exchange.getResponse().writeWith(
                messageUseCase.streamMessage(xTenantID, r.getSessionId(), r.getMessage(), r.getModelOverride())
                    .map(content -> {
                        String jsonChunk = toJson(Map.of("content", content));
                        String event = "data: " + jsonChunk + "\n\n";
                        return exchange.getResponse().bufferFactory().wrap(event.getBytes());
                    })
                    .concatWith(Mono.fromCallable(() ->
                        exchange.getResponse().bufferFactory().wrap(
                            ("data: " + toJson(Map.of("type", "done")) + "\n\n").getBytes())))
            )
        ).then(Mono.empty());
    }

    @Override
    public Mono<ResponseEntity<SessionResponse>> createSession(Mono<CreateSessionRequest> req, String xTenantID, ServerWebExchange exchange) {
        return req.flatMap(r -> {
            var meta = r.getMetadata() != null
                ? com.spectrayan.synaptiq.chat.domain.model.Session.SessionMetadata.builder()
                    .deviceType(r.getMetadata() != null ? (String) r.getMetadata().get("deviceType") : null)
                    .locale(r.getMetadata() != null ? (String) r.getMetadata().get("locale") : null)
                    .build()
                : null;
            return sessionUseCase.createOrGetSession(xTenantID, r.getSessionId(), meta)
                .map(mapper::toSessionDto)
                .map(d -> ResponseEntity.status(201).body(d));
        });
    }

    @Override
    public Mono<ResponseEntity<SessionListResponse>> listSessions(String xTenantID, Integer limit, Integer offset, ServerWebExchange exchange) {
        int lm = limit != null ? limit : 20;
        int off = offset != null ? offset : 0;
        return sessionUseCase.listSessions(xTenantID, lm, off)
            .map(mapper::toSummaryDto)
            .collectList()
            .zipWith(sessionUseCase.countSessions(xTenantID))
            .map(t -> ResponseEntity.ok(mapper.toListDto(t.getT1(), t.getT2())));
    }

    @Override
    public Mono<ResponseEntity<SessionResponse>> updateSession(String sessionId, Mono<UpdateSessionRequest> req, String xTenantID, ServerWebExchange exchange) {
        return req.flatMap(r -> sessionUseCase.updateSession(xTenantID, sessionId, r.getTitle())
            .map(mapper::toSessionDto)
            .map(ResponseEntity::ok));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteSession(String sessionId, String xTenantID, ServerWebExchange exchange) {
        return sessionUseCase.deleteSession(xTenantID, sessionId)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @Override
    public Mono<ResponseEntity<SessionHistoryResponse>> getSessionHistory(String sessionId, String xTenantID, ServerWebExchange exchange) {
        return sessionUseCase.getSession(xTenantID, sessionId)
            .map(s -> ResponseEntity.ok(new SessionHistoryResponse()
                .sessionId(s.getSessionId())
                .total(s.getTurns() != null ? s.getTurns().size() : 0)));
    }

    @Override
    public Mono<ResponseEntity<Void>> resetSession(String sessionId, String xTenantID, ServerWebExchange exchange) {
        return sessionUseCase.resetSession(xTenantID, sessionId)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    // ── Helpers ──

    /**
     * Serialize an object to JSON using Jackson's ObjectMapper.
     * Replaces the previous hand-rolled JSON escaping that was fragile
     * and would break on special characters, unicode, etc.
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize SSE chunk to JSON", e);
            return "{\"error\":\"serialization_failed\"}";
        }
    }
}

