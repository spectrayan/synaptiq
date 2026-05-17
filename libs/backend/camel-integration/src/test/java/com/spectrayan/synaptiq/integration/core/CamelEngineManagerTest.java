package com.spectrayan.synaptiq.integration.core;

import com.spectrayan.synaptiq.integration.autoconfigure.CamelIntegrationProperties;
import com.spectrayan.synaptiq.integration.template.IntegrationRouteTemplates;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CamelEngineManager} — validates Camel engine lifecycle,
 * dynamic route loading from YAML, and route management.
 */
@DisplayName("CamelEngineManager")
class CamelEngineManagerTest {

    private CamelEngineManager engineManager;

    @BeforeEach
    void setUp() throws Exception {
        CamelIntegrationProperties properties = new CamelIntegrationProperties();
        // Create engine with the built-in template definitions
        engineManager = new CamelEngineManager(
                properties,
                List.of(new IntegrationRouteTemplates()),
                Optional.empty()
        );
        engineManager.start();
    }

    @AfterEach
    void tearDown() {
        if (engineManager != null && engineManager.isRunning()) {
            engineManager.stop();
        }
    }

    @Nested
    @DisplayName("Lifecycle")
    class Lifecycle {

        @Test
        @DisplayName("should start and report running")
        void startsAndReportsRunning() {
            assertThat(engineManager.isRunning()).isTrue();
            assertThat(engineManager.getCamelContext()).isNotNull();
            assertThat(engineManager.getCamelContext().isStarted()).isTrue();
        }

        @Test
        @DisplayName("should stop cleanly")
        void stopsCleanly() {
            engineManager.stop();
            assertThat(engineManager.isRunning()).isFalse();
        }

        @Test
        @DisplayName("should be idempotent on double start")
        void idempotentStart() {
            engineManager.start(); // second call
            assertThat(engineManager.isRunning()).isTrue();
        }

        @Test
        @DisplayName("should be idempotent on double stop")
        void idempotentStop() {
            engineManager.stop();
            engineManager.stop(); // second call
            assertThat(engineManager.isRunning()).isFalse();
        }
    }

    @Nested
    @DisplayName("Dynamic YAML route loading")
    class YamlRouteLoading {

        @Test
        @DisplayName("should load a route from YAML DSL")
        void loadsRouteFromYaml() throws Exception {
            String yaml = """
                    - route:
                        id: "yaml-test-route"
                        from:
                          uri: "direct:yaml-input"
                          steps:
                            - to:
                                uri: "mock:yaml-output"
                    """;

            engineManager.loadRouteFromYaml("yaml-test-route", yaml);

            assertThat(engineManager.isRouteActive("yaml-test-route")).isTrue();
            assertThat(engineManager.getActiveRouteIds()).contains("yaml-test-route");
        }

        @Test
        @DisplayName("should remove a loaded route")
        void removesRoute() throws Exception {
            String yaml = """
                    - route:
                        id: "removable-route"
                        from:
                          uri: "direct:remove-me"
                          steps:
                            - log:
                                message: "will be removed"
                    """;

            engineManager.loadRouteFromYaml("removable-route", yaml);
            assertThat(engineManager.isRouteActive("removable-route")).isTrue();

            engineManager.removeRoute("removable-route");
            assertThat(engineManager.isRouteActive("removable-route")).isFalse();
        }

        @Test
        @DisplayName("removeRoute should be no-op for non-existent route")
        void removeNonExistentIsNoOp() throws Exception {
            engineManager.removeRoute("does-not-exist");
            // No exception thrown
        }

        @Test
        @DisplayName("should load a YAML route with timer component")
        void loadsTimerRoute() throws Exception {
            String yaml = """
                    - route:
                        id: "timer-yaml-route"
                        from:
                          uri: "timer:yaml-timer?period=60000&repeatCount=0"
                          steps:
                            - log:
                                message: "Timer fired from YAML"
                    """;

            engineManager.loadRouteFromYaml("timer-yaml-route", yaml);
            assertThat(engineManager.isRouteActive("timer-yaml-route")).isTrue();
        }

        @Test
        @DisplayName("should load a YAML route with content-based routing")
        void loadsContentBasedRoutingYaml() throws Exception {
            String yaml = """
                    - route:
                        id: "cbr-yaml-route"
                        from:
                          uri: "direct:cbr-input"
                          steps:
                            - choice:
                                when:
                                  - simple: "${header.type} == 'A'"
                                    steps:
                                      - to:
                                          uri: "mock:type-a"
                                otherwise:
                                  steps:
                                    - to:
                                        uri: "mock:type-other"
                    """;

            engineManager.loadRouteFromYaml("cbr-yaml-route", yaml);
            assertThat(engineManager.isRouteActive("cbr-yaml-route")).isTrue();
        }
    }

    @Nested
    @DisplayName("Tenant route filtering")
    class TenantRouteFiltering {

        @Test
        @DisplayName("should filter routes by tenant prefix")
        void filtersRoutesByTenant() throws Exception {
            // Load routes for two tenants
            String yaml1 = """
                    - route:
                        id: "tenantA__route1"
                        from:
                          uri: "direct:tenantA-route1"
                          steps:
                            - log:
                                message: "tenantA route1"
                    """;
            String yaml2 = """
                    - route:
                        id: "tenantA__route2"
                        from:
                          uri: "direct:tenantA-route2"
                          steps:
                            - log:
                                message: "tenantA route2"
                    """;
            String yaml3 = """
                    - route:
                        id: "tenantB__route1"
                        from:
                          uri: "direct:tenantB-route1"
                          steps:
                            - log:
                                message: "tenantB route1"
                    """;

            engineManager.loadRouteFromYaml("tenantA__route1", yaml1);
            engineManager.loadRouteFromYaml("tenantA__route2", yaml2);
            engineManager.loadRouteFromYaml("tenantB__route1", yaml3);

            List<String> tenantARoutes = engineManager.getRouteIdsForTenant("tenantA");
            List<String> tenantBRoutes = engineManager.getRouteIdsForTenant("tenantB");

            assertThat(tenantARoutes).containsExactlyInAnyOrder("tenantA__route1", "tenantA__route2");
            assertThat(tenantBRoutes).containsExactly("tenantB__route1");
        }

        @Test
        @DisplayName("should return empty list for tenant with no routes")
        void returnsEmptyForUnknownTenant() {
            assertThat(engineManager.getRouteIdsForTenant("nonexistent")).isEmpty();
        }
    }
}
