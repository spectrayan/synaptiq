package com.spectrayan.synaptiq.schemaregistry.application.port.in;

import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;

public interface SchemaRegistryUseCase {
    Mono<List<String>> listCollections(String tenantId);
    Mono<Map<String, Object>> inferSchema(String tenantId, String collection);
    Mono<List<Map<String, Object>>> query(String tenantId, String collection, Map<String, Object> filter, int limit);
}
