package com.spectrayan.synaptiq.integration.application.port.in;

import com.spectrayan.synaptiq.integration.model.ConnectionTestResult;
import com.spectrayan.synaptiq.integration.model.RouteConfig;
import reactor.core.publisher.Mono;

/**
 * Inbound port for integration write operations.
 */
public interface IntegrationCommandUseCase {

    /**
     * Create a new integration for a tenant.
     */
    Mono<RouteConfig> create(CreateIntegrationCommand command);

    /**
     * Activate a previously created integration.
     */
    Mono<RouteConfig> activate(String routeConfigId);

    /**
     * Deactivate a running integration.
     */
    Mono<RouteConfig> deactivate(String routeConfigId);

    /**
     * Test the connectivity of an integration.
     */
    Mono<ConnectionTestResult> testConnection(String routeConfigId);

    /**
     * Delete an integration entirely.
     */
    Mono<Void> delete(String routeConfigId);

    record CreateIntegrationCommand(
            String tenantId,
            String name,
            String description,
            String connectorType,
            String templateId,
            java.util.Map<String, String> parameters,
            String credentialRef
    ) {}
}
