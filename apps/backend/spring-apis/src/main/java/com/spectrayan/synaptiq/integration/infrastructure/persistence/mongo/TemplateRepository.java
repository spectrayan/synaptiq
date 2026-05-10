package com.spectrayan.synaptiq.integration.infrastructure.persistence.mongo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive MongoDB repository for integration template documents.
 */
public interface TemplateRepository extends ReactiveMongoRepository<TemplateDocument, String> {

    Mono<TemplateDocument> findByTemplateId(String templateId);

    Mono<Void> deleteByTemplateId(String templateId);

    /** Find all global templates (tenantId is null). */
    Flux<TemplateDocument> findByTenantIdIsNull();

    /** Find templates scoped to a specific tenant. */
    Flux<TemplateDocument> findByTenantId(String tenantId);
}
