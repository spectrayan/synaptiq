package com.spectrayan.synaptiq.integration.spi;

import com.spectrayan.synaptiq.integration.model.ExecutionResult;
import reactor.core.publisher.Mono;

/**
 * SPI for logging integration execution results.
 * <p>
 * The consuming application implements this to persist audit logs
 * to MongoDB, send to analytics, or emit events.
 */
public interface ExecutionLogger {

    /**
     * Log an execution result (success or failure).
     */
    Mono<Void> log(ExecutionResult result);
}
