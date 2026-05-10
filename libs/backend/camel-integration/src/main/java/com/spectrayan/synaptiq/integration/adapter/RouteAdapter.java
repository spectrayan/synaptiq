package com.spectrayan.synaptiq.integration.adapter;

import com.spectrayan.synaptiq.integration.model.ConnectionTestResult;
import com.spectrayan.synaptiq.integration.model.RouteConfig;
import reactor.core.publisher.Mono;

/**
 * Adapter for connector-type-specific validation and connectivity testing.
 * <p>
 * Adapters are <b>optional</b> — they provide specialized validation and
 * connectivity testing for known connector types. If no adapter is registered
 * for a connector type, generic validation is used.
 * <p>
 * Route construction is handled entirely by Camel's native RouteTemplate
 * definitions (built-in Java DSL or custom YAML from DB).
 * Adapters do NOT build routes.
 */
public interface RouteAdapter {

    /**
     * Which connector type this adapter handles.
     * Returns a string to support both built-in and custom connector types.
     */
    String supportedType();

    /**
     * Validate the route configuration before saving.
     *
     * @param config the route configuration to validate
     * @return empty mono if valid, error mono with validation message if invalid
     */
    Mono<Void> validateConfig(RouteConfig config);

    /**
     * Test connectivity for the given route configuration.
     */
    Mono<ConnectionTestResult> testConnection(RouteConfig config);
}
