package com.spectrayan.synaptiq.action.infrastructure.persistence.mongo.repository;

import com.spectrayan.synaptiq.action.application.port.out.SavedItemPersistencePort;
import com.spectrayan.synaptiq.action.domain.model.SavedItem;
import com.spectrayan.synaptiq.action.infrastructure.persistence.mongo.mapper.ActionPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adapter implementing the domain's {@link SavedItemPersistencePort}.
 * Converts between domain models and MongoDB documents using MapStruct.
 */
@Component
@RequiredArgsConstructor
public class SavedItemMongoAdapter implements SavedItemPersistencePort {

    private final SavedItemReactiveMongoRepository mongoRepository;
    private final ActionPersistenceMapper mapper;

    @Override
    public Mono<SavedItem> save(SavedItem savedItem) {
        return mongoRepository.save(mapper.toDocument(savedItem))
            .map(mapper::toDomain);
    }

    @Override
    public Flux<SavedItem> findByTenantIdAndSessionId(String tenantId, String sessionId) {
        return mongoRepository.findByTenantIdAndSessionId(tenantId, sessionId)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> deleteByTenantIdAndItemIdAndSessionId(String tenantId, String itemId, String sessionId) {
        return mongoRepository.deleteByTenantIdAndItemIdAndSessionId(tenantId, itemId, sessionId)
            .map(count -> count > 0);
    }
}
