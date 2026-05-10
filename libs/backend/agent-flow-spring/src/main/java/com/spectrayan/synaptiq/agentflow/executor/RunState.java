package com.spectrayan.synaptiq.agentflow.executor;

import com.spectrayan.synaptiq.agentflow.builder.models.settings.FlowSettings;
import lombok.Data;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
public class RunState {
    private final FlowSettings spec;
    private final Object graph; // Replace with ADK Agent type once we define graph builder
    private final Instant createdAt = Instant.now();
    
    private final CountDownLatch cancelEvent = new CountDownLatch(1);
    private final AtomicBoolean done = new AtomicBoolean(false);
    
    private Object result;
    private Throwable error;
    
    // Original input state/messages
    private Object input;
    
    public RunState(FlowSettings spec, Object graph) {
        this.spec = spec;
        this.graph = graph;
    }
    
    public void cancel() {
        cancelEvent.countDown();
    }
    
    public boolean isCancelled() {
        return cancelEvent.getCount() == 0;
    }
}
