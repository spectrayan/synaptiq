package com.spectrayan.synaptiq.notification.infrastructure.persistence.mongo.repository;

import com.spectrayan.synaptiq.notification.infrastructure.persistence.mongo.entity.NotificationDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * Reactive MongoDB repository for notification documents.
 */
public interface NotificationReactiveMongoRepository extends ReactiveMongoRepository<NotificationDocument, String> {

    Mono<Long> countByUserIdAndTenantIdAndRead(String userId, String tenantId, boolean read);

    Mono<Void> deleteByIdAndUserId(String id, String userId);
}
