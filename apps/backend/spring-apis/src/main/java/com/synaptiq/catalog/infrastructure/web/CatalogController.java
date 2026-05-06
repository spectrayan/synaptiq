package com.synaptiq.catalog.infrastructure.web;

import com.synaptiq.catalog.application.port.in.CatalogCommandUseCase;
import com.synaptiq.catalog.application.port.in.CatalogQueryUseCase;
import com.synaptiq.infrastructure.in.web.api.CatalogApi;
import com.synaptiq.infrastructure.in.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CatalogController implements CatalogApi {
    private final CatalogQueryUseCase catalogQueryUseCase;
    private final CatalogCommandUseCase catalogCommandUseCase;
    private final CatalogDtoMapper mapper;

    @Override
    public Mono<ResponseEntity<CatalogSchemaResponse>> getActiveSchema(String xTenantID, ServerWebExchange exchange) {
        return catalogQueryUseCase.getActiveSchema(xTenantID)
            .map(mapper::toSchemaDto)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<CatalogItemResponse>> createCatalogItem(
            Mono<CreateCatalogItemRequest> req, String xTenantID, ServerWebExchange exchange) {
        return req.flatMap(r -> catalogCommandUseCase.createItem(new CatalogCommandUseCase.CreateItemCommand(
                xTenantID, r.getData(),
                r.getStatus() != null ? r.getStatus().getValue() : "DRAFT"))
            .map(mapper::toItemDto)
            .map(d -> ResponseEntity.status(201).body(d)));
    }

    @Override
    public Mono<ResponseEntity<CatalogItemListResponse>> listCatalogItems(String xTenantID,
            String statusFilter, Integer skip, Integer limit, ServerWebExchange exchange) {
        int sk = skip != null ? skip : 0;
        int lm = limit != null ? limit : 20;
        Mono<Long> countMono = catalogQueryUseCase.countItems(xTenantID);
        return catalogQueryUseCase.listItems(xTenantID, statusFilter, sk, lm)
            .map(mapper::toItemDto)
            .collectList()
            .zipWith(countMono)
            .map(t -> ResponseEntity.ok(mapper.toListDto(t.getT1(), t.getT2())));
    }

    @Override
    public Mono<ResponseEntity<CatalogItemResponse>> getCatalogItem(String itemId, String xTenantID, ServerWebExchange exchange) {
        return catalogQueryUseCase.getItem(xTenantID, itemId)
            .map(mapper::toItemDto)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<CatalogItemResponse>> updateCatalogItem(String itemId,
            Mono<Map<String, Object>> requestBody, String xTenantID, ServerWebExchange exchange) {
        return requestBody.flatMap(updates -> catalogCommandUseCase.updateItem(xTenantID, itemId, updates)
            .map(mapper::toItemDto)
            .map(ResponseEntity::ok));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCatalogItem(String itemId, String xTenantID, ServerWebExchange exchange) {
        return catalogCommandUseCase.deleteItem(xTenantID, itemId)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @Override
    public Mono<ResponseEntity<CatalogImportResponse>> importCatalogItems(String xTenantID,
            Part file, ServerWebExchange exchange) {
        if (file instanceof org.springframework.http.codec.multipart.FilePart fp) {
            return catalogCommandUseCase.importItems(xTenantID, fp)
                .map(ResponseEntity::ok);
        }
        return Mono.just(ResponseEntity.badRequest().build());
    }
}
