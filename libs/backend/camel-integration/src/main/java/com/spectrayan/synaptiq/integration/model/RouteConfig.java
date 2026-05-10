package com.spectrayan.synaptiq.integration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Tenant integration route definition.
 * <p>
 * Represents a configured integration for a specific tenant.
 * Can be template-based (templateId + parameters) or custom YAML.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteConfig {

    /** Unique route configuration ID. */
    private String routeConfigId;

    /** Owning tenant ID — used for isolation. */
    private String tenantId;

    /** Human-readable name for this integration. */
    private String name;

    /** Optional description. */
    private String description;

    /**
     * Connector type label. Free-form string to support both built-in
     * types (REST_API, WEBHOOK, etc.) and custom tenant-defined types.
     */
    private String connectorType;

    /**
     * Template ID — references a built-in or custom template.
     * If null, {@link #routeYaml} must be provided for custom routes.
     */
    private String templateId;

    /**
     * Parameters for template instantiation.
     * Keys correspond to template parameter names.
     */
    @Builder.Default
    private Map<String, String> parameters = new HashMap<>();

    /**
     * Raw Camel YAML DSL — for advanced/custom routes.
     * Used when {@link #templateId} is null.
     */
    private String routeYaml;

    /** Reference to the credential (encrypted secret, OAuth2 token, etc.). */
    private String credentialRef;

    /** Current lifecycle status. */
    @Builder.Default
    private RouteStatus status = RouteStatus.PENDING;

    /**
     * Computed Camel route ID: {@code {tenantId}__{routeConfigId}}.
     * Set by the engine when route is activated.
     */
    private String camelRouteId;

    /** Last successful connection test timestamp. */
    private Instant lastTestedAt;

    /** Last execution timestamp. */
    private Instant lastExecutedAt;

    /** Error message from last failure (if status == ERROR). */
    private String lastError;

    private Instant createdAt;
    private Instant updatedAt;
}
