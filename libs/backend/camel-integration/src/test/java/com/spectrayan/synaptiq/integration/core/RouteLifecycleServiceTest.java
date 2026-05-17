package com.spectrayan.synaptiq.integration.core;

import com.spectrayan.synaptiq.integration.autoconfigure.CamelIntegrationProperties;
import com.spectrayan.synaptiq.integration.model.RouteConfig;
import com.spectrayan.synaptiq.integration.model.RouteStatus;
import com.spectrayan.synaptiq.integration.stub.InMemoryCredentialProvider;
import com.spectrayan.synaptiq.integration.stub.InMemoryExecutionLogger;
import com.spectrayan.synaptiq.integration.stub.InMemoryRouteConfigProvider;
import com.spectrayan.synaptiq.integration.stub.InMemoryTemplateConfigProvider;
import com.spectrayan.synaptiq.integration.template.IntegrationRouteTemplates;
import com.spectrayan.synaptiq.integration.template.TemplateRegistry;
import com.spectrayan.synaptiq.integration.tenant.TenantRateLimitPolicy;
import org.junit.jupiter.api.*;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link RouteLifecycleService} — tests the full
 * lifecycle flow: activate → route appears in CamelContext → deactivate.
 * Uses in-memory SPI stubs (no database, no external services).
 */
@DisplayName("RouteLifecycleService (Integration)")
class RouteLifecycleServiceTest {

    private CamelEngineManager engineManager;
    private RouteAdapterRegistry adapterRegistry;
    private InMemoryRouteConfigProvider configProvider;
    private InMemoryCredentialProvider credentialProvider;
    private InMemoryExecutionLogger executionLogger;
    private TemplateRegistry templateRegistry;
    private TenantRateLimitPolicy rateLimitPolicy;
    private RouteLifecycleService lifecycleService;

    @BeforeEach
    void setUp() {
        CamelIntegrationProperties properties = new CamelIntegrationProperties();
        properties.getTenant().setMaxRoutesPerTenant(10);

        engineManager = new CamelEngineManager(
                properties,
                List.of(new IntegrationRouteTemplates()),
                Optional.empty()
        );
        engineManager.start();

        adapterRegistry = new RouteAdapterRegistry(List.of());
        configProvider = new InMemoryRouteConfigProvider();
        credentialProvider = new InMemoryCredentialProvider();
        executionLogger = new InMemoryExecutionLogger();
        templateRegistry = new TemplateRegistry(new InMemoryTemplateConfigProvider());
        rateLimitPolicy = new TenantRateLimitPolicy(engineManager, properties);

        lifecycleService = new RouteLifecycleService(
                engineManager, adapterRegistry, configProvider,
                credentialProvider, executionLogger, templateRegistry,
                rateLimitPolicy
        );
    }

    @AfterEach
    void tearDown() {
        if (engineManager.isRunning()) {
            engineManager.stop();
        }
    }

    @Nested
    @DisplayName("activateRoute()")
    class ActivateRoute {

        @Test
        @DisplayName("should activate a template-based route and update status to ACTIVE")
        void activatesTemplateRoute() {
            RouteConfig config = RouteConfig.builder()
                    .routeConfigId("rc-1")
                    .tenantId("tenant-A")
                    .name("REST Poll")
                    .connectorType("REST_API")
                    .templateId("rest-api-poll")
                    .parameters(Map.of(
                            "url", "mock:lifecycle-rest-output",
                            "pollIntervalMs", "999999999"
                    ))
                    .status(RouteStatus.PENDING)
                    .build();
            configProvider.save(config).block();

            StepVerifier.create(lifecycleService.activateRoute("rc-1"))
                    .assertNext(activated -> {
                        assertThat(activated.getStatus()).isEqualTo(RouteStatus.ACTIVE);
                    })
                    .verifyComplete();

            // Route should now be in CamelContext
            String expectedRouteId = TenantRouteNamingStrategy.buildRouteId("tenant-A", "rc-1");
            assertThat(engineManager.isRouteActive(expectedRouteId)).isTrue();
        }

        @Test
        @DisplayName("should activate a YAML-based custom route")
        void activatesYamlRoute() {
            String yaml = """
                    - route:
                        id: "tenant-B__rc-yaml"
                        from:
                          uri: "direct:tenant-B__rc-yaml"
                          steps:
                            - log:
                                message: "YAML route active"
                    """;

            RouteConfig config = RouteConfig.builder()
                    .routeConfigId("rc-yaml")
                    .tenantId("tenant-B")
                    .name("Custom YAML Route")
                    .connectorType("CUSTOM_YAML")
                    .routeYaml(yaml)
                    .status(RouteStatus.PENDING)
                    .build();
            configProvider.save(config).block();

            StepVerifier.create(lifecycleService.activateRoute("rc-yaml"))
                    .assertNext(activated -> {
                        assertThat(activated.getStatus()).isEqualTo(RouteStatus.ACTIVE);
                    })
                    .verifyComplete();

            assertThat(engineManager.isRouteActive("tenant-B__rc-yaml")).isTrue();
        }

        @Test
        @DisplayName("should resolve credentials during activation")
        void resolvesCredentials() {
            credentialProvider.addSecret("cred/rest-api-key", "Bearer abc123");

            RouteConfig config = RouteConfig.builder()
                    .routeConfigId("rc-cred")
                    .tenantId("tenant-C")
                    .name("REST with Creds")
                    .connectorType("REST_API")
                    .templateId("rest-api-poll")
                    .parameters(Map.of(
                            "url", "mock:cred-rest-output",
                            "pollIntervalMs", "999999999"
                    ))
                    .credentialRef("cred/rest-api-key")
                    .status(RouteStatus.PENDING)
                    .build();
            configProvider.save(config).block();

            StepVerifier.create(lifecycleService.activateRoute("rc-cred"))
                    .assertNext(activated -> {
                        assertThat(activated.getStatus()).isEqualTo(RouteStatus.ACTIVE);
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("should set status to ERROR when activation fails")
        void setsErrorOnFailure() {
            RouteConfig config = RouteConfig.builder()
                    .routeConfigId("rc-fail")
                    .tenantId("tenant-D")
                    .name("Failing Route")
                    .connectorType("UNKNOWN_TYPE")
                    // No templateId, no routeYaml, no adapter — should fail
                    .status(RouteStatus.PENDING)
                    .build();
            configProvider.save(config).block();

            StepVerifier.create(lifecycleService.activateRoute("rc-fail"))
                    .assertNext(result -> {
                        assertThat(result.getStatus()).isEqualTo(RouteStatus.ERROR);
                        assertThat(result.getLastError()).isNotBlank();
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("should reject activation when tenant exceeds rate limit")
        void rejectsAtRateLimit() {
            CamelIntegrationProperties limitedProps = new CamelIntegrationProperties();
            limitedProps.getTenant().setMaxRoutesPerTenant(0); // Zero max = always deny
            TenantRateLimitPolicy zeroPolicy = new TenantRateLimitPolicy(engineManager, limitedProps);
            RouteLifecycleService limitedService = new RouteLifecycleService(
                    engineManager, adapterRegistry, configProvider,
                    credentialProvider, executionLogger, templateRegistry,
                    zeroPolicy
            );

            RouteConfig config = RouteConfig.builder()
                    .routeConfigId("rc-limited")
                    .tenantId("tenant-E")
                    .name("Rate Limited Route")
                    .connectorType("REST_API")
                    .templateId("rest-api-poll")
                    .parameters(Map.of("url", "mock:dummy"))
                    .status(RouteStatus.PENDING)
                    .build();
            configProvider.save(config).block();

            StepVerifier.create(limitedService.activateRoute("rc-limited"))
                    .expectError(IllegalStateException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("deactivateRoute()")
    class DeactivateRoute {

        @Test
        @DisplayName("should deactivate an active route and set status to DISABLED")
        void deactivatesActiveRoute() {
            // First activate
            RouteConfig config = RouteConfig.builder()
                    .routeConfigId("rc-deactivate")
                    .tenantId("tenant-F")
                    .name("Deactivatable Route")
                    .connectorType("REST_API")
                    .templateId("rest-api-poll")
                    .parameters(Map.of(
                            "url", "mock:deactivate-output",
                            "pollIntervalMs", "999999999"
                    ))
                    .status(RouteStatus.PENDING)
                    .build();
            configProvider.save(config).block();

            lifecycleService.activateRoute("rc-deactivate").block();
            String camelRouteId = TenantRouteNamingStrategy.buildRouteId("tenant-F", "rc-deactivate");
            assertThat(engineManager.isRouteActive(camelRouteId)).isTrue();

            // Now deactivate
            StepVerifier.create(lifecycleService.deactivateRoute("rc-deactivate"))
                    .assertNext(deactivated -> {
                        assertThat(deactivated.getStatus()).isEqualTo(RouteStatus.DISABLED);
                    })
                    .verifyComplete();

            assertThat(engineManager.isRouteActive(camelRouteId)).isFalse();
        }
    }

    @Nested
    @DisplayName("reloadRoute()")
    class ReloadRoute {

        @Test
        @DisplayName("should reload a route (deactivate + reactivate)")
        void reloadsRoute() {
            RouteConfig config = RouteConfig.builder()
                    .routeConfigId("rc-reload")
                    .tenantId("tenant-G")
                    .name("Reloadable Route")
                    .connectorType("REST_API")
                    .templateId("rest-api-poll")
                    .parameters(Map.of(
                            "url", "mock:reload-output",
                            "pollIntervalMs", "999999999"
                    ))
                    .status(RouteStatus.PENDING)
                    .build();
            configProvider.save(config).block();

            lifecycleService.activateRoute("rc-reload").block();

            StepVerifier.create(lifecycleService.reloadRoute("rc-reload"))
                    .assertNext(reloaded -> {
                        assertThat(reloaded.getStatus()).isEqualTo(RouteStatus.ACTIVE);
                    })
                    .verifyComplete();

            String camelRouteId = TenantRouteNamingStrategy.buildRouteId("tenant-G", "rc-reload");
            assertThat(engineManager.isRouteActive(camelRouteId)).isTrue();
        }
    }

    @Nested
    @DisplayName("testConnection()")
    class TestConnection {

        @Test
        @DisplayName("should return generic success when no adapter is registered")
        void genericSuccessWithoutAdapter() {
            RouteConfig config = RouteConfig.builder()
                    .routeConfigId("rc-test-conn")
                    .tenantId("tenant-H")
                    .name("No Adapter Route")
                    .connectorType("UNKNOWN_TYPE")
                    .templateId("some-template")
                    .status(RouteStatus.PENDING)
                    .build();
            configProvider.save(config).block();

            StepVerifier.create(lifecycleService.testConnection("rc-test-conn"))
                    .assertNext(result -> {
                        assertThat(result.isSuccess()).isTrue();
                        assertThat(result.getMessage()).contains("No specialized test adapter");
                    })
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("removeAllRoutesForTenant()")
    class RemoveAllRoutes {

        @Test
        @DisplayName("should remove all routes for a tenant from CamelContext")
        void removesAllRoutes() throws Exception {
            // Load routes directly into engine
            for (int i = 1; i <= 3; i++) {
                String yaml = String.format("""
                        - route:
                            id: "tenant-I__route-%d"
                            from:
                              uri: "direct:tenant-I-r%d"
                              steps:
                                - log:
                                    message: "test"
                        """, i, i);
                engineManager.loadRouteFromYaml("tenant-I__route-" + i, yaml);
            }
            assertThat(engineManager.getRouteIdsForTenant("tenant-I")).hasSize(3);

            StepVerifier.create(lifecycleService.removeAllRoutesForTenant("tenant-I"))
                    .verifyComplete();

            assertThat(engineManager.getRouteIdsForTenant("tenant-I")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Auto-resolve templateId from connectorType")
    class AutoResolveTemplate {

        @Test
        @DisplayName("should auto-resolve templateId when not explicitly set")
        void autoResolvesTemplateFromType() {
            RouteConfig config = RouteConfig.builder()
                    .routeConfigId("rc-auto")
                    .tenantId("tenant-J")
                    .name("Auto-resolve Route")
                    .connectorType("REST_API")
                    // templateId is null — should auto-resolve to "rest-api-poll"
                    .parameters(Map.of(
                            "url", "mock:auto-resolve-output",
                            "pollIntervalMs", "999999999"
                    ))
                    .status(RouteStatus.PENDING)
                    .build();
            configProvider.save(config).block();

            StepVerifier.create(lifecycleService.activateRoute("rc-auto"))
                    .assertNext(activated -> {
                        assertThat(activated.getStatus()).isEqualTo(RouteStatus.ACTIVE);
                        assertThat(activated.getTemplateId()).isEqualTo("rest-api-poll");
                    })
                    .verifyComplete();
        }
    }
}
