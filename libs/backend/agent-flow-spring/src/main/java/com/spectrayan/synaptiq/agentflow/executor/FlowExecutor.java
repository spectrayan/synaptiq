package com.spectrayan.synaptiq.agentflow.executor;

import com.spectrayan.synaptiq.agentflow.builder.FlowBuilder;
import com.spectrayan.synaptiq.agentflow.builder.models.settings.FlowSettings;
import com.spectrayan.synaptiq.agentflow.spi.CompiledFlow;
import com.spectrayan.synaptiq.agentflow.spi.FlowExecutionContext;
import com.spectrayan.synaptiq.agentflow.spi.FlowExecutionEvent;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Core flow execution engine.
 * <p>
 * Manages the lifecycle of flow runs: compiling settings into executable
 * flows via {@link FlowBuilder}, starting runs, streaming events,
 * handling cancellation, and retrieving results.
 */
@Slf4j
public class FlowExecutor {

    private final FlowBuilder flowBuilder;
    private final Map<String, RunState> runs = new ConcurrentHashMap<>();

    public FlowExecutor(FlowBuilder flowBuilder) {
        this.flowBuilder = flowBuilder;
    }

    /**
     * Compile and start a new run.
     *
     * @param spec  the flow definition
     * @param input the user query or initial state
     * @return the unique run ID
     */
    public String startRun(FlowSettings spec, Object input) {
        log.info("Starting run for flow: {} ({})", spec.getName(), spec.getId());

        CompiledFlow compiledFlow = flowBuilder.build(spec);

        RunState run = new RunState(spec, compiledFlow);
        run.setInput(input);

        String runId = UUID.randomUUID().toString();
        runs.put(runId, run);

        log.debug("Run {} created for flow '{}'", runId, spec.getName());
        return runId;
    }

    /**
     * Stream execution events for the given run.
     *
     * @param runId the run identifier
     * @return a reactive stream of typed execution events
     */
    public Flux<FlowExecutionEvent> streamRun(String runId) {
        RunState run = getRun(runId);

        FlowExecutionContext ctx = new FlowExecutionContext(
            runId, new AtomicBoolean(false)
        );

        return run.getCompiledFlow().execute(run.getInput(), ctx)
            .doOnNext(event -> {
                if (event.type() == FlowExecutionEvent.EventType.FLOW_COMPLETED) {
                    run.setResult(event.metadata());
                    run.getDone().set(true);
                }
                if (event.type() == FlowExecutionEvent.EventType.ERROR) {
                    run.setError(new RuntimeException(event.content()));
                    run.getDone().set(true);
                }
            })
            .doOnError(err -> {
                run.setError(err);
                run.getDone().set(true);
            })
            .doOnComplete(() -> run.getDone().set(true));
    }

    /**
     * Cancel a running flow execution.
     *
     * @param runId the run identifier
     */
    public void cancelRun(String runId) {
        RunState run = getRun(runId);
        run.cancel();
        log.info("Run {} cancelled", runId);
    }

    /**
     * Retrieve the result of a completed run.
     *
     * @param runId the run identifier
     * @return the result, or null if not yet complete
     * @throws Throwable if the run failed
     */
    public Object getResult(String runId) throws Throwable {
        RunState run = getRun(runId);
        if (run.getResult() != null) {
            return run.getResult();
        }
        if (run.getError() != null) {
            throw run.getError();
        }
        return null;
    }

    /**
     * Check whether a run is complete.
     */
    public boolean isComplete(String runId) {
        return getRun(runId).getDone().get();
    }

    private RunState getRun(String runId) {
        RunState run = runs.get(runId);
        if (run == null) {
            throw new IllegalArgumentException("Unknown run_id: " + runId);
        }
        return run;
    }
}
