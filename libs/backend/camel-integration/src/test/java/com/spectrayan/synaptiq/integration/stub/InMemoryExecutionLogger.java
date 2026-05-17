package com.spectrayan.synaptiq.integration.stub;

import com.spectrayan.synaptiq.integration.model.ExecutionResult;
import com.spectrayan.synaptiq.integration.spi.ExecutionLogger;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory {@link ExecutionLogger} for testing.
 * Captures all logged executions for later assertion.
 */
public class InMemoryExecutionLogger implements ExecutionLogger {

    private final List<ExecutionResult> logs = new CopyOnWriteArrayList<>();

    public List<ExecutionResult> getLogs() {
        return List.copyOf(logs);
    }

    public int logCount() {
        return logs.size();
    }

    public void clear() {
        logs.clear();
    }

    @Override
    public Mono<Void> log(ExecutionResult result) {
        logs.add(result);
        return Mono.empty();
    }
}
