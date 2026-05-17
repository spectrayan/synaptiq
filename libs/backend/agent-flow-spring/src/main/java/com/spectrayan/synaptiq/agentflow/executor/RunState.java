package com.spectrayan.synaptiq.agentflow.executor;

import com.spectrayan.synaptiq.agentflow.builder.models.settings.FlowSettings;
import com.spectrayan.synaptiq.agentflow.spi.CompiledFlow;
import lombok.Data;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tracks the state of a single flow execution run.
 */
@Data
public class RunState {

    private final FlowSettings spec;
    private final CompiledFlow compiledFlow;
    private final Instant createdAt = Instant.now();
    
    private final CountDownLatch cancelEvent = new CountDownLatch(1);
    private final AtomicBoolean done = new AtomicBoolean(false);
    
    private Object result;
    private Throwable error;
    
    /** Original input state/messages. */
    private Object input;
    
    public RunState(FlowSettings spec, CompiledFlow compiledFlow) {
        this.spec = spec;
        this.compiledFlow = compiledFlow;
    }
    
    public void cancel() {
        cancelEvent.countDown();
    }
    
    public boolean isCancelled() {
        return cancelEvent.getCount() == 0;
    }
}
