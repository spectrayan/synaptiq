package com.synaptiq.catalog.application.service;

import com.synaptiq.catalog.application.port.in.CatalogQueryUseCase;
import com.synaptiq.catalog.application.port.out.CatalogPersistencePort;
import com.synaptiq.catalog.domain.model.CatalogItem;
import com.synaptiq.catalog.domain.model.CatalogSchema;
import com.synaptiq.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CatalogQueryService implements CatalogQueryUseCase {
    private final CatalogPersistencePort persistence;

    @Override public Mono<CatalogSchema> getActiveSchema(String tenantId) {
        return persistence.findActiveSchema(tenantId);
    }

    @Override public Flux<CatalogItem> listItems(String tenantId, String statusFilter, int skip, int limit) {
        return persistence.findItemsByTenantId(tenantId, statusFilter, skip, Math.min(limit, 100));
    }

    @Override public Mono<CatalogItem> getItem(String tenantId, String itemId) {
        return persistence.findItemById(itemId, tenantId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Item not found")));
    }

    @Override public Mono<Long> countItems(String tenantId) {
        return persistence.countByTenantId(tenantId);
    }
}
