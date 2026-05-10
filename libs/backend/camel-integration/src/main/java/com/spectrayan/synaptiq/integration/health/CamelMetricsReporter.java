package com.spectrayan.synaptiq.integration.health;

import com.spectrayan.synaptiq.integration.core.CamelEngineManager;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Exports Camel engine metrics to Micrometer.
 * <p>
 * Metrics:
 * <ul>
 *   <li>{@code synaptiq.integration.routes.active} — number of active routes</li>
 *   <li>{@code synaptiq.integration.engine.status} — 1 if running, 0 if stopped</li>
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
public class CamelMetricsReporter {

    private final CamelEngineManager engineManager;
    private final MeterRegistry meterRegistry;

    @PostConstruct
    public void registerMetrics() {
        Gauge.builder("synaptiq.integration.routes.active",
                        engineManager, mgr -> mgr.getActiveRouteIds().size())
                .description("Number of active Camel integration routes")
                .register(meterRegistry);

        Gauge.builder("synaptiq.integration.engine.status",
                        engineManager, mgr -> mgr.isRunning() ? 1.0 : 0.0)
                .description("Camel integration engine status (1=running, 0=stopped)")
                .register(meterRegistry);

        log.info("Camel integration metrics registered with Micrometer");
    }
}
