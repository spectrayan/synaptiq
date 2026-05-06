package com.synaptiq.catalog.application.port.in;

import com.synaptiq.catalog.domain.model.CatalogItem;
import com.synaptiq.catalog.domain.model.CatalogSchema;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CatalogQueryUseCase {
    Mono<CatalogSchema> getActiveSchema(String tenantId);
    Flux<CatalogItem> listItems(String tenantId, String statusFilter, int skip, int limit);
    Mono<CatalogItem> getItem(String tenantId, String itemId);
    Mono<Long> countItems(String tenantId);
}
