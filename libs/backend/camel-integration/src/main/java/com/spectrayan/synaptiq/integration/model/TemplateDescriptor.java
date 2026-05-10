package com.spectrayan.synaptiq.integration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes an integration template that tenants can browse and instantiate.
 * <p>
 * Templates can be:
 * <ul>
 *   <li><b>Built-in</b> ({@code builtIn = true}): Shipped with the library,
 *       defined as native Camel RouteTemplates in Java DSL. These are seed data
 *       and cannot be modified by tenants.</li>
 *   <li><b>Custom</b> ({@code builtIn = false}): Created by admins at runtime,
 *       stored in MongoDB. These carry a {@link #routeYaml} field containing
 *       Camel YAML DSL that gets loaded into the CamelContext dynamically.</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDescriptor {

    /** Unique template identifier (matches the Camel routeTemplate ID for built-ins). */
    private String templateId;

    /** Display name shown in the UI. */
    private String displayName;

    /** Human-readable description. */
    private String description;

    /** Icon identifier (e.g., "slack", "rest-api", "database"). */
    private String icon;

    /** Category for grouping (e.g., "Messaging", "Data", "Notification"). */
    private String category;

    /**
     * Connector type label. Free-form string to allow tenants to define
     * custom connector types without code changes.
     * Built-in types: REST_API, WEBHOOK, DATABASE, SLACK, EMAIL, etc.
     */
    private String connectorType;

    /** Parameter definitions — describes what the tenant must configure. */
    @Builder.Default
    private List<ParameterDefinition> parameters = new ArrayList<>();

    /** Whether this template requires credentials. */
    @Builder.Default
    private boolean requiresCredential = false;

    /**
     * Whether this is a system built-in template (not editable by tenants)
     * or a custom template created at runtime.
     */
    @Builder.Default
    private boolean builtIn = false;

    /**
     * Optional tenant scope. If null, the template is global (available to all tenants).
     * If set, only visible to the specified tenant.
     */
    private String tenantId;

    /**
     * Camel YAML DSL for custom templates. Built-in templates use compiled
     * Java RouteTemplate definitions instead and leave this null.
     * <p>
     * The YAML must define a {@code routeTemplate} with parameter placeholders
     * matching the {@link #parameters} definitions. Example:
     * <pre>{@code
     * - route-template:
     *     id: "my-custom-api"
     *     parameters:
     *       - name: "url"
     *       - name: "apiKey"
     *     from:
     *       uri: "timer:{{routeId}}?period=60000"
     *     steps:
     *       - set-header:
     *           name: "Authorization"
     *           constant: "Bearer {{apiKey}}"
     *       - to:
     *           uri: "{{url}}"
     *       - log:
     *           message: "API call result: ${body}"
     * }</pre>
     */
    private String routeYaml;

    /** When this template was created. */
    private Instant createdAt;

    /** When this template was last updated. */
    private Instant updatedAt;

    /**
     * Describes a single parameter that a tenant must provide
     * when instantiating this template.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParameterDefinition {
        private String name;
        private String displayName;
        private String description;
        private String type; // "string", "number", "boolean", "secret", "cron", "url"
        @Builder.Default
        private boolean required = true;
        private String defaultValue;
        private String placeholder;
    }
}
