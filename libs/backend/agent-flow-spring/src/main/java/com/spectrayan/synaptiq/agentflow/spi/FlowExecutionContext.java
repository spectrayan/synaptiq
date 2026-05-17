package com.spectrayan.synaptiq.agentflow.spi;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Mutable execution context passed into {@link CompiledFlow#execute}.
 * <p>
 * Carries the run identifier and a cancellation signal that the executor
 * can set to request early termination of the flow.
 *
 * @param runId     unique identifier for this execution run
 * @param cancelled atomic flag — set to {@code true} to request cancellation
 */
public record FlowExecutionContext(
    String runId,
    AtomicBoolean cancelled
) {

    /**
     * Check whether cancellation has been requested.
     */
    public boolean isCancelled() {
        return cancelled.get();
    }

    /**
     * Request cancellation of the running flow.
     */
    public void cancel() {
        cancelled.set(true);
    }
}
