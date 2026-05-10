package com.synaptiq.workflow.infrastructure.web;

import com.synaptiq.agentflow.builder.models.settings.FlowSettings;
import com.synaptiq.infrastructure.in.web.api.WorkflowsApi;
import com.synaptiq.infrastructure.in.web.dto.*;
import com.synaptiq.workflow.application.port.in.*;
import com.synaptiq.workflow.domain.model.FlowEvent;
import com.synaptiq.workflow.domain.model.WorkflowRun;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class WorkflowController implements WorkflowsApi {
    private final WorkflowCrudUseCase workflowCrud;
    private final WorkflowSharingUseCase workflowSharing;
    private final WorkflowExecutionUseCase workflowExecution;
    private final WorkflowGenerationUseCase workflowGeneration;
    private final PromptRegenerationUseCase promptRegeneration;
    private final WorkflowToolRegistryUseCase toolRegistry;
    private final WorkflowRunHistoryUseCase runHistory;
    private final WorkflowDtoMapper mapper;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    // ═══════════════════════════════════════════════════════════════════════
    // CRUD — already implemented
    // ═══════════════════════════════════════════════════════════════════════

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

    // ═══════════════════════════════════════════════════════════════════════
    // Sharing — already implemented
    // ═══════════════════════════════════════════════════════════════════════

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

    // ═══════════════════════════════════════════════════════════════════════
    // Tool Registry — NOW IMPLEMENTED
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    public Mono<ResponseEntity<ToolCatalogResponse>> listWorkflowTools(ServerWebExchange exchange) {
        return toolRegistry.listAvailableTools()
            .map(t -> new ToolDefinitionResponse()
                .id(t.getId()).name(t.getName())
                .description(t.getDescription()).category(t.getCategory())
                .icon(t.getIcon()))
            .collectList()
            .map(tools -> ResponseEntity.ok(new ToolCatalogResponse().tools(tools)));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Run History — NOW IMPLEMENTED
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    public Mono<ResponseEntity<Flux<WorkflowRunSummary>>> listWorkflowRuns(String workflowId, String xTenantID, Integer limit, ServerWebExchange exchange) {
        Flux<WorkflowRunSummary> runs = runHistory.listRuns(xTenantID, workflowId, limit != null ? limit : 20)
            .map(this::toRunSummaryDto);
        return Mono.just(ResponseEntity.ok(runs));
    }

    @Override
    public Mono<ResponseEntity<WorkflowRunDetail>> getWorkflowRunDetail(String runId, String xTenantID, ServerWebExchange exchange) {
        return runHistory.getRunDetail(xTenantID, runId)
            .map(this::toRunDetailDto)
            .map(ResponseEntity::ok);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Execution — NOW IMPLEMENTED (SSE streaming)
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    public Mono<ResponseEntity<String>> executeWorkflow(Mono<ExecuteWorkflowRequest> request, String xTenantID, ServerWebExchange exchange) {
        return request.flatMap(req -> {
            // The spec is passed inline in the request body
            FlowSettings flowSpec = toFlowSettings(req.getSpec());
            Object input = req.getInputText() != null ? req.getInputText() : "";

            // Start a run and stream events as SSE
            String runId = objectMapper.convertValue(flowSpec, FlowSettings.class).getId();
            Flux<FlowEvent> events = workflowExecution.executeWorkflow(xTenantID, runId != null ? runId : "inline", input);

            Flux<String> sseStream = events.map(this::flowEventToSse);

            exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_EVENT_STREAM);
            return exchange.getResponse()
                .writeWith(sseStream.map(s -> exchange.getResponse().bufferFactory().wrap(s.getBytes())))
                .then(Mono.just(ResponseEntity.ok().build()));
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Generation — NOW IMPLEMENTED (SSE streaming)
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    public Mono<ResponseEntity<String>> generateWorkflow(Mono<GenerateWorkflowRequest> request, String xTenantID, ServerWebExchange exchange) {
        return request.flatMap(req -> {
            Flux<FlowEvent> events = workflowGeneration.generateWorkflow(xTenantID, req.getPrompt());

            // Use generation-specific SSE mapping: status, component, done, error
            Flux<String> sseStream = events.map(this::flowEventToGenerationSse);

            exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_EVENT_STREAM);
            return exchange.getResponse()
                .writeWith(sseStream.map(s -> exchange.getResponse().bufferFactory().wrap(s.getBytes())))
                .then(Mono.just(ResponseEntity.ok().build()));
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Prompt Regeneration — NOW IMPLEMENTED
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    public Mono<ResponseEntity<RegeneratePromptResponse>> regeneratePrompt(Mono<RegeneratePromptRequest> request, String xTenantID, ServerWebExchange exchange) {
        return request.flatMap(req -> promptRegeneration.regeneratePrompt(
                xTenantID,
                req.getNodeId(),
                req.getNodeLabel(),
                req.getNodeDescription(),
                req.getCurrentPrompt(),
                req.getInstruction(),
                null
            )
            .map(prompt -> ResponseEntity.ok(new RegeneratePromptResponse().prompt(prompt))));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Private helpers
    // ═══════════════════════════════════════════════════════════════════════

    private FlowSettings toFlowSettings(Object spec) {
        return objectMapper.convertValue(spec, FlowSettings.class);
    }

    private String flowEventToSse(FlowEvent event) {
        try {
            return switch (event) {
                case FlowEvent.Started e -> "event: execution_start\ndata: " + objectMapper.writeValueAsString(Map.of("run_id", e.runId(), "started_at", e.timestamp().toEpochMilli())) + "\n\n";
                case FlowEvent.StepCompleted e -> "event: node_complete\ndata: " + objectMapper.writeValueAsString(Map.of("node_id", e.stepId(), "label", e.stepName(), "status", e.status(), "output", e.output())) + "\n\n";
                case FlowEvent.TokenDelta e -> "event: text\ndata: " + objectMapper.writeValueAsString(Map.of("step_id", e.stepId(), "delta", e.delta())) + "\n\n";
                case FlowEvent.Completed e -> "event: execution_complete\ndata: " + objectMapper.writeValueAsString(Map.of("run_id", e.runId(), "result", e.finalOutput(), "total_duration_ms", e.durationMs())) + "\n\n";
                case FlowEvent.Failed e -> "event: execution_error\ndata: " + objectMapper.writeValueAsString(Map.of("run_id", e.runId(), "error", e.error(), "failed_node", e.failedStepId() != null ? e.failedStepId() : "")) + "\n\n";
            };
        } catch (Exception ex) {
            return "event: error\ndata: " + ex.getMessage() + "\n\n";
        }
    }

    /**
     * Generation-specific SSE mapping.
     * Frontend expects: status, component, text, done, error.
     */
    private String flowEventToGenerationSse(FlowEvent event) {
        try {
            return switch (event) {
                case FlowEvent.Started e -> "event: status\ndata: Initializing workflow generation...\n\n";
                case FlowEvent.StepCompleted e -> "event: status\ndata: " + e.stepName() + "\n\n";
                case FlowEvent.TokenDelta e -> "event: text\ndata: " + e.delta() + "\n\n";
                case FlowEvent.Completed e -> {
                    // Emit the generated spec as a 'component' event, then 'done'
                    String specJson = objectMapper.writeValueAsString(Map.of("spec", e.finalOutput()));
                    yield "event: component\ndata: " + specJson + "\n\nevent: done\ndata: {}\n\n";
                }
                case FlowEvent.Failed e -> "event: error\ndata: " + e.error() + "\n\n";
            };
        } catch (Exception ex) {
            return "event: error\ndata: " + ex.getMessage() + "\n\n";
        }
    }

    private WorkflowRunSummary toRunSummaryDto(WorkflowRun run) {
        var dto = new WorkflowRunSummary();
        dto.setRunId(run.getId());
        dto.setWorkflowId(run.getWorkflowId());
        dto.setStatus(run.getStatus() != null ? WorkflowRunSummary.StatusEnum.fromValue(run.getStatus()) : null);
        if (run.getStartedAt() != null) dto.setStartedAt(run.getStartedAt().atOffset(java.time.ZoneOffset.UTC));
        if (run.getCompletedAt() != null) dto.setCompletedAt(run.getCompletedAt().atOffset(java.time.ZoneOffset.UTC));
        dto.setTotalDurationMs(run.getTotalDurationMs());
        return dto;
    }

    private WorkflowRunDetail toRunDetailDto(WorkflowRun run) {
        var dto = new WorkflowRunDetail();
        dto.setRunId(run.getId());
        dto.setWorkflowId(run.getWorkflowId());
        dto.setStatus(run.getStatus() != null ? WorkflowRunDetail.StatusEnum.fromValue(run.getStatus()) : null);
        if (run.getStartedAt() != null) dto.setStartedAt(run.getStartedAt().atOffset(java.time.ZoneOffset.UTC));
        if (run.getCompletedAt() != null) dto.setCompletedAt(run.getCompletedAt().atOffset(java.time.ZoneOffset.UTC));
        dto.setTotalDurationMs(run.getTotalDurationMs());
        dto.setResult(run.getResult());

        if (run.getNodes() != null) {
            Map<String, WorkflowRunNodeDetail> nodeDtos = run.getNodes().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> {
                    var n = e.getValue();
                    var nd = new WorkflowRunNodeDetail();
                    nd.setStatus(n.getStatus() != null ? NodeExecutionStatus.fromValue(n.getStatus()) : null);
                    nd.setDurationMs(n.getDurationMs());
                    nd.setOutput(n.getOutput());
                    nd.setError(n.getError());
                    if (n.getStartedAt() != null) nd.setStartedAt(n.getStartedAt().atOffset(java.time.ZoneOffset.UTC));
                    if (n.getCompletedAt() != null) nd.setCompletedAt(n.getCompletedAt().atOffset(java.time.ZoneOffset.UTC));
                    return nd;
                }));
            dto.setNodes(nodeDtos);
        }
        return dto;
    }
}
