package com.spectrayan.synaptiq.workflow.infrastructure.persistence.mongo.repository;

import com.spectrayan.synaptiq.workflow.application.port.out.WorkflowRunPersistencePort;
import com.spectrayan.synaptiq.workflow.domain.model.WorkflowRun;
import com.spectrayan.synaptiq.workflow.infrastructure.persistence.mongo.entity.WorkflowRunDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WorkflowRunMongoAdapter implements WorkflowRunPersistencePort {
    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public Mono<WorkflowRun> save(WorkflowRun run) {
        return mongoTemplate.save(toDoc(run)).map(this::toDomain);
    }

    @Override
    public Mono<WorkflowRun> findById(String runId, String tenantId) {
        return mongoTemplate.findOne(
            Query.query(Criteria.where("_id").is(runId).and("tenantId").is(tenantId)),
            WorkflowRunDocument.class
        ).map(this::toDomain);
    }

    @Override
    public Flux<WorkflowRun> findByWorkflowId(String workflowId, String tenantId, int limit) {
        Query query = Query.query(
            Criteria.where("workflowId").is(workflowId).and("tenantId").is(tenantId)
        ).with(Sort.by(Sort.Direction.DESC, "startedAt")).limit(limit);
        return mongoTemplate.find(query, WorkflowRunDocument.class).map(this::toDomain);
    }

    // ── Mapping ────────────────────────────────────────────────────────────

    private WorkflowRunDocument toDoc(WorkflowRun run) {
        Map<String, WorkflowRunDocument.NodeRunDetailDoc> nodeDocs = new HashMap<>();
        if (run.getNodes() != null) {
            nodeDocs = run.getNodes().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> toNodeDoc(e.getValue())));
        }
        return WorkflowRunDocument.builder()
            .id(run.getId())
            .workflowId(run.getWorkflowId())
            .tenantId(run.getTenantId())
            .status(run.getStatus())
            .startedAt(run.getStartedAt())
            .completedAt(run.getCompletedAt())
            .totalDurationMs(run.getTotalDurationMs())
            .nodes(nodeDocs)
            .result(run.getResult())
            .error(run.getError())
            .failedNodeId(run.getFailedNodeId())
            .build();
    }

    private WorkflowRunDocument.NodeRunDetailDoc toNodeDoc(WorkflowRun.NodeRunDetail n) {
        return WorkflowRunDocument.NodeRunDetailDoc.builder()
            .nodeId(n.getNodeId()).nodeName(n.getNodeName()).status(n.getStatus())
            .startedAt(n.getStartedAt()).completedAt(n.getCompletedAt())
            .durationMs(n.getDurationMs()).output(n.getOutput()).error(n.getError())
            .build();
    }

    private WorkflowRun toDomain(WorkflowRunDocument doc) {
        Map<String, WorkflowRun.NodeRunDetail> nodes = new HashMap<>();
        if (doc.getNodes() != null) {
            nodes = doc.getNodes().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> toNodeDomain(e.getValue())));
        }
        return WorkflowRun.builder()
            .id(doc.getId())
            .workflowId(doc.getWorkflowId())
            .tenantId(doc.getTenantId())
            .status(doc.getStatus())
            .startedAt(doc.getStartedAt())
            .completedAt(doc.getCompletedAt())
            .totalDurationMs(doc.getTotalDurationMs())
            .nodes(nodes)
            .result(doc.getResult())
            .error(doc.getError())
            .failedNodeId(doc.getFailedNodeId())
            .createdAt(doc.getCreatedAt())
            .updatedAt(doc.getUpdatedAt())
            .build();
    }

    private WorkflowRun.NodeRunDetail toNodeDomain(WorkflowRunDocument.NodeRunDetailDoc d) {
        return WorkflowRun.NodeRunDetail.builder()
            .nodeId(d.getNodeId()).nodeName(d.getNodeName()).status(d.getStatus())
            .startedAt(d.getStartedAt()).completedAt(d.getCompletedAt())
            .durationMs(d.getDurationMs()).output(d.getOutput()).error(d.getError())
            .build();
    }
}
