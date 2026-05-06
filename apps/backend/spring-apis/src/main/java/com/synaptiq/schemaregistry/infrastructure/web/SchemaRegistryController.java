package com.synaptiq.schemaregistry.infrastructure.web;

import com.synaptiq.infrastructure.in.web.api.SchemaRegistryApi;
import com.synaptiq.infrastructure.in.web.dto.CollectionListResponse;
import com.synaptiq.infrastructure.in.web.dto.QueryResultResponse;
import com.synaptiq.schemaregistry.application.port.in.SchemaRegistryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SchemaRegistryController implements SchemaRegistryApi {
    private final SchemaRegistryUseCase schemaRegistryUseCase;

    @Override
    public Mono<ResponseEntity<CollectionListResponse>> listCollections(String xTenantID, ServerWebExchange exchange) {
        return schemaRegistryUseCase.listCollections(xTenantID)
            .map(c -> ResponseEntity.ok(new CollectionListResponse().collections(c)));
    }

    @Override
    public Mono<ResponseEntity<Map<String, String>>> inferSchema(String xTenantID, String collection, ServerWebExchange exchange) {
        return schemaRegistryUseCase.inferSchema(xTenantID, collection)
            .map(schema -> {
                var result = new java.util.LinkedHashMap<String, String>();
                schema.forEach((k, v) -> result.put(k, v != null ? v.toString() : "null"));
                return ResponseEntity.ok(result);
            });
    }

    @Override
    public Mono<ResponseEntity<QueryResultResponse>> queryCollection(String xTenantID, String collection,
            Integer limit, Mono<Map<String, Object>> filter, ServerWebExchange exchange) {
        return filter.defaultIfEmpty(Map.of()).flatMap(f ->
            schemaRegistryUseCase.query(xTenantID, collection, f, limit != null ? limit : 20)
                .map(docs -> ResponseEntity.ok(new QueryResultResponse()
                    .total(docs.size()))));
    }
}
