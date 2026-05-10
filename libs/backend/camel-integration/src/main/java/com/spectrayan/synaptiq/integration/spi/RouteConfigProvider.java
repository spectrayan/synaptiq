package com.spectrayan.synaptiq.integration.spi;

import com.spectrayan.synaptiq.integration.model.RouteConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * SPI for loading and persisting route configurations.
 * <p>
 * The consuming application (e.g., spring-apis) implements this backed
 * by MongoDB, PostgreSQL, or any other persistence layer.
 */
public interface RouteConfigProvider {

    /**
     * Find a route configuration by its unique ID.
     */
    Mono<RouteConfig> findByRouteConfigId(String routeConfigId);

    /**
     * Find all active routes for a given tenant (loaded on startup / tenant onboarding).
     */
    Flux<RouteConfig> findActiveByTenantId(String tenantId);

    /**
     * Find all route configs for a tenant regardless of status.
     */
    Flux<RouteConfig> findAllByTenantId(String tenantId);

    /**
     * Find all active routes across all tenants.
     * Used by {@link com.spectrayan.synaptiq.integration.core.StartupRouteLoader}
     * to reload routes on application restart.
     */
    Flux<RouteConfig> findAllActive();

    /**
     * Persist a new or updated route configuration.
     */
    Mono<RouteConfig> save(RouteConfig config);

    /**
     * Delete a route configuration.
     */
    Mono<Void> deleteByRouteConfigId(String routeConfigId);

    /**
     * Update the status (and optional error message) of a route.
     */
    Mono<RouteConfig> updateStatus(String routeConfigId,
                                   com.spectrayan.synaptiq.integration.model.RouteStatus status,
                                   String errorMessage);
}
