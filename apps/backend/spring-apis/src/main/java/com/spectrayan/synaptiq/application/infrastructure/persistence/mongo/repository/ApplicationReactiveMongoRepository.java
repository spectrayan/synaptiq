package com.spectrayan.synaptiq.application.infrastructure.persistence.mongo.repository;

import com.spectrayan.synaptiq.application.infrastructure.persistence.mongo.entity.ApplicationDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ApplicationReactiveMongoRepository extends ReactiveMongoRepository<ApplicationDocument, String> {
    Mono<ApplicationDocument> findByAppId(String appId);
    Flux<ApplicationDocument> findByTenantId(String tenantId);
    Mono<ApplicationDocument> findByTenantIdAndIsDefaultTrue(String tenantId);
    Mono<Long> countByTenantId(String tenantId);
    Mono<Void> deleteByAppId(String appId);
}
