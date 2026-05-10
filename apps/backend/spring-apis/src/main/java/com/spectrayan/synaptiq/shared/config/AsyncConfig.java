package com.spectrayan.synaptiq.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables async processing for event listeners and background tasks.
 * <p>
 * Domain event listeners annotated with {@code @Async} will execute on
 * a separate thread pool, preventing notification fan-out or analytics
 * aggregation from blocking the request thread.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
