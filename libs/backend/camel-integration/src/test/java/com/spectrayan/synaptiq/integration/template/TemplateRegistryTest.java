package com.spectrayan.synaptiq.integration.template;

import com.spectrayan.synaptiq.integration.model.TemplateDescriptor;
import com.spectrayan.synaptiq.integration.stub.InMemoryTemplateConfigProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link TemplateRegistry} — built-in templates,
 * custom DB-backed templates, and template precedence rules.
 */
@DisplayName("TemplateRegistry")
class TemplateRegistryTest {

    private InMemoryTemplateConfigProvider dbProvider;
    private TemplateRegistry registry;

    @BeforeEach
    void setUp() {
        dbProvider = new InMemoryTemplateConfigProvider();
        registry = new TemplateRegistry(dbProvider);
    }

    @Nested
    @DisplayName("Built-in templates")
    class BuiltInTemplateTests {

        @Test
        @DisplayName("should have all 5 built-in templates registered")
        void hasAllBuiltIns() {
            List<TemplateDescriptor> templates = registry.listTemplates();
            assertThat(templates).hasSizeGreaterThanOrEqualTo(5);

            List<String> ids = templates.stream()
                    .map(TemplateDescriptor::getTemplateId)
                    .toList();
            assertThat(ids).contains(
                    "rest-api-poll",
                    "webhook-receiver",
                    "slack-notify",
                    "email-notify",
                    "db-query"
            );
        }

        @Test
        @DisplayName("all built-in templates should be marked as builtIn")
        void builtInsAreMarked() {
            for (TemplateDescriptor t : BuiltInTemplates.all()) {
                Optional<TemplateDescriptor> found = registry.findTemplate(t.getTemplateId());
                assertThat(found).isPresent();
                assertThat(found.get().isBuiltIn()).isTrue();
            }
        }

        @Test
        @DisplayName("should find default template for REST_API connector type")
        void findsDefaultForRestApi() {
            Optional<TemplateDescriptor> found = registry.findDefaultTemplateForType("REST_API");
            assertThat(found).isPresent();
            assertThat(found.get().getTemplateId()).isEqualTo("rest-api-poll");
        }

        @Test
        @DisplayName("should find default template for each built-in connector type")
        void findsDefaultsForAllTypes() {
            for (String type : List.of("REST_API", "WEBHOOK", "SLACK", "EMAIL", "DATABASE")) {
                assertThat(registry.findDefaultTemplateForType(type))
                        .as("Default template for " + type)
                        .isPresent();
            }
        }

        @Test
        @DisplayName("should return empty for unknown connector type")
        void returnsEmptyForUnknown() {
            assertThat(registry.findDefaultTemplateForType("UNKNOWN")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Custom templates from DB")
    class CustomTemplateTests {

        @Test
        @DisplayName("should include custom templates in listings")
        void includesCustomTemplates() {
            TemplateDescriptor custom = TemplateDescriptor.builder()
                    .templateId("custom-api")
                    .displayName("Custom API")
                    .connectorType("CUSTOM")
                    .builtIn(false)
                    .build();
            dbProvider.save(custom).block();

            List<TemplateDescriptor> all = registry.listTemplates();
            assertThat(all.stream().map(TemplateDescriptor::getTemplateId))
                    .contains("custom-api");
        }

        @Test
        @DisplayName("built-in templates should take precedence over DB entries with same ID")
        void builtInTakesPrecedence() {
            // Save a custom template with the same ID as a built-in
            TemplateDescriptor shadow = TemplateDescriptor.builder()
                    .templateId("rest-api-poll")
                    .displayName("Shadowed REST")
                    .connectorType("REST_API")
                    .builtIn(false)
                    .build();
            dbProvider.save(shadow).block();

            Optional<TemplateDescriptor> found = registry.findTemplate("rest-api-poll");
            assertThat(found).isPresent();
            assertThat(found.get().isBuiltIn()).isTrue();
            assertThat(found.get().getDisplayName()).isEqualTo("REST API Polling");
        }

        @Test
        @DisplayName("should not allow modification of built-in templates")
        void cannotModifyBuiltIn() {
            TemplateDescriptor modified = TemplateDescriptor.builder()
                    .templateId("rest-api-poll")
                    .displayName("Modified")
                    .build();

            assertThatThrownBy(() -> registry.saveCustomTemplate(modified))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("built-in");
        }

        @Test
        @DisplayName("should not allow deletion of built-in templates")
        void cannotDeleteBuiltIn() {
            assertThatThrownBy(() -> registry.deleteCustomTemplate("rest-api-poll"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("built-in");
        }

        @Test
        @DisplayName("should save and retrieve custom template")
        void savesAndRetrievesCustom() {
            TemplateDescriptor custom = TemplateDescriptor.builder()
                    .templateId("my-custom-template")
                    .displayName("My Custom Template")
                    .connectorType("CUSTOM")
                    .builtIn(false)
                    .routeYaml("route-yaml-content")
                    .createdAt(Instant.now())
                    .build();

            TemplateDescriptor saved = registry.saveCustomTemplate(custom);
            assertThat(saved.getTemplateId()).isEqualTo("my-custom-template");
            assertThat(saved.isBuiltIn()).isFalse();

            Optional<TemplateDescriptor> found = registry.findTemplate("my-custom-template");
            assertThat(found).isPresent();
            assertThat(found.get().getRouteYaml()).isEqualTo("route-yaml-content");
        }

        @Test
        @DisplayName("should delete custom template")
        void deletesCustomTemplate() {
            TemplateDescriptor custom = TemplateDescriptor.builder()
                    .templateId("deleteable")
                    .displayName("Deleteable")
                    .connectorType("CUSTOM")
                    .build();
            dbProvider.save(custom).block();

            registry.deleteCustomTemplate("deleteable");

            assertThat(registry.findTemplate("deleteable")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tenant-scoped template listing")
    class TenantScopedTests {

        @Test
        @DisplayName("should include global and tenant-scoped templates for a specific tenant")
        void includesGlobalAndTenantScoped() {
            // Global custom template
            dbProvider.save(TemplateDescriptor.builder()
                    .templateId("global-custom")
                    .displayName("Global")
                    .connectorType("CUSTOM")
                    .build()).block();

            // Tenant-scoped custom template
            dbProvider.save(TemplateDescriptor.builder()
                    .templateId("tenant-specific")
                    .displayName("Tenant Specific")
                    .connectorType("CUSTOM")
                    .tenantId("tenant-A")
                    .build()).block();

            List<TemplateDescriptor> forTenantA = registry.listTemplates("tenant-A");
            assertThat(forTenantA.stream().map(TemplateDescriptor::getTemplateId))
                    .contains("global-custom", "tenant-specific");
        }
    }

    @Nested
    @DisplayName("Registry without DB provider")
    class NullProviderTests {

        @Test
        @DisplayName("should work with null TemplateConfigProvider")
        void worksWithoutDbProvider() {
            TemplateRegistry noPersistence = new TemplateRegistry(null);

            // Should still list built-ins
            assertThat(noPersistence.listTemplates()).hasSizeGreaterThanOrEqualTo(5);
            assertThat(noPersistence.findTemplate("rest-api-poll")).isPresent();
        }

        @Test
        @DisplayName("should throw on save when no provider")
        void throwsOnSaveWithoutProvider() {
            TemplateRegistry noPersistence = new TemplateRegistry(null);
            TemplateDescriptor custom = TemplateDescriptor.builder()
                    .templateId("no-db")
                    .displayName("No DB")
                    .connectorType("CUSTOM")
                    .build();

            assertThatThrownBy(() -> noPersistence.saveCustomTemplate(custom))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("TemplateConfigProvider");
        }
    }
}
