package com.spectrayan.synaptiq.agentflow.builder.models.settings;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionPolicy {
    
    @Builder.Default
    private boolean deterministic = false;
    
    private Integer seed;
    
    @Builder.Default
    private ResourceLimits resources = new ResourceLimits();

    /** Parallel execution configuration (virtual threads, max concurrency). */
    @Builder.Default
    private ParallelConfig parallel = new ParallelConfig();
}

