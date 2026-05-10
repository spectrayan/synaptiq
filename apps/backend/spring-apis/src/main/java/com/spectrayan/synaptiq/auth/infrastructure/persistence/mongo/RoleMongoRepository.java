package com.spectrayan.synaptiq.auth.infrastructure.persistence.mongo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RoleMongoRepository extends ReactiveMongoRepository<RoleDocument, String> {
    Mono<RoleDocument> findBySlug(String slug);
    Flux<RoleDocument> findByTenantIdOrTenantIdIsNull(String tenantId);
    Mono<Boolean> existsBySlug(String slug);
    Mono<Void> deleteBySlug(String slug);
}
