package com.spectrayan.synaptiq.integration.core;

import com.spectrayan.synaptiq.integration.model.RouteConfig;
import com.spectrayan.synaptiq.integration.model.RouteStatus;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Fluent builder for constructing {@link RouteConfig} instances programmatically.
 * <p>
 * Provides validation, default values, and a clean DSL for creating route
 * configurations outside of template-based flows. Useful for:
 * <ul>
 *   <li>Programmatic route creation from code/tests</li>
 *   <li>Building configs from deserialized JSON/YAML</li>
 *   <li>MCP-driven dynamic integration setup</li>
 * </ul>
 *
 * <pre>{@code
 * RouteConfig config = RouteConfigBuilder.forTenant("tenant-123")
 *     .name("Slack Alerts")
 *     .connectorType("SLACK")
 *     .templateId("slack-notify")
 *     .param("channel", "#ops-alerts")
 *     .param("webhookUrl", "https://hooks.slack.com/...")
 *     .credentialRef("credential/slack-token")
 *     .build();
 * }</pre>
 */
public class RouteConfigBuilder {

    private String tenantId;
    private String routeConfigId;
    private String name;
    private String description;
    private String connectorType;
    private String templateId;
    private final Map<String, String> parameters = new HashMap<>();
    private String routeYaml;
    private String credentialRef;

    private RouteConfigBuilder(String tenantId) {
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId must not be null");
        this.routeConfigId = UUID.randomUUID().toString();
    }

    /**
     * Start building a route config for a specific tenant.
     */
    public static RouteConfigBuilder forTenant(String tenantId) {
        return new RouteConfigBuilder(tenantId);
    }

    public RouteConfigBuilder routeConfigId(String id) {
        this.routeConfigId = id;
        return this;
    }

    public RouteConfigBuilder name(String name) {
        this.name = name;
        return this;
    }

    public RouteConfigBuilder description(String description) {
        this.description = description;
        return this;
    }

    public RouteConfigBuilder connectorType(String type) {
        this.connectorType = type;
        return this;
    }

    public RouteConfigBuilder templateId(String templateId) {
        this.templateId = templateId;
        return this;
    }

    public RouteConfigBuilder param(String key, String value) {
        this.parameters.put(key, value);
        return this;
    }

    public RouteConfigBuilder params(Map<String, String> params) {
        this.parameters.putAll(params);
        return this;
    }

    public RouteConfigBuilder routeYaml(String yaml) {
        this.routeYaml = yaml;
        return this;
    }

    public RouteConfigBuilder credentialRef(String ref) {
        this.credentialRef = ref;
        return this;
    }

    /**
     * Build and validate the route configuration.
     *
     * @return a fully constructed {@link RouteConfig} in PENDING status
     * @throws IllegalStateException if required fields are missing
     */
    public RouteConfig build() {
        validate();
        Instant now = Instant.now();
        return RouteConfig.builder()
                .routeConfigId(routeConfigId)
                .tenantId(tenantId)
                .name(name)
                .description(description)
                .connectorType(connectorType)
                .templateId(templateId)
                .parameters(new HashMap<>(parameters))
                .routeYaml(routeYaml)
                .credentialRef(credentialRef)
                .status(RouteStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Route name is required");
        }
        if (connectorType == null || connectorType.isBlank()) {
            throw new IllegalStateException("connectorType is required");
        }
        if (templateId == null && routeYaml == null && parameters.isEmpty()) {
            throw new IllegalStateException(
                    "Route must have a templateId, routeYaml, or parameters for the adapter to generate YAML");
        }
    }
}
