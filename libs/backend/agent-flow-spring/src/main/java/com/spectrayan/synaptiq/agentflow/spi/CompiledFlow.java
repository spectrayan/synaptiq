package com.spectrayan.synaptiq.agentflow.spi;

import reactor.core.publisher.Flux;

/**
 * A compiled, executable agent flow.
 * <p>
 * Provider implementations (e.g. Google ADK, LangGraph4J) wrap their
 * native graph/runner behind this interface so the executor remains
 * runtime-agnostic.
 */
public interface CompiledFlow {

    /**
     * Unique name of the compiled flow (typically from {@code FlowSettings.name}).
     */
    String name();

    /**
     * Execute the flow with the given input and return a reactive stream
     * of normalized execution events.
     *
     * @param input   the user query or initial state
     * @param context execution context carrying run ID and cancellation signal
     * @return a {@link Flux} of {@link FlowExecutionEvent} instances
     */
    Flux<FlowExecutionEvent> execute(Object input, FlowExecutionContext context);
}
