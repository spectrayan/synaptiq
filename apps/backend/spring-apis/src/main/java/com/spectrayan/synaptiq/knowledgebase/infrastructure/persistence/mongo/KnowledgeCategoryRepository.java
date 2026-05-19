package com.spectrayan.synaptiq.knowledgebase.infrastructure.persistence.mongo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface KnowledgeCategoryRepository extends ReactiveMongoRepository<KnowledgeCategoryEntity, String> {
    Flux<KnowledgeCategoryEntity> findByTenantId(String tenantId);
}
