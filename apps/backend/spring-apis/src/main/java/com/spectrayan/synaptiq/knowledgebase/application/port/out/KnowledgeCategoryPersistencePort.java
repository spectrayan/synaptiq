package com.spectrayan.synaptiq.knowledgebase.application.port.out;

import com.spectrayan.synaptiq.knowledgebase.domain.model.KnowledgeCategory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface KnowledgeCategoryPersistencePort {
    Mono<KnowledgeCategory> save(KnowledgeCategory category);
    Flux<KnowledgeCategory> findByTenantId(String tenantId);
    Mono<Long> countByTenantId(String tenantId);
}
