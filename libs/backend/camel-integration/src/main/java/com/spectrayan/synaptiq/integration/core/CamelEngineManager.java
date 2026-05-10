package com.spectrayan.synaptiq.integration.core;

import com.spectrayan.synaptiq.integration.autoconfigure.CamelIntegrationProperties;
import com.spectrayan.synaptiq.integration.tenant.TenantIsolationInterceptor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.Resource;
import org.apache.camel.spi.RoutesLoader;
import org.apache.camel.support.PluginHelper;
import org.apache.camel.support.ResourceHelper;
import org.springframework.context.SmartLifecycle;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages the Apache Camel {@link CamelContext} lifecycle.
 * <p>
 * This is the central engine bean — it owns the CamelContext, handles
 * startup/shutdown coordinated with Spring's lifecycle, and provides
 * methods for dynamically loading/removing routes.
 */
@Slf4j
public class CamelEngineManager implements SmartLifecycle {

    @Getter
    private final CamelContext camelContext;
    private final CamelIntegrationProperties properties;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public CamelEngineManager(CamelIntegrationProperties properties,
                              List<RouteBuilder> routeBuilders,
                              Optional<TenantIsolationInterceptor> interceptor) {
        this.properties = properties;
        this.camelContext = createContext(routeBuilders, interceptor);
    }

    private CamelContext createContext(List<RouteBuilder> routeBuilders,
                                      Optional<TenantIsolationInterceptor> interceptor) {
        DefaultCamelContext ctx = new DefaultCamelContext();

        // Register tenant isolation interceptor
        interceptor.ifPresent(i -> {
            ctx.getCamelContextExtension().addInterceptStrategy(i);
            log.info("Tenant isolation interceptor registered");
        });

        // Register built-in route builders (templates, etc.)
        for (RouteBuilder builder : routeBuilders) {
            try {
                ctx.addRoutes(builder);
                log.debug("Added route builder: {}", builder.getClass().getSimpleName());
            } catch (Exception e) {
                log.error("Failed to add route builder: {}", builder.getClass().getSimpleName(), e);
            }
        }

        return ctx;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Dynamic Route Management
    // ═══════════════════════════════════════════════════════════════

    /**
     * Load a route from YAML DSL string into the running CamelContext.
     */
    public void loadRouteFromYaml(String routeId, String yamlContent) throws Exception {
        Resource resource = ResourceHelper.fromBytes(routeId + ".yaml", yamlContent.getBytes());
        RoutesLoader loader = PluginHelper.getRoutesLoader(camelContext);
        loader.loadRoutes(resource);
        log.info("Loaded route from YAML: {}", routeId);
    }

    /**
     * Remove a route from the running CamelContext.
     */
    public void removeRoute(String routeId) throws Exception {
        if (camelContext.getRoute(routeId) != null) {
            camelContext.getRouteController().stopRoute(routeId);
            camelContext.removeRoute(routeId);
            log.info("Removed route: {}", routeId);
        } else {
            log.debug("Route not found for removal: {}", routeId);
        }
    }

    /**
     * Check if a route is currently loaded and running.
     */
    public boolean isRouteActive(String routeId) {
        Route route = camelContext.getRoute(routeId);
        return route != null;
    }

    /**
     * Get all currently loaded route IDs.
     */
    public List<String> getActiveRouteIds() {
        return camelContext.getRoutes().stream()
                .map(Route::getRouteId)
                .toList();
    }

    /**
     * Get route IDs for a specific tenant.
     */
    public List<String> getRouteIdsForTenant(String tenantId) {
        String prefix = tenantId + TenantRouteNamingStrategy.SEPARATOR;
        return camelContext.getRoutes().stream()
                .map(Route::getRouteId)
                .filter(id -> id.startsWith(prefix))
                .toList();
    }

    // ═══════════════════════════════════════════════════════════════
    //  SmartLifecycle — coordinated with Spring
    // ═══════════════════════════════════════════════════════════════

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            try {
                camelContext.start();
                log.info("Camel integration engine started (routes: {})",
                        camelContext.getRoutes().size());
            } catch (Exception e) {
                running.set(false);
                throw new IllegalStateException("Failed to start Camel integration engine", e);
            }
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            try {
                camelContext.stop();
                log.info("Camel integration engine stopped");
            } catch (Exception e) {
                log.error("Error stopping Camel integration engine", e);
            }
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public int getPhase() {
        // Start after Spring context is ready, stop before
        return Integer.MAX_VALUE - 100;
    }
}
