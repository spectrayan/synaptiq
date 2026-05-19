package com.spectrayan.synaptiq.knowledgebase.infrastructure.persistence.mongo;

import com.spectrayan.synaptiq.knowledgebase.application.port.out.KnowledgeCategoryPersistencePort;
import com.spectrayan.synaptiq.knowledgebase.domain.model.KnowledgeCategory;
import com.spectrayan.synaptiq.knowledgebase.infrastructure.persistence.mongo.mapper.KnowledgeCategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class KnowledgeCategoryPersistenceAdapter implements KnowledgeCategoryPersistencePort {
    private final KnowledgeCategoryRepository repository;
    private final KnowledgeCategoryMapper mapper;

    @Override
    public Mono<KnowledgeCategory> save(KnowledgeCategory category) {
        return Mono.just(category)
                .map(mapper::toEntity)
                .flatMap(repository::save)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<KnowledgeCategory> findByTenantId(String tenantId) {
        return repository.findByTenantId(tenantId).map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countByTenantId(String tenantId) {
        return repository.findByTenantId(tenantId).count();
    }
}
