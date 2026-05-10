package com.spectrayan.synaptiq.integration.template;

import com.spectrayan.synaptiq.integration.model.TemplateDescriptor;
import com.spectrayan.synaptiq.integration.spi.TemplateConfigProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.TemplatedRouteBuilder;
import org.apache.camel.spi.Resource;
import org.apache.camel.spi.RoutesLoader;
import org.apache.camel.support.PluginHelper;
import org.apache.camel.support.ResourceHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Composite template registry — serves templates from two sources:
 * <ol>
 *   <li><b>Built-in</b>: Compiled Java RouteTemplate definitions
 *       (from {@link IntegrationRouteTemplates}). These use Camel's native
 *       {@link TemplatedRouteBuilder} for instantiation.</li>
 *   <li><b>Custom (DB-stored)</b>: Templates created at runtime by admins/tenants,
 *       stored via {@link TemplateConfigProvider}. These carry Camel YAML DSL
 *       and are loaded via Camel's native {@link RoutesLoader}.</li>
 * </ol>
 * <p>
 * This design means <b>no code changes are needed</b> to add new integration types.
 * Admins can create custom templates via the API with a Camel YAML route definition,
 * and tenants can instantly use them.
 */
@Slf4j
public class TemplateRegistry {

    /** Built-in templates (immutable, shipped with the library). */
    private final Map<String, TemplateDescriptor> builtInTemplates = new ConcurrentHashMap<>();

    /** Optional DB-backed provider for custom templates. */
    private final TemplateConfigProvider templateConfigProvider;

    public TemplateRegistry(TemplateConfigProvider templateConfigProvider) {
        this.templateConfigProvider = templateConfigProvider;
        // Index built-in templates by ID
        BuiltInTemplates.all().forEach(t -> {
            t.setBuiltIn(true);
            builtInTemplates.put(t.getTemplateId(), t);
        });
        log.info("Template registry initialized with {} built-in templates", builtInTemplates.size());
    }

    // ═══════════════════════════════════════════════════════════════
    //  Query
    // ═══════════════════════════════════════════════════════════════

    /**
     * List all templates available to a tenant: built-ins + global customs + tenant-scoped.
     * Built-in templates always take precedence — DB entries with the same templateId are ignored.
     */
    public List<TemplateDescriptor> listTemplates(String tenantId) {
        List<TemplateDescriptor> result = new ArrayList<>(builtInTemplates.values());
        if (templateConfigProvider != null && tenantId != null) {
            templateConfigProvider.findAllAccessibleByTenant(tenantId)
                    .filter(t -> !builtInTemplates.containsKey(t.getTemplateId()))
                    .collectList()
                    .blockOptional()
                    .ifPresent(result::addAll);
        }
        return result;
    }

    /**
     * List all templates (no tenant filter). Used by admin endpoints.
     * Built-in templates always take precedence — DB duplicates are excluded.
     */
    public List<TemplateDescriptor> listTemplates() {
        List<TemplateDescriptor> result = new ArrayList<>(builtInTemplates.values());
        if (templateConfigProvider != null) {
            templateConfigProvider.findAll()
                    .filter(t -> !builtInTemplates.containsKey(t.getTemplateId()))
                    .collectList()
                    .blockOptional()
                    .ifPresent(result::addAll);
        }
        return result;
    }

    /**
     * Find a template by ID — checks built-in first, then DB.
     * Built-in always wins if both exist.
     */
    public Optional<TemplateDescriptor> findTemplate(String templateId) {
        TemplateDescriptor builtIn = builtInTemplates.get(templateId);
        if (builtIn != null) return Optional.of(builtIn);

        if (templateConfigProvider != null) {
            return templateConfigProvider.findByTemplateId(templateId)
                    .blockOptional();
        }
        return Optional.empty();
    }

    /**
     * Find the default template for a given connector type.
     * Built-in templates are checked first and take precedence.
     */
    public Optional<TemplateDescriptor> findDefaultTemplateForType(String connectorType) {
        // Check built-ins first
        Optional<TemplateDescriptor> builtIn = builtInTemplates.values().stream()
                .filter(t -> connectorType.equals(t.getConnectorType()))
                .findFirst();
        if (builtIn.isPresent()) return builtIn;

        // Then check DB (excluding any that shadow built-in IDs)
        if (templateConfigProvider != null) {
            return templateConfigProvider.findAll()
                    .filter(t -> connectorType.equals(t.getConnectorType()))
                    .filter(t -> !builtInTemplates.containsKey(t.getTemplateId()))
                    .next()
                    .blockOptional();
        }
        return Optional.empty();
    }

    // ═══════════════════════════════════════════════════════════════
    //  Route Instantiation
    // ═══════════════════════════════════════════════════════════════

    /**
     * Instantiate a route from a template in the given CamelContext.
     * <p>
     * For <b>built-in templates</b>: Uses Camel's native {@link TemplatedRouteBuilder}
     * which references the compiled Java RouteTemplate.
     * <p>
     * For <b>custom templates</b>: Loads the YAML DSL into CamelContext via
     * Camel's native {@link RoutesLoader}, then instantiates the template.
     */
    public void instantiate(CamelContext context,
                            String templateId,
                            String routeId,
                            String tenantId,
                            Map<String, String> parameters) {

        TemplateDescriptor descriptor = findTemplate(templateId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Template not found: " + templateId));

        // For custom (DB-stored) templates, load the YAML into CamelContext first
        if (!descriptor.isBuiltIn() && descriptor.getRouteYaml() != null) {
            loadCustomTemplateYaml(context, templateId, descriptor.getRouteYaml());
        }

        // Use Camel's native TemplatedRouteBuilder for instantiation
        TemplatedRouteBuilder builder = TemplatedRouteBuilder.builder(context, templateId)
                .routeId(routeId)
                .parameter("tenantId", tenantId)
                .parameter("routeId", routeId);

        parameters.forEach(builder::parameter);

        try {
            builder.add();
            log.info("Instantiated template '{}' as route '{}' for tenant '{}'",
                    templateId, routeId, tenantId);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to instantiate template '" + templateId
                            + "' as route '" + routeId + "'", e);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Template CRUD (admin operations)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Create or update a custom template. Built-in templates cannot be modified.
     */
    public TemplateDescriptor saveCustomTemplate(TemplateDescriptor template) {
        if (builtInTemplates.containsKey(template.getTemplateId())) {
            throw new IllegalArgumentException(
                    "Cannot modify built-in template: " + template.getTemplateId());
        }
        if (templateConfigProvider == null) {
            throw new IllegalStateException(
                    "TemplateConfigProvider not configured — cannot persist custom templates");
        }
        template.setBuiltIn(false);
        return templateConfigProvider.save(template).block();
    }

    /**
     * Delete a custom template.
     */
    public void deleteCustomTemplate(String templateId) {
        if (builtInTemplates.containsKey(templateId)) {
            throw new IllegalArgumentException(
                    "Cannot delete built-in template: " + templateId);
        }
        if (templateConfigProvider != null) {
            templateConfigProvider.deleteByTemplateId(templateId).block();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Internal
    // ═══════════════════════════════════════════════════════════════

    /** Tracks which custom templates have already been loaded into CamelContext. */
    private final java.util.Set<String> loadedCustomTemplates = ConcurrentHashMap.newKeySet();

    /**
     * Load a custom template's YAML into the CamelContext so it becomes
     * available for TemplatedRouteBuilder.
     */
    private void loadCustomTemplateYaml(CamelContext context,
                                        String templateId,
                                        String yamlContent) {
        if (!loadedCustomTemplates.add(templateId)) {
            return; // Already loaded, skip
        }
        try {
            Resource resource = ResourceHelper.fromBytes(
                    templateId + ".yaml", yamlContent.getBytes());
            RoutesLoader loader = PluginHelper.getRoutesLoader(context);
            loader.loadRoutes(resource);
            log.info("Loaded custom template YAML into CamelContext: {}", templateId);
        } catch (Exception e) {
            loadedCustomTemplates.remove(templateId); // Rollback on failure
            throw new IllegalStateException(
                    "Failed to load custom template YAML: " + templateId, e);
        }
    }
}
