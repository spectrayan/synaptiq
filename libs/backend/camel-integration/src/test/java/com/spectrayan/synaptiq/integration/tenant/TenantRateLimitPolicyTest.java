package com.spectrayan.synaptiq.integration.tenant;

import com.spectrayan.synaptiq.integration.autoconfigure.CamelIntegrationProperties;
import com.spectrayan.synaptiq.integration.core.CamelEngineManager;
import com.spectrayan.synaptiq.integration.template.IntegrationRouteTemplates;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TenantRateLimitPolicy}.
 */
@DisplayName("TenantRateLimitPolicy")
class TenantRateLimitPolicyTest {

    private CamelEngineManager engineManager;
    private TenantRateLimitPolicy policy;
    private CamelIntegrationProperties properties;

    @BeforeEach
    void setUp() {
        properties = new CamelIntegrationProperties();
        properties.getTenant().setMaxRoutesPerTenant(2); // Low limit for testing
        engineManager = new CamelEngineManager(
                properties,
                List.of(new IntegrationRouteTemplates()),
                Optional.empty()
        );
        engineManager.start();
        policy = new TenantRateLimitPolicy(engineManager, properties);
    }

    @AfterEach
    void tearDown() {
        if (engineManager.isRunning()) {
            engineManager.stop();
        }
    }

    @Test
    @DisplayName("should allow route when tenant has no routes")
    void allowsWhenEmpty() {
        assertThat(policy.canAddRoute("tenant-1")).isTrue();
        assertThat(policy.getActiveRouteCount("tenant-1")).isZero();
    }

    @Test
    @DisplayName("should allow routes up to the limit")
    void allowsUpToLimit() throws Exception {
        // Add one route for tenant-1
        String yaml = """
                - route:
                    id: "tenant-1__route-1"
                    from:
                      uri: "direct:t1-r1"
                      steps:
                        - log:
                            message: "test"
                """;
        engineManager.loadRouteFromYaml("tenant-1__route-1", yaml);

        assertThat(policy.canAddRoute("tenant-1")).isTrue();
        assertThat(policy.getActiveRouteCount("tenant-1")).isEqualTo(1);
    }

    @Test
    @DisplayName("should deny route when tenant reaches max limit")
    void deniesAtLimit() throws Exception {
        // Add two routes (max=2)
        for (int i = 1; i <= 2; i++) {
            String yaml = String.format("""
                    - route:
                        id: "tenant-1__route-%d"
                        from:
                          uri: "direct:t1-r%d"
                          steps:
                            - log:
                                message: "test %d"
                    """, i, i, i);
            engineManager.loadRouteFromYaml("tenant-1__route-" + i, yaml);
        }

        assertThat(policy.canAddRoute("tenant-1")).isFalse();
        assertThat(policy.getActiveRouteCount("tenant-1")).isEqualTo(2);
    }

    @Test
    @DisplayName("different tenants should have independent limits")
    void independentTenantLimits() throws Exception {
        // Fill tenant-1 to max
        for (int i = 1; i <= 2; i++) {
            String yaml = String.format("""
                    - route:
                        id: "tenant-1__route-%d"
                        from:
                          uri: "direct:t1-r%d"
                          steps:
                            - log:
                                message: "test"
                    """, i, i);
            engineManager.loadRouteFromYaml("tenant-1__route-" + i, yaml);
        }

        // tenant-2 should still be allowed
        assertThat(policy.canAddRoute("tenant-1")).isFalse();
        assertThat(policy.canAddRoute("tenant-2")).isTrue();
    }
}
