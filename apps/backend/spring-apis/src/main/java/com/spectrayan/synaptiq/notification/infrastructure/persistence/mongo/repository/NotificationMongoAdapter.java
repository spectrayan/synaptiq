package com.spectrayan.synaptiq.notification.infrastructure.persistence.mongo.repository;

import com.spectrayan.synaptiq.notification.application.port.out.NotificationPersistencePort;
import com.spectrayan.synaptiq.notification.domain.model.Notification;
import com.spectrayan.synaptiq.notification.infrastructure.persistence.mongo.entity.NotificationDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * MongoDB adapter implementing the notification repository port.
 * Uses both Spring Data repository and ReactiveMongoTemplate for
 * cursor-based queries and bulk updates.
 */
@Component
@RequiredArgsConstructor
public class NotificationMongoAdapter implements NotificationPersistencePort {

    private final NotificationReactiveMongoRepository mongoRepo;
    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public Mono<Notification> save(Notification notification) {
        NotificationDocument doc = toDocument(notification);
        if (doc.getId() == null) {
            doc.setId(UUID.randomUUID().toString());
        }
        return mongoRepo.save(doc).map(this::toDomain);
    }

    @Override
    public Flux<Notification> findByUserAndTenant(String userId, String tenantId,
                                                   boolean unreadOnly, Instant before, int limit) {
        Instant cursor = before != null ? before : Instant.now();

        Criteria criteria = Criteria.where("userId").is(userId)
                .and("tenantId").is(tenantId)
                .and("createdAt").lt(cursor);

        if (unreadOnly) {
            criteria = criteria.and("read").is(false);
        }

        Query query = new Query(criteria)
                .with(Sort.by(Sort.Direction.DESC, "createdAt"))
                .limit(limit);

        return mongoTemplate.find(query, NotificationDocument.class)
                .map(this::toDomain);
    }

    @Override
    public Mono<Long> countByUserIdAndTenantIdAndReadFalse(String userId, String tenantId) {
        return mongoRepo.countByUserIdAndTenantIdAndRead(userId, tenantId, false);
    }

    @Override
    public Mono<Void> markAsRead(String id, String userId) {
        Query query = new Query(Criteria.where("_id").is(id).and("userId").is(userId));
        Update update = new Update().set("read", true);
        return mongoTemplate.updateFirst(query, update, NotificationDocument.class).then();
    }

    @Override
    public Mono<Void> markAllReadByUserAndTenant(String userId, String tenantId) {
        Query query = new Query(
                Criteria.where("userId").is(userId)
                        .and("tenantId").is(tenantId)
                        .and("read").is(false));
        Update update = new Update().set("read", true);
        return mongoTemplate.updateMulti(query, update, NotificationDocument.class).then();
    }

    @Override
    public Mono<Void> deleteByIdAndUserId(String id, String userId) {
        return mongoRepo.deleteByIdAndUserId(id, userId);
    }

    @Override
    public Mono<Void> deleteAllByUserAndTenant(String userId, String tenantId) {
        Query query = new Query(
                Criteria.where("userId").is(userId)
                        .and("tenantId").is(tenantId));
        return mongoTemplate.remove(query, NotificationDocument.class).then();
    }

    // ── Mapping ──

    private NotificationDocument toDocument(Notification n) {
        return NotificationDocument.builder()
                .id(n.getId())
                .userId(n.getUserId())
                .tenantId(n.getTenantId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .icon(n.getIcon())
                .payload(n.getPayload())
                .read(n.isRead())
                .createdAt(n.getCreatedAt() != null ? n.getCreatedAt() : Instant.now())
                .build();
    }

    private Notification toDomain(NotificationDocument doc) {
        return Notification.builder()
                .id(doc.getId())
                .userId(doc.getUserId())
                .tenantId(doc.getTenantId())
                .type(doc.getType())
                .title(doc.getTitle())
                .message(doc.getMessage())
                .icon(doc.getIcon())
                .payload(doc.getPayload())
                .read(doc.isRead())
                .createdAt(doc.getCreatedAt())
                .build();
    }
}
