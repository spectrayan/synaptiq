package com.spectrayan.synaptiq.integration.core;

import com.spectrayan.synaptiq.integration.autoconfigure.CamelIntegrationProperties;
import com.spectrayan.synaptiq.integration.template.IntegrationRouteTemplates;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that verify YAML DSL routes are loaded correctly into a real
 * CamelContext and can process messages end-to-end.
 */
@DisplayName("YAML DSL Route Integration")
class YamlDslRouteTest {

    private CamelEngineManager engineManager;

    @BeforeEach
    void setUp() {
        CamelIntegrationProperties props = new CamelIntegrationProperties();
        engineManager = new CamelEngineManager(
                props,
                List.of(new IntegrationRouteTemplates()),
                Optional.empty()
        );
        engineManager.start();
    }

    @AfterEach
    void tearDown() {
        if (engineManager.isRunning()) {
            engineManager.stop();
        }
    }

    @Test
    @DisplayName("should route messages through a simple YAML route")
    void simpleYamlRoute() throws Exception {
        String yaml = """
                - route:
                    id: "simple-yaml"
                    from:
                      uri: "direct:yaml-in"
                      steps:
                        - to:
                            uri: "mock:yaml-out"
                """;

        engineManager.loadRouteFromYaml("simple-yaml", yaml);

        CamelContext ctx = engineManager.getCamelContext();
        MockEndpoint mock = ctx.getEndpoint("mock:yaml-out", MockEndpoint.class);
        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived("Hello YAML");

        ProducerTemplate producer = ctx.createProducerTemplate();
        producer.sendBody("direct:yaml-in", "Hello YAML");

        mock.assertIsSatisfied();
    }

    @Test
    @DisplayName("should route messages through a YAML route with content-based routing")
    void contentBasedRouting() throws Exception {
        String yaml = """
                - route:
                    id: "cbr-yaml"
                    from:
                      uri: "direct:cbr-in"
                      steps:
                        - choice:
                            when:
                              - simple: "${header.priority} == 'HIGH'"
                                steps:
                                  - to:
                                      uri: "mock:high-priority"
                            otherwise:
                              steps:
                                - to:
                                    uri: "mock:normal-priority"
                """;

        engineManager.loadRouteFromYaml("cbr-yaml", yaml);

        CamelContext ctx = engineManager.getCamelContext();
        MockEndpoint highMock = ctx.getEndpoint("mock:high-priority", MockEndpoint.class);
        MockEndpoint normalMock = ctx.getEndpoint("mock:normal-priority", MockEndpoint.class);

        highMock.expectedMessageCount(1);
        normalMock.expectedMessageCount(1);

        ProducerTemplate producer = ctx.createProducerTemplate();
        producer.sendBodyAndHeader("direct:cbr-in", "Urgent!", "priority", "HIGH");
        producer.sendBodyAndHeader("direct:cbr-in", "Normal", "priority", "LOW");

        highMock.assertIsSatisfied();
        normalMock.assertIsSatisfied();
    }

    @Test
    @DisplayName("should route messages through a YAML route with transform")
    void transformRoute() throws Exception {
        String yaml = """
                - route:
                    id: "transform-yaml"
                    from:
                      uri: "direct:transform-in"
                      steps:
                        - setBody:
                            simple: "Transformed: ${body}"
                        - to:
                            uri: "mock:transform-out"
                """;

        engineManager.loadRouteFromYaml("transform-yaml", yaml);

        CamelContext ctx = engineManager.getCamelContext();
        MockEndpoint mock = ctx.getEndpoint("mock:transform-out", MockEndpoint.class);
        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived("Transformed: original");

        ProducerTemplate producer = ctx.createProducerTemplate();
        producer.sendBody("direct:transform-in", "original");

        mock.assertIsSatisfied();
    }

    @Test
    @DisplayName("should route messages through a YAML route with filter")
    void filterRoute() throws Exception {
        String yaml = """
                - route:
                    id: "filter-yaml"
                    from:
                      uri: "direct:filter-in"
                      steps:
                        - filter:
                            simple: "${header.accept} == 'true'"
                            steps:
                              - to:
                                  uri: "mock:accepted"
                """;

        engineManager.loadRouteFromYaml("filter-yaml", yaml);

        CamelContext ctx = engineManager.getCamelContext();
        MockEndpoint acceptedMock = ctx.getEndpoint("mock:accepted", MockEndpoint.class);
        acceptedMock.expectedMessageCount(1);

        ProducerTemplate producer = ctx.createProducerTemplate();
        producer.sendBodyAndHeader("direct:filter-in", "Pass", "accept", "true");
        producer.sendBodyAndHeader("direct:filter-in", "Reject", "accept", "false");

        acceptedMock.assertIsSatisfied();
    }

    @Test
    @DisplayName("should support multicast pattern in YAML")
    void multicastRoute() throws Exception {
        String yaml = """
                - route:
                    id: "multicast-yaml"
                    from:
                      uri: "direct:multicast-in"
                      steps:
                        - multicast:
                            steps:
                              - to:
                                  uri: "mock:branch-a"
                              - to:
                                  uri: "mock:branch-b"
                """;

        engineManager.loadRouteFromYaml("multicast-yaml", yaml);

        CamelContext ctx = engineManager.getCamelContext();
        MockEndpoint branchA = ctx.getEndpoint("mock:branch-a", MockEndpoint.class);
        MockEndpoint branchB = ctx.getEndpoint("mock:branch-b", MockEndpoint.class);

        branchA.expectedMessageCount(1);
        branchA.expectedBodiesReceived("Broadcast");
        branchB.expectedMessageCount(1);
        branchB.expectedBodiesReceived("Broadcast");

        ProducerTemplate producer = ctx.createProducerTemplate();
        producer.sendBody("direct:multicast-in", "Broadcast");

        branchA.assertIsSatisfied();
        branchB.assertIsSatisfied();
    }

    @Test
    @DisplayName("should load multiple YAML routes and isolate them")
    void multipleRouteIsolation() throws Exception {
        String yaml1 = """
                - route:
                    id: "iso-route-1"
                    from:
                      uri: "direct:iso-1"
                      steps:
                        - to:
                            uri: "mock:iso-out-1"
                """;
        String yaml2 = """
                - route:
                    id: "iso-route-2"
                    from:
                      uri: "direct:iso-2"
                      steps:
                        - to:
                            uri: "mock:iso-out-2"
                """;

        engineManager.loadRouteFromYaml("iso-route-1", yaml1);
        engineManager.loadRouteFromYaml("iso-route-2", yaml2);

        CamelContext ctx = engineManager.getCamelContext();
        MockEndpoint mock1 = ctx.getEndpoint("mock:iso-out-1", MockEndpoint.class);
        MockEndpoint mock2 = ctx.getEndpoint("mock:iso-out-2", MockEndpoint.class);

        mock1.expectedMessageCount(1);
        mock1.expectedBodiesReceived("Message for route 1");
        mock2.expectedMessageCount(1);
        mock2.expectedBodiesReceived("Message for route 2");

        ProducerTemplate producer = ctx.createProducerTemplate();
        producer.sendBody("direct:iso-1", "Message for route 1");
        producer.sendBody("direct:iso-2", "Message for route 2");

        mock1.assertIsSatisfied();
        mock2.assertIsSatisfied();
    }

    @Test
    @DisplayName("should load YAML route with header manipulation")
    void headerManipulationRoute() throws Exception {
        String yaml = """
                - route:
                    id: "header-yaml"
                    from:
                      uri: "direct:header-in"
                      steps:
                        - setHeader:
                            name: "enriched"
                            simple: "enriched-${body}"
                        - to:
                            uri: "mock:header-out"
                """;

        engineManager.loadRouteFromYaml("header-yaml", yaml);

        CamelContext ctx = engineManager.getCamelContext();
        MockEndpoint mock = ctx.getEndpoint("mock:header-out", MockEndpoint.class);
        mock.expectedMessageCount(1);
        mock.expectedHeaderReceived("enriched", "enriched-payload");

        ProducerTemplate producer = ctx.createProducerTemplate();
        producer.sendBody("direct:header-in", "payload");

        mock.assertIsSatisfied();
    }

    @Test
    @DisplayName("should handle route removal and re-addition")
    void routeRemovalAndReAdd() throws Exception {
        String yaml = """
                - route:
                    id: "temp-route"
                    from:
                      uri: "direct:temp"
                      steps:
                        - to:
                            uri: "mock:temp-out"
                """;

        // Load
        engineManager.loadRouteFromYaml("temp-route", yaml);
        assertThat(engineManager.isRouteActive("temp-route")).isTrue();

        // Remove
        engineManager.removeRoute("temp-route");
        assertThat(engineManager.isRouteActive("temp-route")).isFalse();

        // Re-add
        engineManager.loadRouteFromYaml("temp-route", yaml);
        assertThat(engineManager.isRouteActive("temp-route")).isTrue();

        // Verify it still works
        CamelContext ctx = engineManager.getCamelContext();
        MockEndpoint mock = ctx.getEndpoint("mock:temp-out", MockEndpoint.class);
        mock.expectedMessageCount(1);

        ProducerTemplate producer = ctx.createProducerTemplate();
        producer.sendBody("direct:temp", "Re-added");

        mock.assertIsSatisfied();
    }
}
