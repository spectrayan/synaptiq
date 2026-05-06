package com.synaptiq.workflow.infrastructure.web;

import com.synaptiq.agentflow.builder.models.settings.FlowSettings;
import com.synaptiq.infrastructure.in.web.api.WorkflowsApi;
import com.synaptiq.infrastructure.in.web.dto.*;
import com.synaptiq.workflow.application.port.in.WorkflowCrudUseCase;
import com.synaptiq.workflow.application.port.in.WorkflowSharingUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class WorkflowController implements WorkflowsApi {
    private final WorkflowCrudUseCase workflowCrud;
    private final WorkflowSharingUseCase workflowSharing;
    private final WorkflowDtoMapper mapper;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Override
    public Mono<ResponseEntity<WorkflowResponse>> saveWorkflow(Mono<SaveWorkflowRequest> req, String xTenantID, ServerWebExchange exchange) {
        return req.flatMap(r -> workflowCrud.save(xTenantID, toFlowSettings(r.getSpec()))
            .map(mapper::toDto)
            .map(d -> ResponseEntity.status(201).body(d)));
    }

    @Override
    public Mono<ResponseEntity<WorkflowListResponse>> listWorkflows(String xTenantID, Integer limit, ServerWebExchange exchange) {
        return workflowCrud.list(xTenantID, limit != null ? limit : 20)
            .map(mapper::toDto)
            .collectList()
            .map(mapper::toListDto)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<WorkflowResponse>> getWorkflow(String workflowId, String xTenantID, ServerWebExchange exchange) {
        return workflowCrud.get(xTenantID, workflowId)
            .map(mapper::toDto)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<WorkflowResponse>> updateWorkflow(String workflowId, Mono<SaveWorkflowRequest> req, String xTenantID, ServerWebExchange exchange) {
        return req.flatMap(r -> workflowCrud.update(xTenantID, workflowId, toFlowSettings(r.getSpec()))
            .map(mapper::toDto)
            .map(ResponseEntity::ok));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteWorkflow(String workflowId, String xTenantID, ServerWebExchange exchange) {
        return workflowCrud.delete(xTenantID, workflowId)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @Override
    public Mono<ResponseEntity<WorkflowResponse>> duplicateWorkflow(String workflowId, String xTenantID, ServerWebExchange exchange) {
        return workflowCrud.duplicate(xTenantID, workflowId)
            .map(mapper::toDto)
            .map(d -> ResponseEntity.status(201).body(d));
    }

    @Override
    public Mono<ResponseEntity<ShareWorkflowResponse>> shareWorkflow(String workflowId, String xTenantID, ServerWebExchange exchange) {
        return workflowSharing.share(xTenantID, workflowId)
            .map(w -> ResponseEntity.ok(new ShareWorkflowResponse()
                .shareToken(w.getShareToken()).success(true)));
    }

    @Override
    public Mono<ResponseEntity<WorkflowResponse>> getSharedWorkflow(String shareToken, ServerWebExchange exchange) {
        return workflowSharing.getShared(shareToken)
            .map(mapper::toDto)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<WorkflowListResponse>> listWorkflowTemplates(ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(new WorkflowListResponse()));
    }

    @Override
    public Mono<ResponseEntity<ToolCatalogResponse>> listWorkflowTools(ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(new ToolCatalogResponse()));
    }

    @Override
    public Mono<ResponseEntity<reactor.core.publisher.Flux<WorkflowRunSummary>>> listWorkflowRuns(String workflowId, String xTenantID, Integer limit, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.status(org.springframework.http.HttpStatus.NOT_IMPLEMENTED).build());
    }

    @Override
    public Mono<ResponseEntity<WorkflowRunDetail>> getWorkflowRunDetail(String runId, String xTenantID, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.status(org.springframework.http.HttpStatus.NOT_IMPLEMENTED).build());
    }

    @Override
    public Mono<ResponseEntity<String>> executeWorkflow(Mono<ExecuteWorkflowRequest> executeWorkflowRequest, String xTenantID, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.status(org.springframework.http.HttpStatus.NOT_IMPLEMENTED).build());
    }

    @Override
    public Mono<ResponseEntity<String>> generateWorkflow(Mono<GenerateWorkflowRequest> generateWorkflowRequest, String xTenantID, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.status(org.springframework.http.HttpStatus.NOT_IMPLEMENTED).build());
    }

    @Override
    public Mono<ResponseEntity<RegeneratePromptResponse>> regeneratePrompt(Mono<RegeneratePromptRequest> regeneratePromptRequest, String xTenantID, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.status(org.springframework.http.HttpStatus.NOT_IMPLEMENTED).build());
    }

    /**
     * Converts the OpenAPI-generated Map spec into a typed FlowSettings.
     * This is the infrastructure boundary conversion — the domain never sees raw Maps.
     */
    private FlowSettings toFlowSettings(Object spec) {
        return objectMapper.convertValue(spec, FlowSettings.class);
    }
}
