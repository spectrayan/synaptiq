package com.spectrayan.synaptiq.integration.adapter.database;

import com.spectrayan.synaptiq.integration.adapter.RouteAdapter;
import com.spectrayan.synaptiq.integration.model.ConnectionTestResult;
import com.spectrayan.synaptiq.integration.model.RouteConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Validates and tests configuration for database query integrations.
 * <p>
 * Route construction is handled by the {@code db-query} Camel RouteTemplate
 * defined in {@link com.spectrayan.synaptiq.integration.template.IntegrationRouteTemplates}.
 * Only activated if {@code camel-jdbc} is on the classpath.
 */
@Slf4j
@Component
@ConditionalOnClass(name = "org.apache.camel.component.jdbc.JdbcComponent")
public class DatabaseRouteAdapter implements RouteAdapter {

    @Override
    public String supportedType() {
        return "DATABASE";
    }

    @Override
    public Mono<Void> validateConfig(RouteConfig config) {
        return Mono.fromCallable(() -> {
            String query = config.getParameters().get("query");
            if (query == null || query.isBlank()) {
                throw new IllegalArgumentException(
                        "Parameter 'query' is required for DATABASE connector");
            }
            if (config.getCredentialRef() == null || config.getCredentialRef().isBlank()) {
                throw new IllegalArgumentException(
                        "Credential reference (JDBC URL) is required for DATABASE connector");
            }
            return true;
        }).then();
    }

    @Override
    public Mono<ConnectionTestResult> testConnection(RouteConfig config) {
        return Mono.just(ConnectionTestResult.success(
                "Database connector configured. Connection will be tested on first execution.", 0));
    }
}
