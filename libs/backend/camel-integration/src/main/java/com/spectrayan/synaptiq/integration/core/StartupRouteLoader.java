package com.spectrayan.synaptiq.integration.core;

import com.spectrayan.synaptiq.integration.spi.RouteConfigProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Loads all active integration routes from the persistence layer
 * when the application finishes starting.
 * <p>
 * This is the "configuration loader" that bridges the database-stored
 * route definitions with the running Camel engine. On application startup:
 * <ol>
 *   <li>Queries all {@code ACTIVE} routes from {@link RouteConfigProvider}</li>
 *   <li>For each, delegates to {@link RouteLifecycleService#activateRoute} which:
 *       <ul>
 *         <li>Resolves credentials via {@link com.spectrayan.synaptiq.integration.spi.CredentialProvider}</li>
 *         <li>Looks up the template or generates YAML via the adapter</li>
 *         <li>Loads the route into the running CamelContext</li>
 *       </ul>
 *   </li>
 *   <li>Logs summary of loaded/failed routes per tenant</li>
 * </ol>
 * <p>
 * Routes that fail to load are marked as ERROR in the database
 * (handled by {@link RouteLifecycleService}).
 */
@Slf4j
@RequiredArgsConstructor
public class StartupRouteLoader {

    private final RouteConfigProvider routeConfigProvider;
    private final RouteLifecycleService routeLifecycleService;
    private final AtomicBoolean loaded = new AtomicBoolean(false);

    /**
     * Triggered after Spring context is fully initialized and the
     * CamelContext has been started by {@link CamelEngineManager}.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (!loaded.compareAndSet(false, true)) {
            log.debug("StartupRouteLoader already executed — skipping duplicate event");
            return;
        }

        log.info("Loading active integration routes from persistence...");

        routeConfigProvider.findAllActive()
                .flatMap(config -> {
                    log.debug("Loading route '{}' (tenant: {}, type: {})",
                            config.getName(), config.getTenantId(), config.getConnectorType());
                    return routeLifecycleService.activateRoute(config.getRouteConfigId())
                            .doOnNext(c -> log.info("✓ Loaded route '{}' for tenant '{}'",
                                    c.getName(), c.getTenantId()))
                            .doOnError(e -> log.error("✗ Failed to load route '{}': {}",
                                    config.getName(), e.getMessage()))
                            .onErrorResume(e -> reactor.core.publisher.Mono.empty());
                })
                .collectList()
                .subscribe(
                        loaded -> log.info("Startup route loading complete: {} routes activated", loaded.size()),
                        error -> log.error("Startup route loading failed", error)
                );
    }
}
