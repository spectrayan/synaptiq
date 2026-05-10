package com.spectrayan.synaptiq.notification.infrastructure.web;

import com.spectrayan.synaptiq.infrastructure.in.web.api.NotificationsApi;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.NotificationCountResponse;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.NotificationListResponse;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.NotificationResponse;
import com.spectrayan.synaptiq.notification.application.port.in.QueryNotificationUseCase;
import com.spectrayan.synaptiq.notification.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * REST controller for notification management.
 * <p>
 * Implements the generated {@link NotificationsApi} interface from the OpenAPI spec.
 * All annotations (path, method, params, swagger docs) come from the generated interface.
 * This controller only contains the business delegation logic.
 * <p>
 * Fully reactive — no blocking calls.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationsApi {

    private final QueryNotificationUseCase queryUseCase;

    @Override
    public Mono<ResponseEntity<NotificationListResponse>> listNotifications(
            String xTenantID,
            Boolean unreadOnly,
            OffsetDateTime before,
            Integer limit,
            ServerWebExchange exchange) {

        String tenantId = xTenantID != null ? xTenantID : "";
        boolean filterUnread = Boolean.TRUE.equals(unreadOnly);
        Instant cursor = before != null ? before.toInstant() : null;
        int fetchLimit = Math.min(limit != null ? limit : 20, 50);

        return resolveUserId(exchange)
                .flatMap(userId ->
                    queryUseCase.getByUserAndTenant(userId, tenantId, filterUnread, cursor, fetchLimit + 1)
                            .collectList()
                            .map(items -> {
                                boolean hasMore = items.size() > fetchLimit;
                                List<Notification> page = hasMore ? items.subList(0, fetchLimit) : items;

                                NotificationListResponse response = new NotificationListResponse();
                                response.setItems(page.stream().map(this::toDto).toList());
                                response.setHasMore(hasMore);
                                return ResponseEntity.ok(response);
                            })
                );
    }

    @Override
    public Mono<ResponseEntity<NotificationCountResponse>> getUnreadNotificationCount(
            String xTenantID,
            ServerWebExchange exchange) {

        String tenantId = xTenantID != null ? xTenantID : "";

        return resolveUserId(exchange)
                .flatMap(userId ->
                    queryUseCase.countUnread(userId, tenantId)
                            .map(count -> {
                                NotificationCountResponse response = new NotificationCountResponse();
                                response.setUnread(count);
                                return ResponseEntity.ok(response);
                            })
                );
    }

    @Override
    public Mono<ResponseEntity<Void>> markNotificationAsRead(
            String id,
            ServerWebExchange exchange) {

        return resolveUserId(exchange)
                .flatMap(userId ->
                    queryUseCase.markAsRead(id, userId)
                            .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                );
    }

    @Override
    public Mono<ResponseEntity<Void>> markAllNotificationsRead(
            String xTenantID,
            ServerWebExchange exchange) {

        String tenantId = xTenantID != null ? xTenantID : "";

        return resolveUserId(exchange)
                .flatMap(userId ->
                    queryUseCase.markAllRead(userId, tenantId)
                            .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                );
    }

    @Override
    public Mono<ResponseEntity<Void>> dismissNotification(
            String id,
            ServerWebExchange exchange) {

        return resolveUserId(exchange)
                .flatMap(userId ->
                    queryUseCase.dismiss(id, userId)
                            .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                );
    }

    @Override
    public Mono<ResponseEntity<Void>> clearAllNotifications(
            String xTenantID,
            ServerWebExchange exchange) {

        String tenantId = xTenantID != null ? xTenantID : "";

        return resolveUserId(exchange)
                .flatMap(userId ->
                    queryUseCase.clearAll(userId, tenantId)
                            .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                );
    }

    // ── Helpers ──

    private Mono<String> resolveUserId(ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .map(p -> p.getName())
                .defaultIfEmpty("anonymous");
    }

    private NotificationResponse toDto(Notification n) {
        NotificationResponse dto = new NotificationResponse();
        dto.setId(n.getId());
        dto.setUserId(n.getUserId());
        dto.setTenantId(n.getTenantId());
        dto.setType(n.getType());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setIcon(n.getIcon());
        dto.setRead(n.isRead());
        dto.setCreatedAt(n.getCreatedAt() != null ? n.getCreatedAt().atOffset(ZoneOffset.UTC) : null);
        dto.setPayload(n.getPayload());
        return dto;
    }
}
