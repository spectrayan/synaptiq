package com.spectrayan.synaptiq.workflow.application.service;

import com.spectrayan.synaptiq.agentflow.executor.FlowExecutor;
import com.spectrayan.synaptiq.workflow.application.port.in.WorkflowQueryUseCase;
import com.spectrayan.synaptiq.workflow.application.port.in.WorkflowExecutionUseCase;
import com.spectrayan.synaptiq.workflow.domain.model.FlowEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkflowExecutionService implements WorkflowExecutionUseCase {
    private final WorkflowQueryUseCase workflowQuery;
    private final FlowExecutor flowExecutor;

    @Override
    public Flux<FlowEvent> executeWorkflow(String tenantId, String workflowId, Object input) {
        return workflowQuery.get(tenantId, workflowId).flatMapMany(w -> {
            String runId = flowExecutor.startRun(w.getSpec(), input);
            return Flux.concat(
                Flux.just(new FlowEvent.Started(runId, Instant.now())),
                flowExecutor.streamRun(runId)
                    .map(event -> mapToFlowEvent(runId, event))
            );
        });
    }

    @SuppressWarnings("unchecked")
    private FlowEvent mapToFlowEvent(String runId, Map<String, Object> raw) {
        String type = (String) raw.getOrDefault("type", "step_completed");
        return switch (type) {
            case "completed" -> new FlowEvent.Completed(
                runId, raw, 0, Instant.now()
            );
            case "error" -> new FlowEvent.Failed(
                runId,
                (String) raw.getOrDefault("error", "Unknown error"),
                (String) raw.get("stepId"),
                Instant.now()
            );
            case "token_delta" -> new FlowEvent.TokenDelta(
                runId,
                (String) raw.get("stepId"),
                (String) raw.getOrDefault("delta", "")
            );
            default -> new FlowEvent.StepCompleted(
                runId,
                (String) raw.get("stepId"),
                (String) raw.get("stepName"),
                (String) raw.getOrDefault("status", "completed"),
                raw,
                Instant.now()
            );
        };
    }
}
