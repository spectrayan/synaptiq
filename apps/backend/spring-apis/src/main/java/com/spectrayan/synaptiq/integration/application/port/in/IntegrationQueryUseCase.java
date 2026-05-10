package com.spectrayan.synaptiq.integration.application.port.in;

import com.spectrayan.synaptiq.integration.model.RouteConfig;
import com.spectrayan.synaptiq.integration.model.TemplateDescriptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Inbound port for integration read operations.
 */
public interface IntegrationQueryUseCase {

    /**
     * List all integrations for a tenant.
     */
    Flux<RouteConfig> listByTenantId(String tenantId);

    /**
     * Get a single integration by ID.
     */
    Mono<RouteConfig> getByRouteConfigId(String routeConfigId);

    /**
     * List all available integration templates.
     */
    List<TemplateDescriptor> listTemplates();
}
