package com.synaptiq.action.infrastructure.persistence.mongo.repository;

import com.synaptiq.action.infrastructure.persistence.mongo.entity.SavedItemDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SavedItemReactiveMongoRepository extends ReactiveMongoRepository<SavedItemDocument, String> {

    Flux<SavedItemDocument> findByTenantIdAndSessionId(String tenantId, String sessionId);

    Mono<Long> deleteByTenantIdAndItemIdAndSessionId(String tenantId, String itemId, String sessionId);
}
