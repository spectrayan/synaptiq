package com.spectrayan.synaptiq.integration.template;

import com.spectrayan.synaptiq.integration.autoconfigure.CamelIntegrationProperties;
import com.spectrayan.synaptiq.integration.core.CamelEngineManager;
import com.spectrayan.synaptiq.integration.stub.InMemoryTemplateConfigProvider;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for template instantiation — verifies that
 * built-in Camel RouteTemplates can be instantiated with parameters
 * and produce functioning routes with a real CamelContext.
 *
 * <p>Note: Templates that depend on external Camel components (slack, smtp,
 * jdbc, platform-http) cannot be fully tested without those components.
 * We focus on templates using built-in components (direct, timer, mock, log).</p>
 */
@DisplayName("Template Instantiation (Integration)")
class TemplateInstantiationTest {

    private CamelEngineManager engineManager;
    private TemplateRegistry templateRegistry;
    private InMemoryTemplateConfigProvider dbProvider;

    @BeforeEach
    void setUp() {
        CamelIntegrationProperties props = new CamelIntegrationProperties();
        engineManager = new CamelEngineManager(
                props,
                List.of(new IntegrationRouteTemplates()),
                Optional.empty()
        );
        engineManager.start();

        dbProvider = new InMemoryTemplateConfigProvider();
        templateRegistry = new TemplateRegistry(dbProvider);
    }

    @AfterEach
    void tearDown() {
        if (engineManager.isRunning()) {
            engineManager.stop();
        }
    }

    @Nested
    @DisplayName("Built-in template instantiation")
    class BuiltInTemplateInstantiation {

        @Test
        @DisplayName("should instantiate rest-api-poll template with mock endpoint")
        void instantiatesRestApiPoll() {
            CamelContext ctx = engineManager.getCamelContext();

            // rest-api-poll uses timer: → to(url) — override URL to mock:
            templateRegistry.instantiate(ctx, "rest-api-poll",
                    "tenant1__rest-route", "tenant1",
                    Map.of(
                            "url", "mock:rest-output",
                            "method", "GET",
                            "pollIntervalMs", "999999999"
                    ));

            assertThat(engineManager.isRouteActive("tenant1__rest-route")).isTrue();
        }

        @Test
        @DisplayName("should throw for unknown template ID")
        void throwsForUnknownTemplate() {
            CamelContext ctx = engineManager.getCamelContext();

            assertThatThrownBy(() ->
                    templateRegistry.instantiate(ctx, "non-existent-template",
                            "route-id", "tenant-1", Map.of())
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Template not found");
        }

        @Test
        @DisplayName("should instantiate same template for different tenants")
        void sameTenantDifferentRoutes() {
            CamelContext ctx = engineManager.getCamelContext();

            // Both tenants use rest-api-poll
            templateRegistry.instantiate(ctx, "rest-api-poll",
                    "tA__rest-1", "tA",
                    Map.of("url", "mock:tA-rest-out", "pollIntervalMs", "999999999"));

            templateRegistry.instantiate(ctx, "rest-api-poll",
                    "tB__rest-1", "tB",
                    Map.of("url", "mock:tB-rest-out", "pollIntervalMs", "999999999"));

            assertThat(engineManager.isRouteActive("tA__rest-1")).isTrue();
            assertThat(engineManager.isRouteActive("tB__rest-1")).isTrue();
            assertThat(engineManager.getRouteIdsForTenant("tA")).containsExactly("tA__rest-1");
            assertThat(engineManager.getRouteIdsForTenant("tB")).containsExactly("tB__rest-1");
        }

        @Test
        @DisplayName("should instantiate multiple rest-api-poll instances for same tenant")
        void multipleInstancesSameTenant() {
            CamelContext ctx = engineManager.getCamelContext();

            templateRegistry.instantiate(ctx, "rest-api-poll",
                    "tA__rest-1", "tA",
                    Map.of("url", "mock:tA-rest-out1", "pollIntervalMs", "999999999"));

            templateRegistry.instantiate(ctx, "rest-api-poll",
                    "tA__rest-2", "tA",
                    Map.of("url", "mock:tA-rest-out2", "pollIntervalMs", "999999999"));

            assertThat(engineManager.getRouteIdsForTenant("tA")).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Custom YAML template instantiation")
    class CustomYamlTemplateInstantiation {

        @Test
        @DisplayName("should instantiate a custom template from YAML stored in DB")
        void instantiatesCustomTemplate() {
            // Camel 4 route-template YAML DSL — camelCase, steps inside from
            String customYaml = """
                    - routeTemplate:
                        id: "custom-logger"
                        parameters:
                          - name: "tenantId"
                          - name: "routeId"
                          - name: "logMessage"
                            defaultValue: "Default log"
                        from:
                          uri: "direct:{{routeId}}"
                          steps:
                            - log:
                                message: "{{logMessage}} from tenant {{tenantId}}"
                            - to:
                                uri: "mock:custom-output"
                    """;

            var customDescriptor = com.spectrayan.synaptiq.integration.model.TemplateDescriptor.builder()
                    .templateId("custom-logger")
                    .displayName("Custom Logger")
                    .connectorType("CUSTOM")
                    .builtIn(false)
                    .routeYaml(customYaml)
                    .parameters(List.of(
                            com.spectrayan.synaptiq.integration.model.TemplateDescriptor.ParameterDefinition.builder()
                                    .name("logMessage").displayName("Log Message").type("string")
                                    .required(false).defaultValue("Default log").build()
                    ))
                    .build();
            dbProvider.save(customDescriptor).block();

            CamelContext ctx = engineManager.getCamelContext();
            templateRegistry.instantiate(ctx, "custom-logger",
                    "tenant-X__custom-1", "tenant-X",
                    Map.of("logMessage", "Hello Custom World"));

            assertThat(engineManager.isRouteActive("tenant-X__custom-1")).isTrue();
        }

        @Test
        @DisplayName("should route messages through custom template instance")
        void routesMessagesThroughCustomTemplate() throws Exception {
            String customYaml = """
                    - routeTemplate:
                        id: "custom-passthrough"
                        parameters:
                          - name: "tenantId"
                          - name: "routeId"
                        from:
                          uri: "direct:{{routeId}}"
                          steps:
                            - to:
                                uri: "mock:custom-passthrough-output"
                    """;

            dbProvider.save(com.spectrayan.synaptiq.integration.model.TemplateDescriptor.builder()
                    .templateId("custom-passthrough")
                    .displayName("Passthrough")
                    .connectorType("CUSTOM")
                    .builtIn(false)
                    .routeYaml(customYaml)
                    .build()).block();

            CamelContext ctx = engineManager.getCamelContext();
            templateRegistry.instantiate(ctx, "custom-passthrough",
                    "tA__pass-1", "tA", Map.of());

            // Send a message through the route
            MockEndpoint mock = ctx.getEndpoint("mock:custom-passthrough-output", MockEndpoint.class);
            mock.expectedMessageCount(1);
            mock.expectedBodiesReceived("Test payload");

            ProducerTemplate producer = ctx.createProducerTemplate();
            producer.sendBody("direct:tA__pass-1", "Test payload");

            mock.assertIsSatisfied();
        }

        @Test
        @DisplayName("should instantiate custom template with transform logic")
        void customTemplateWithTransform() throws Exception {
            String customYaml = """
                    - routeTemplate:
                        id: "custom-transform"
                        parameters:
                          - name: "tenantId"
                          - name: "routeId"
                          - name: "prefix"
                            defaultValue: "Processed"
                        from:
                          uri: "direct:{{routeId}}"
                          steps:
                            - setBody:
                                simple: "{{prefix}}: ${body}"
                            - to:
                                uri: "mock:custom-transform-output"
                    """;

            dbProvider.save(com.spectrayan.synaptiq.integration.model.TemplateDescriptor.builder()
                    .templateId("custom-transform")
                    .displayName("Transform Template")
                    .connectorType("CUSTOM")
                    .builtIn(false)
                    .routeYaml(customYaml)
                    .build()).block();

            CamelContext ctx = engineManager.getCamelContext();
            templateRegistry.instantiate(ctx, "custom-transform",
                    "tC__transform-1", "tC",
                    Map.of("prefix", "ENRICHED"));

            MockEndpoint mock = ctx.getEndpoint("mock:custom-transform-output", MockEndpoint.class);
            mock.expectedMessageCount(1);
            mock.expectedBodiesReceived("ENRICHED: raw-data");

            ProducerTemplate producer = ctx.createProducerTemplate();
            producer.sendBody("direct:tC__transform-1", "raw-data");

            mock.assertIsSatisfied();
        }
    }
}
