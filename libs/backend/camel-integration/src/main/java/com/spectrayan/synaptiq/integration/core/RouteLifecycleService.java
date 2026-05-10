package com.spectrayan.synaptiq.integration.core;

import com.spectrayan.synaptiq.integration.adapter.RouteAdapter;
import com.spectrayan.synaptiq.integration.model.*;
import com.spectrayan.synaptiq.integration.spi.CredentialProvider;
import com.spectrayan.synaptiq.integration.spi.ExecutionLogger;
import com.spectrayan.synaptiq.integration.spi.RouteConfigProvider;
import com.spectrayan.synaptiq.integration.template.TemplateRegistry;
import com.spectrayan.synaptiq.integration.tenant.TenantRateLimitPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * High-level service for route lifecycle operations.
 * <p>
 * Orchestrates: validate → resolve credentials → load route → update status.
 * Bridges the reactive SPI layer with the blocking Camel API.
 */
@Slf4j
@RequiredArgsConstructor
public class RouteLifecycleService {

    private final CamelEngineManager engineManager;
    private final RouteAdapterRegistry adapterRegistry;
    private final RouteConfigProvider configProvider;
    private final CredentialProvider credentialProvider;
    private final ExecutionLogger executionLogger;
    private final TemplateRegistry templateRegistry;
    private final TenantRateLimitPolicy rateLimitPolicy;

    /**
     * Activate a route: resolve credentials, generate YAML or use template, load into Camel.
     */
    public Mono<RouteConfig> activateRoute(String routeConfigId) {
        return configProvider.findByRouteConfigId(routeConfigId)
                .flatMap(config -> {
                    // Check tenant rate limit
                    if (!rateLimitPolicy.canAddRoute(config.getTenantId())) {
                        return Mono.error(new IllegalStateException(
                                "Tenant " + config.getTenantId() + " has reached max route limit"));
                    }

                    String camelRouteId = TenantRouteNamingStrategy.buildRouteId(
                            config.getTenantId(), config.getRouteConfigId());
                    config.setCamelRouteId(camelRouteId);

                    return resolveAndLoadRoute(config)
                            .then(configProvider.updateStatus(
                                    routeConfigId, RouteStatus.ACTIVE, null))
                            .doOnSuccess(c -> log.info("Activated route: {} for tenant: {}",
                                    camelRouteId, config.getTenantId()))
                            .onErrorResume(e -> {
                                log.error("Failed to activate route: {}", routeConfigId, e);
                                return configProvider.updateStatus(
                                        routeConfigId, RouteStatus.ERROR, e.getMessage());
                            });
                });
    }

    /**
     * Deactivate a route: stop and remove from CamelContext.
     */
    public Mono<RouteConfig> deactivateRoute(String routeConfigId) {
        return configProvider.findByRouteConfigId(routeConfigId)
                .flatMap(config -> {
                    String camelRouteId = config.getCamelRouteId();
                    if (camelRouteId == null) {
                        camelRouteId = TenantRouteNamingStrategy.buildRouteId(
                                config.getTenantId(), config.getRouteConfigId());
                    }
                    String finalRouteId = camelRouteId;
                    return Mono.fromCallable(() -> {
                                engineManager.removeRoute(finalRouteId);
                                return true;
                            })
                            .subscribeOn(Schedulers.boundedElastic())
                            .then(configProvider.updateStatus(
                                    routeConfigId, RouteStatus.DISABLED, null));
                });
    }

    /**
     * Reload a route: remove old → load new config.
     */
    public Mono<RouteConfig> reloadRoute(String routeConfigId) {
        return deactivateRoute(routeConfigId)
                .then(activateRoute(routeConfigId));
    }

    /**
     * Test a route's connectivity without activating it.
     * If no adapter is registered for the connector type, returns a generic success.
     */
    public Mono<ConnectionTestResult> testConnection(String routeConfigId) {
        return configProvider.findByRouteConfigId(routeConfigId)
                .flatMap(config -> {
                    var adapterOpt = adapterRegistry.findAdapter(config.getConnectorType());
                    if (adapterOpt.isEmpty()) {
                        return Mono.just(ConnectionTestResult.success(
                                "No specialized test adapter for type '"
                                        + config.getConnectorType() + "'. Configuration looks valid.", 0));
                    }
                    return adapterOpt.get().testConnection(config)
                            .flatMap(result -> {
                                config.setLastTestedAt(Instant.now());
                                return configProvider.save(config).thenReturn(result);
                            });
                });
    }

    /**
     * Load all active routes for a tenant (used on startup or tenant login).
     */
    public Flux<String> loadAllRoutesForTenant(String tenantId) {
        return configProvider.findActiveByTenantId(tenantId)
                .flatMap(config -> activateRoute(config.getRouteConfigId())
                        .map(RouteConfig::getCamelRouteId)
                        .onErrorResume(e -> {
                            log.error("Failed to load route {} for tenant {}",
                                    config.getRouteConfigId(), tenantId, e);
                            return Mono.empty();
                        }));
    }

    /**
     * Remove all routes for a tenant (used on tenant offboarding).
     */
    public Mono<Void> removeAllRoutesForTenant(String tenantId) {
        return Mono.fromCallable(() -> {
            for (String routeId : engineManager.getRouteIdsForTenant(tenantId)) {
                try {
                    engineManager.removeRoute(routeId);
                } catch (Exception e) {
                    log.error("Failed to remove route: {}", routeId, e);
                }
            }
            return true;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    // ═══════════════════════════════════════════════════════════════
    //  Internal helpers
    // ═══════════════════════════════════════════════════════════════

    private Mono<Void> resolveAndLoadRoute(RouteConfig config) {
        // Resolve credentials if present
        Mono<Map<String, String>> paramsMono;
        if (config.getCredentialRef() != null && !config.getCredentialRef().isBlank()) {
            paramsMono = credentialProvider.resolve(config.getCredentialRef())
                    .map(secret -> {
                        Map<String, String> params = new HashMap<>(config.getParameters());
                        params.put("_resolvedCredential", secret);
                        return params;
                    });
        } else {
            paramsMono = Mono.just(new HashMap<>(config.getParameters()));
        }

        return paramsMono.flatMap(resolvedParams -> {
            // Path 1: Custom YAML — advanced users provide raw Camel YAML DSL
            if (config.getRouteYaml() != null && !config.getRouteYaml().isBlank()) {
                return loadFromYaml(config);
            }

            // Path 2: Template-based — uses Camel's native TemplatedRouteBuilder
            // Auto-resolve templateId from connectorType if not explicitly set
            String templateId = config.getTemplateId();
            if (templateId == null || templateId.isBlank()) {
                templateId = templateRegistry.findDefaultTemplateForType(config.getConnectorType())
                        .orElseThrow(() -> new IllegalStateException(
                                "No template found for connector type: " + config.getConnectorType()
                                        + ". Provide a templateId or routeYaml."))
                        .getTemplateId();
                config.setTemplateId(templateId);
            }
            return loadFromTemplate(config, resolvedParams);
        });
    }

    /**
     * Instantiate a route from a Camel RouteTemplate (native Java DSL).
     */
    private Mono<Void> loadFromTemplate(RouteConfig config, Map<String, String> resolvedParams) {
        return Mono.fromCallable(() -> {
            templateRegistry.instantiate(
                    engineManager.getCamelContext(),
                    config.getTemplateId(),
                    config.getCamelRouteId(),
                    config.getTenantId(),
                    resolvedParams);
            return true;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Load a route from raw Camel YAML DSL (native RoutesLoader).
     */
    private Mono<Void> loadFromYaml(RouteConfig config) {
        return Mono.fromCallable(() -> {
            engineManager.loadRouteFromYaml(config.getCamelRouteId(), config.getRouteYaml());
            return true;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
