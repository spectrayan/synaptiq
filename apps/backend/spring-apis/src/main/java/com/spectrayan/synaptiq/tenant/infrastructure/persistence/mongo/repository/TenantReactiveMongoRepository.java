package com.spectrayan.synaptiq.tenant.infrastructure.persistence.mongo.repository;

import com.spectrayan.synaptiq.tenant.infrastructure.persistence.mongo.entity.TenantRegistryDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface TenantReactiveMongoRepository extends ReactiveMongoRepository<TenantRegistryDocument, String> {
    Mono<TenantRegistryDocument> findByTenantId(String tenantId);
    Mono<TenantRegistryDocument> findBySlug(String slug);
}
