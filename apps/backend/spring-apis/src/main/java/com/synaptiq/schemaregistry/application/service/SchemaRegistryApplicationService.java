package com.synaptiq.schemaregistry.application.service;

import com.synaptiq.schemaregistry.application.port.in.SchemaRegistryUseCase;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.*;

@Service @RequiredArgsConstructor
public class SchemaRegistryApplicationService implements SchemaRegistryUseCase {
    private final ReactiveMongoTemplate mongoTemplate;

    @Override public Mono<List<String>> listCollections(String tenantId) {
        return mongoTemplate.getCollectionNames().collectList();
    }

    @Override public Mono<Map<String, Object>> inferSchema(String tenantId, String collection) {
        return mongoTemplate.findOne(new BasicQuery("{}"), Document.class, collection)
            .map(doc -> {
                Map<String, Object> schema = new LinkedHashMap<>();
                doc.forEach((key, value) -> schema.put(key, value != null ? value.getClass().getSimpleName() : "null"));
                return schema;
            })
            .defaultIfEmpty(Map.of());
    }

    @Override public Mono<List<Map<String, Object>>> query(String tenantId, String collection, Map<String, Object> filter, int limit) {
        var queryDoc = new Document(filter != null ? filter : Map.of());
        return mongoTemplate.find(new BasicQuery(queryDoc).limit(Math.min(limit, 100)), Document.class, collection)
            .map(doc -> (Map<String, Object>) new LinkedHashMap<>(doc)).collectList();
    }
}
