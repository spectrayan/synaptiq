package com.spectrayan.synaptiq.integration.health;

import com.spectrayan.synaptiq.integration.core.CamelEngineManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.ServiceStatus;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health reporter for the Camel integration engine.
 * <p>
 * Exposes engine health as a map — can be consumed by Actuator,
 * REST endpoints, or admin dashboards.
 */
@Slf4j
@RequiredArgsConstructor
public class CamelHealthIndicator {

    private final CamelEngineManager engineManager;

    /**
     * Get the engine health status as a map.
     */
    public Map<String, Object> health() {
        CamelContext ctx = engineManager.getCamelContext();
        ServiceStatus status = ctx.getStatus();

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("status", status == ServiceStatus.Started ? "UP" : "DOWN");
        details.put("camelStatus", status.name());
        details.put("contextName", ctx.getName());
        details.put("activeRoutes", ctx.getRoutes().size());
        details.put("uptime", ctx.getUptime());
        return details;
    }

    /**
     * Whether the engine is healthy (started).
     */
    public boolean isHealthy() {
        return engineManager.getCamelContext().getStatus() == ServiceStatus.Started;
    }
}
