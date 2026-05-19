package com.spectrayan.synaptiq.knowledgebase.application.service;

import com.spectrayan.synaptiq.knowledgebase.application.port.out.KnowledgeCategoryPersistencePort;
import com.spectrayan.synaptiq.knowledgebase.domain.model.KnowledgeCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeCategoryService {
    private final KnowledgeCategoryPersistencePort persistencePort;

    public Mono<KnowledgeCategory> createCategory(String tenantId, String name, String description) {
        KnowledgeCategory category = KnowledgeCategory.builder()
                .tenantId(tenantId)
                .name(name)
                .description(description)
                .build();
        return persistencePort.save(category);
    }

    public Flux<KnowledgeCategory> listCategories(String tenantId) {
        return persistencePort.findByTenantId(tenantId);
    }
}
