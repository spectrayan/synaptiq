package com.spectrayan.synaptiq.knowledgebase.infrastructure.persistence.mongo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface KnowledgeDocumentRepository extends ReactiveMongoRepository<KnowledgeDocumentEntity, String> {
    Flux<KnowledgeDocumentEntity> findByTenantId(String tenantId);
    Flux<KnowledgeDocumentEntity> findByTenantIdAndCategoryId(String tenantId, String categoryId);
}
