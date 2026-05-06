package com.synaptiq.catalog.application.port.in;

import com.synaptiq.catalog.domain.model.CatalogItem;
import reactor.core.publisher.Mono;
import java.util.Map;

public interface CatalogCommandUseCase {
    Mono<CatalogItem> createItem(CreateItemCommand command);
    Mono<CatalogItem> updateItem(String tenantId, String itemId, Map<String, Object> data);
    Mono<Void> deleteItem(String tenantId, String itemId);
    Mono<com.synaptiq.infrastructure.in.web.dto.CatalogImportResponse> importItems(String tenantId, org.springframework.http.codec.multipart.FilePart filePart);

    record CreateItemCommand(String tenantId, Map<String, Object> data, String status) {}
}
