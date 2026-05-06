package com.synaptiq.action.application.port.in;

import com.synaptiq.action.domain.model.SavedItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Inbound port for saved item queries and management.
 */
public interface SavedItemUseCase {

    Flux<SavedItem> getSavedItems(String tenantId, String sessionId);

    Mono<Void> removeSavedItem(String tenantId, String itemId, String sessionId);
}
