package com.spectrayan.synaptiq.action.application.port.out;

import com.spectrayan.synaptiq.action.domain.model.SavedItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Outbound port for saved item persistence.
 * Implemented by SavedItemMongoAdapter in the infrastructure layer.
 * Returns domain models — never MongoDB documents.
 */
public interface SavedItemPersistencePort {

    Mono<SavedItem> save(SavedItem savedItem);

    Flux<SavedItem> findByTenantIdAndSessionId(String tenantId, String sessionId);

    Mono<Boolean> deleteByTenantIdAndItemIdAndSessionId(String tenantId, String itemId, String sessionId);
}
