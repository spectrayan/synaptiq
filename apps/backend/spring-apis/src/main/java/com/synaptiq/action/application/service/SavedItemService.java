package com.synaptiq.action.application.service;

import com.synaptiq.action.application.port.in.SavedItemUseCase;
import com.synaptiq.action.application.port.out.SavedItemPersistencePort;
import com.synaptiq.action.domain.model.SavedItem;
import com.synaptiq.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SavedItemService implements SavedItemUseCase {

    private final SavedItemPersistencePort savedItemPersistence;

    @Override
    public Flux<SavedItem> getSavedItems(String tenantId, String sessionId) {
        return savedItemPersistence.findByTenantIdAndSessionId(tenantId, sessionId);
    }

    @Override
    public Mono<Void> removeSavedItem(String tenantId, String itemId, String sessionId) {
        return savedItemPersistence.deleteByTenantIdAndItemIdAndSessionId(tenantId, itemId, sessionId)
            .flatMap(deleted -> {
                if (!deleted) {
                    return Mono.error(new ResourceNotFoundException("Saved item not found"));
                }
                return Mono.empty();
            });
    }
}
