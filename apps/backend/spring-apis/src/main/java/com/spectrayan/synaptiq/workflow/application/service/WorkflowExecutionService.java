package com.spectrayan.synaptiq.workflow.application.service;

import com.spectrayan.synaptiq.agentflow.executor.FlowExecutor;
import com.spectrayan.synaptiq.agentflow.spi.FlowExecutionEvent;
import com.spectrayan.synaptiq.workflow.application.port.in.WorkflowQueryUseCase;
import com.spectrayan.synaptiq.workflow.application.port.in.WorkflowExecutionUseCase;
import com.spectrayan.synaptiq.workflow.domain.model.FlowEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Instant;

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
                    .map(event -> mapExecutionEvent(runId, event))
            );
        });
    }

    /**
     * Map normalized {@link FlowExecutionEvent} to domain {@link FlowEvent}.
     */
    private FlowEvent mapExecutionEvent(String runId, FlowExecutionEvent event) {
        return switch (event.type()) {
            case STEP_COMPLETED -> new FlowEvent.StepCompleted(
                runId,
                event.stepId(),
                event.stepName(),
                "completed",
                event.metadata(),
                event.timestamp()
            );
            case TOKEN_DELTA -> new FlowEvent.TokenDelta(
                runId,
                event.stepId(),
                event.content()
            );
            case FLOW_COMPLETED -> new FlowEvent.Completed(
                runId,
                event.metadata(),
                0L,
                event.timestamp()
            );
            case ERROR -> new FlowEvent.Failed(
                runId,
                event.content(),
                event.stepId(),
                event.timestamp()
            );
            case STEP_STARTED, TOOL_CALL, TOOL_RESULT -> new FlowEvent.StepCompleted(
                runId,
                event.stepId(),
                event.stepName(),
                event.type().name().toLowerCase(),
                event.metadata(),
                event.timestamp()
            );
        };
    }
}
