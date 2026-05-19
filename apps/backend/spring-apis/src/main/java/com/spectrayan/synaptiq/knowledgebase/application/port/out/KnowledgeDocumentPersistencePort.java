package com.spectrayan.synaptiq.knowledgebase.application.port.out;

import com.spectrayan.synaptiq.knowledgebase.domain.model.KnowledgeDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface KnowledgeDocumentPersistencePort {
    Mono<KnowledgeDocument> save(KnowledgeDocument document);
    Mono<KnowledgeDocument> findById(String id);
    Flux<KnowledgeDocument> findByTenantId(String tenantId);
    Mono<Void> deleteById(String id);
}
