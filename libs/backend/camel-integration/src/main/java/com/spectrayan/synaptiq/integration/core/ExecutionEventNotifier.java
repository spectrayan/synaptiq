package com.spectrayan.synaptiq.integration.core;

import com.spectrayan.synaptiq.integration.model.ExecutionResult;
import com.spectrayan.synaptiq.integration.spi.ExecutionLogger;
import com.spectrayan.synaptiq.integration.spi.RouteConfigProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.spi.CamelEvent;
import org.apache.camel.support.EventNotifierSupport;

import java.util.Map;
import java.util.UUID;

/**
 * Listens to Camel exchange completion events and delegates to the
 * {@link ExecutionLogger} SPI for audit persistence.
 * <p>
 * This is the bridge between Camel's event model and the application's
 * execution logging infrastructure. It fires on every exchange completion
 * (success or failure), extracts tenant context from the route ID,
 * and builds an {@link ExecutionResult} for downstream processing.
 */
@Slf4j
@RequiredArgsConstructor
public class ExecutionEventNotifier extends EventNotifierSupport {

    private final ExecutionLogger executionLogger;
    private final RouteConfigProvider routeConfigProvider;

    @Override
    public void notify(CamelEvent event) {
        if (event instanceof CamelEvent.ExchangeCompletedEvent completed) {
            handleExchangeCompleted(completed.getExchange(), true);
        } else if (event instanceof CamelEvent.ExchangeFailedEvent failed) {
            handleExchangeCompleted(failed.getExchange(), false);
        }
    }

    @Override
    public boolean isEnabled(CamelEvent event) {
        return event instanceof CamelEvent.ExchangeCompletedEvent
                || event instanceof CamelEvent.ExchangeFailedEvent;
    }

    private void handleExchangeCompleted(Exchange exchange, boolean success) {
        String routeId = exchange.getFromRouteId();
        if (routeId == null) return;

        String tenantId = TenantRouteNamingStrategy.extractTenantId(routeId);
        String routeConfigId = TenantRouteNamingStrategy.extractRouteConfigId(routeId);

        // Skip non-tenant routes (internal/template routes)
        if (tenantId == null || routeConfigId == null) return;

        long durationMs = 0;
        if (exchange.getCreated() > 0) {
            durationMs = System.currentTimeMillis() - exchange.getCreated();
        }

        ExecutionResult result;
        if (success) {
            result = ExecutionResult.success(
                    routeConfigId, tenantId, routeId, durationMs,
                    Map.of("messageId", exchange.getExchangeId()));
        } else {
            String error = exchange.getException() != null
                    ? exchange.getException().getMessage()
                    : "Unknown error";
            result = ExecutionResult.failure(
                    routeConfigId, tenantId, routeId, durationMs, error);
        }
        result.setExecutionId(UUID.randomUUID().toString());

        // Fire-and-forget — don't block Camel's processing thread
        executionLogger.log(result)
                .doOnError(e -> log.error("Failed to log execution for route {}: {}",
                        routeId, e.getMessage()))
                .subscribe();

        // Update lastExecutedAt on the config (best-effort)
        routeConfigProvider.findByRouteConfigId(routeConfigId)
                .flatMap(config -> {
                    config.setLastExecutedAt(result.getExecutedAt());
                    if (!success) {
                        config.setLastError(result.getErrorMessage());
                    }
                    return routeConfigProvider.save(config);
                })
                .doOnError(e -> log.debug("Could not update lastExecutedAt for {}: {}",
                        routeConfigId, e.getMessage()))
                .subscribe();
    }
}
