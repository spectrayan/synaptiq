package com.spectrayan.synaptiq.knowledgebase.infrastructure.persistence.mongo;

import com.spectrayan.synaptiq.knowledgebase.application.port.out.KnowledgeDocumentPersistencePort;
import com.spectrayan.synaptiq.knowledgebase.domain.model.KnowledgeDocument;
import com.spectrayan.synaptiq.knowledgebase.infrastructure.persistence.mongo.mapper.KnowledgeDocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class KnowledgeDocumentPersistenceAdapter implements KnowledgeDocumentPersistencePort {
    private final KnowledgeDocumentRepository repository;
    private final KnowledgeDocumentMapper mapper;

    @Override
    public Mono<KnowledgeDocument> save(KnowledgeDocument document) {
        return Mono.just(document)
                .map(mapper::toEntity)
                .flatMap(repository::save)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<KnowledgeDocument> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Flux<KnowledgeDocument> findByTenantId(String tenantId) {
        return repository.findByTenantId(tenantId).map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return repository.deleteById(id);
    }
}
