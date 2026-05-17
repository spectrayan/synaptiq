package com.spectrayan.synaptiq.agentflow.builder.models.settings;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Configuration for parallel execution of agents, tools, and sub-flows.
 * <p>
 * When {@code virtualThreads} is enabled, parallel workloads run on
 * Java 21+ virtual threads using {@code StructuredTaskScope}, providing
 * lightweight concurrency without dedicated thread pools.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParallelConfig {

    /** Enable virtual threads for parallel execution (requires Java 21+). */
    @Builder.Default
    private boolean virtualThreads = true;

    /** Maximum number of concurrent tasks. */
    @Builder.Default
    private int maxConcurrency = 10;
}
