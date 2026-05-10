package com.spectrayan.synaptiq.integration.autoconfigure;

import com.spectrayan.synaptiq.integration.adapter.RouteAdapter;
import com.spectrayan.synaptiq.integration.core.CamelEngineManager;
import com.spectrayan.synaptiq.integration.core.ExecutionEventNotifier;
import com.spectrayan.synaptiq.integration.core.RouteAdapterRegistry;
import com.spectrayan.synaptiq.integration.core.RouteLifecycleService;
import com.spectrayan.synaptiq.integration.core.StartupRouteLoader;
import com.spectrayan.synaptiq.integration.health.CamelHealthIndicator;
import com.spectrayan.synaptiq.integration.health.CamelMetricsReporter;
import com.spectrayan.synaptiq.integration.spi.CredentialProvider;
import com.spectrayan.synaptiq.integration.spi.ExecutionLogger;
import com.spectrayan.synaptiq.integration.spi.RouteConfigProvider;
import com.spectrayan.synaptiq.integration.spi.TemplateConfigProvider;
import com.spectrayan.synaptiq.integration.template.TemplateRegistry;
import com.spectrayan.synaptiq.integration.tenant.TenantIsolationInterceptor;
import com.spectrayan.synaptiq.integration.tenant.TenantRateLimitPolicy;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;
import java.util.Optional;

/**
 * Spring Boot auto-configuration for the Camel integration engine.
 * <p>
 * Drop {@code camel-integration} as a dependency and this configuration
 * automatically wires up the CamelContext, adapters, templates, health,
 * and metrics. The consuming application provides SPI implementations
 * ({@link RouteConfigProvider}, {@link CredentialProvider}, {@link ExecutionLogger}).
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(CamelContext.class)
@ConditionalOnProperty(prefix = "synaptiq.integration", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(CamelIntegrationProperties.class)
@ComponentScan(basePackages = "com.spectrayan.synaptiq.integration")
public class CamelIntegrationAutoConfiguration {

    // ═══════════════════════════════════════════════════════════════
    //  Core Engine
    // ═══════════════════════════════════════════════════════════════

    @Bean
    @ConditionalOnMissingBean
    public TenantIsolationInterceptor tenantIsolationInterceptor() {
        log.info("Creating TenantIsolationInterceptor");
        return new TenantIsolationInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public CamelEngineManager camelEngineManager(
            CamelIntegrationProperties properties,
            List<RouteBuilder> routeBuilders,
            Optional<TenantIsolationInterceptor> interceptor) {
        log.info("Creating CamelEngineManager with {} route builders", routeBuilders.size());
        return new CamelEngineManager(properties, routeBuilders, interceptor);
    }

    @Bean
    @ConditionalOnMissingBean
    public TenantRateLimitPolicy tenantRateLimitPolicy(
            CamelEngineManager engineManager,
            CamelIntegrationProperties properties) {
        return new TenantRateLimitPolicy(engineManager, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public TemplateRegistry templateRegistry(
            Optional<TemplateConfigProvider> templateConfigProvider) {
        log.info("Creating TemplateRegistry (DB provider: {})",
                templateConfigProvider.isPresent() ? "available" : "none");
        return new TemplateRegistry(templateConfigProvider.orElse(null));
    }

    // ═══════════════════════════════════════════════════════════════
    //  Route Lifecycle Service (requires SPI beans from consumer)
    // ═══════════════════════════════════════════════════════════════

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({RouteConfigProvider.class, CredentialProvider.class, ExecutionLogger.class})
    public RouteLifecycleService routeLifecycleService(
            CamelEngineManager engineManager,
            RouteAdapterRegistry adapterRegistry,
            RouteConfigProvider configProvider,
            CredentialProvider credentialProvider,
            ExecutionLogger executionLogger,
            TemplateRegistry templateRegistry,
            TenantRateLimitPolicy rateLimitPolicy) {
        log.info("Creating RouteLifecycleService (SPI beans detected)");
        return new RouteLifecycleService(
                engineManager, adapterRegistry, configProvider,
                credentialProvider, executionLogger, templateRegistry,
                rateLimitPolicy);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Startup Loader — auto-loads ACTIVE routes on boot
    // ═══════════════════════════════════════════════════════════════

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RouteLifecycleService.class)
    public StartupRouteLoader startupRouteLoader(
            RouteConfigProvider configProvider,
            RouteLifecycleService lifecycleService) {
        log.info("Creating StartupRouteLoader");
        return new StartupRouteLoader(configProvider, lifecycleService);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Execution Event Notifier — logs exchange completions via SPI
    // ═══════════════════════════════════════════════════════════════

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ExecutionLogger.class, RouteConfigProvider.class})
    public ExecutionEventNotifier executionEventNotifier(
            CamelEngineManager engineManager,
            ExecutionLogger executionLogger,
            RouteConfigProvider routeConfigProvider) {
        ExecutionEventNotifier notifier = new ExecutionEventNotifier(
                executionLogger, routeConfigProvider);
        // Register with the CamelContext so it receives exchange events
        engineManager.getCamelContext().getManagementStrategy()
                .addEventNotifier(notifier);
        log.info("Registered ExecutionEventNotifier for exchange logging");
        return notifier;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Health + Metrics
    // ═══════════════════════════════════════════════════════════════

    @Bean
    @ConditionalOnProperty(prefix = "synaptiq.integration.health", name = "enabled", matchIfMissing = true)
    public CamelHealthIndicator camelHealthIndicator(CamelEngineManager engineManager) {
        log.info("Registering Camel integration health indicator");
        return new CamelHealthIndicator(engineManager);
    }

    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    public CamelMetricsReporter camelMetricsReporter(
            CamelEngineManager engineManager,
            MeterRegistry meterRegistry) {
        log.info("Registering Camel integration metrics");
        return new CamelMetricsReporter(engineManager, meterRegistry);
    }
}
