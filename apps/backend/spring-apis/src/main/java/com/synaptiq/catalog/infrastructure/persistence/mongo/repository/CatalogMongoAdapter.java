package com.synaptiq.catalog.infrastructure.persistence.mongo.repository;

import com.synaptiq.catalog.application.port.out.CatalogPersistencePort;
import com.synaptiq.catalog.domain.model.CatalogItem;
import com.synaptiq.catalog.domain.model.CatalogSchema;
import com.synaptiq.catalog.infrastructure.persistence.mongo.entity.CatalogItemDocument;
import com.synaptiq.catalog.infrastructure.persistence.mongo.entity.CatalogSchemaDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

/**
 * Catalog persistence adapter — direct typed mapping, no Jackson Map conversion.
 */
@Component
@RequiredArgsConstructor
public class CatalogMongoAdapter implements CatalogPersistencePort {
    private final ReactiveMongoTemplate mongoTemplate;

    @Override public Mono<CatalogSchema> findActiveSchema(String tenantId) {
        return mongoTemplate.findOne(Query.query(Criteria.where("tenantId").is(tenantId).and("active").is(true)), CatalogSchemaDocument.class)
            .map(this::toDomain);
    }

    @Override public Mono<CatalogSchema> saveSchema(CatalogSchema schema) {
        return mongoTemplate.save(toDoc(schema)).map(this::toDomain);
    }

    @Override public Mono<CatalogItem> saveItem(CatalogItem item) {
        return mongoTemplate.save(toItemDoc(item)).map(this::toItemDomain);
    }

    @Override public Mono<CatalogItem> findItemById(String id, String tenantId) {
        return mongoTemplate.findOne(Query.query(Criteria.where("_id").is(id).and("tenantId").is(tenantId)), CatalogItemDocument.class)
            .map(this::toItemDomain);
    }

    @Override public Flux<CatalogItem> findItemsByTenantId(String tenantId, String status, int skip, int limit) {
        var query = Query.query(Criteria.where("tenantId").is(tenantId));
        if (status != null && !status.isBlank()) query.addCriteria(Criteria.where("status").is(status));
        query.with(PageRequest.of(skip / Math.max(limit, 1), limit, Sort.by(Sort.Direction.DESC, "createdAt")));
        return mongoTemplate.find(query, CatalogItemDocument.class).map(this::toItemDomain);
    }

    @Override public Mono<Long> countByTenantId(String tenantId) {
        return mongoTemplate.count(Query.query(Criteria.where("tenantId").is(tenantId)), CatalogItemDocument.class);
    }

    @Override public Mono<Void> deleteItem(String id) {
        return mongoTemplate.remove(Query.query(Criteria.where("_id").is(id)), CatalogItemDocument.class).then();
    }

    // ── Schema mappers ──────────────────────────────────────────────

    private CatalogSchemaDocument toDoc(CatalogSchema s) {
        return CatalogSchemaDocument.builder()
            .id(s.getId()).tenantId(s.getTenantId()).name(s.getName())
            .fields(s.getFields() != null ? s.getFields().stream().map(this::toFieldEmbed).toList() : new ArrayList<>())
            .schemaVersion(s.getSchemaVersion()).active(s.isActive())
            .createdAt(s.getCreatedAt()).updatedAt(s.getUpdatedAt())
            .build();
    }

    private CatalogSchema toDomain(CatalogSchemaDocument d) {
        return CatalogSchema.builder()
            .id(d.getId()).tenantId(d.getTenantId()).name(d.getName())
            .fields(d.getFields() != null ? d.getFields().stream().map(this::toFieldDomain).toList() : new ArrayList<>())
            .schemaVersion(d.getSchemaVersion()).active(d.isActive())
            .createdAt(d.getCreatedAt()).updatedAt(d.getUpdatedAt())
            .build();
    }

    private CatalogSchemaDocument.SchemaFieldEmbed toFieldEmbed(CatalogSchema.SchemaField f) {
        return CatalogSchemaDocument.SchemaFieldEmbed.builder()
            .fieldId(f.getFieldId()).label(f.getLabel()).type(f.getType())
            .required(f.isRequired()).searchable(f.isSearchable())
            .displayable(f.isDisplayable()).filterable(f.isFilterable())
            .displayOrder(f.getDisplayOrder())
            .enumValues(f.getEnumValues() != null ? f.getEnumValues() : new ArrayList<>())
            .build();
    }

    private CatalogSchema.SchemaField toFieldDomain(CatalogSchemaDocument.SchemaFieldEmbed f) {
        return CatalogSchema.SchemaField.builder()
            .fieldId(f.getFieldId()).label(f.getLabel()).type(f.getType())
            .required(f.isRequired()).searchable(f.isSearchable())
            .displayable(f.isDisplayable()).filterable(f.isFilterable())
            .displayOrder(f.getDisplayOrder())
            .enumValues(f.getEnumValues() != null ? f.getEnumValues() : new ArrayList<>())
            .build();
    }

    // ── Item mappers (CatalogItem.data is intentionally Map — user-defined) ──

    private CatalogItemDocument toItemDoc(CatalogItem i) {
        return CatalogItemDocument.builder().id(i.getId()).tenantId(i.getTenantId()).status(i.getStatus())
            .data(i.getData()).embedding(i.getEmbedding()).embeddingModel(i.getEmbeddingModel())
            .createdAt(i.getCreatedAt()).updatedAt(i.getUpdatedAt()).build();
    }

    private CatalogItem toItemDomain(CatalogItemDocument d) {
        return CatalogItem.builder().id(d.getId()).tenantId(d.getTenantId()).status(d.getStatus())
            .data(d.getData()).embedding(d.getEmbedding()).embeddingModel(d.getEmbeddingModel())
            .createdAt(d.getCreatedAt()).updatedAt(d.getUpdatedAt()).build();
    }
}
