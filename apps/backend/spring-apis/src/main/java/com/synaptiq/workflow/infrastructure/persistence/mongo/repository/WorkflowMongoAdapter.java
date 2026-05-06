package com.synaptiq.workflow.infrastructure.persistence.mongo.repository;

import com.synaptiq.workflow.application.port.out.WorkflowPersistencePort;
import com.synaptiq.workflow.domain.model.Workflow;
import com.synaptiq.workflow.infrastructure.persistence.mongo.entity.WorkflowDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component @RequiredArgsConstructor
public class WorkflowMongoAdapter implements WorkflowPersistencePort {
    private final ReactiveMongoTemplate mongoTemplate;

    @Override public Mono<Workflow> save(Workflow w) { return mongoTemplate.save(toDoc(w)).map(this::toDomain); }
    @Override public Mono<Workflow> findById(String id, String tenantId) {
        return mongoTemplate.findOne(Query.query(Criteria.where("_id").is(id).and("tenantId").is(tenantId)), WorkflowDocument.class).map(this::toDomain);
    }
    @Override public Flux<Workflow> findByTenantId(String tenantId, int limit) {
        return mongoTemplate.find(Query.query(Criteria.where("tenantId").is(tenantId)).limit(limit), WorkflowDocument.class).map(this::toDomain);
    }
    @Override public Mono<Void> deleteById(String id) {
        return mongoTemplate.remove(Query.query(Criteria.where("_id").is(id)), WorkflowDocument.class).then();
    }
    @Override public Mono<Workflow> findByShareToken(String shareToken) {
        return mongoTemplate.findOne(Query.query(Criteria.where("shareToken").is(shareToken).and("isPublic").is(true)), WorkflowDocument.class).map(this::toDomain);
    }

    private WorkflowDocument toDoc(Workflow w) {
        return WorkflowDocument.builder().id(w.getId()).tenantId(w.getTenantId()).spec(w.getSpec())
            .shareToken(w.getShareToken()).isPublic(w.isPublic()).createdAt(w.getCreatedAt()).updatedAt(w.getUpdatedAt()).build();
    }
    private Workflow toDomain(WorkflowDocument d) {
        return Workflow.builder().id(d.getId()).tenantId(d.getTenantId()).spec(d.getSpec())
            .shareToken(d.getShareToken()).isPublic(d.isPublic()).createdAt(d.getCreatedAt()).updatedAt(d.getUpdatedAt()).build();
    }
}
