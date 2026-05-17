package com.spectrayan.synaptiq.integration.health;

import com.spectrayan.synaptiq.integration.autoconfigure.CamelIntegrationProperties;
import com.spectrayan.synaptiq.integration.core.CamelEngineManager;
import com.spectrayan.synaptiq.integration.template.IntegrationRouteTemplates;
import org.apache.camel.builder.RouteBuilder;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CamelHealthIndicator}.
 */
@DisplayName("CamelHealthIndicator")
class CamelHealthIndicatorTest {

    private CamelEngineManager engineManager;
    private CamelHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        CamelIntegrationProperties props = new CamelIntegrationProperties();
        engineManager = new CamelEngineManager(
                props,
                List.of(new IntegrationRouteTemplates()),
                Optional.empty()
        );
        healthIndicator = new CamelHealthIndicator(engineManager);
    }

    @AfterEach
    void tearDown() {
        if (engineManager.isRunning()) {
            engineManager.stop();
        }
    }

    @Test
    @DisplayName("should report UP when engine is started")
    void reportsUpWhenStarted() {
        engineManager.start();

        Map<String, Object> health = healthIndicator.health();

        assertThat(health.get("status")).isEqualTo("UP");
        assertThat(health.get("camelStatus")).isEqualTo("Started");
        assertThat(health.get("contextName")).isNotNull();
        assertThat(health.get("activeRoutes")).isNotNull();
        assertThat(health.get("uptime")).isNotNull();
        assertThat(healthIndicator.isHealthy()).isTrue();
    }

    @Test
    @DisplayName("should report DOWN when engine is stopped")
    void reportsDownWhenStopped() {
        // Context is created but not started
        Map<String, Object> health = healthIndicator.health();

        assertThat(health.get("status")).isEqualTo("DOWN");
        assertThat(healthIndicator.isHealthy()).isFalse();
    }

    @Test
    @DisplayName("should report route count in health details")
    void reportsRouteCount() throws Exception {
        engineManager.start();

        // Load a test route
        String yaml = """
                - route:
                    id: "health-test-route"
                    from:
                      uri: "direct:health-test"
                      steps:
                        - log:
                            message: "health test"
                """;
        engineManager.loadRouteFromYaml("health-test-route", yaml);

        Map<String, Object> health = healthIndicator.health();
        int routeCount = (int) health.get("activeRoutes");
        assertThat(routeCount).isGreaterThanOrEqualTo(1);
    }
}
