package com.synaptiq.agentflow.executor;

import com.synaptiq.agentflow.builder.models.settings.FlowSettings;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class FlowExecutor {
    
    private final Map<String, RunState> runs = new ConcurrentHashMap<>();
    
    /**
     * Start a run.
     * @param spec The parsed FlowSettings.
     * @param input The input messages/state.
     * @return the run_id
     */
    public String startRun(FlowSettings spec, Object input) {
        // TODO: Build ADK Graph here
        Object graph = new Object(); // placeholder for Google ADK Agent
        
        RunState run = new RunState(spec, graph);
        run.setInput(input);
        
        String runId = UUID.randomUUID().toString();
        runs.put(runId, run);
        
        return runId;
    }
    
    /**
     * Streams the run events.
     */
    public Flux<Map<String, Object>> streamRun(String runId) {
        RunState run = getRun(runId);
        
        // This is a simplified reactive stream representing the execution.
        // Google ADK agents usually run synchronously or via Futures.
        // We'll wrap it in a Flux for parity with Python's generator.
        
        return Flux.create(sink -> {
            try {
                if (run.isCancelled()) {
                    throw new RuntimeException("Run cancelled");
                }
                
                // TODO: Execute ADK agent graph here
                // Example of sending a step event
                Map<String, Object> stepEvent = new HashMap<>();
                stepEvent.put("type", "step");
                stepEvent.put("event", "Agent started");
                stepEvent.put("ts", Instant.now().toEpochMilli());
                sink.next(stepEvent);
                
                // Simulate successful result
                Object out = "ADK Execution Result Placeholder";
                run.setResult(out);
                run.getDone().set(true);
                
                Map<String, Object> resultEvent = new HashMap<>();
                resultEvent.put("type", "result");
                resultEvent.put("result", out);
                resultEvent.put("ts", Instant.now().toEpochMilli());
                sink.next(resultEvent);
                
                sink.complete();
            } catch (Exception e) {
                run.setError(e);
                run.getDone().set(true);
                
                Map<String, Object> errorEvent = new HashMap<>();
                errorEvent.put("type", "error");
                errorEvent.put("error", e.getMessage());
                errorEvent.put("ts", Instant.now().toEpochMilli());
                sink.next(errorEvent);
                sink.complete();
            }
        });
    }
    
    public void cancelRun(String runId) {
        RunState run = getRun(runId);
        run.cancel();
    }
    
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
    
    private RunState getRun(String runId) {
        if (!runs.containsKey(runId)) {
            throw new IllegalArgumentException("Unknown run_id: " + runId);
        }
        return runs.get(runId);
    }
}
