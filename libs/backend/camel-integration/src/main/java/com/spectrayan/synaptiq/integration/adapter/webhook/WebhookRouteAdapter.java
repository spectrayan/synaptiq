package com.spectrayan.synaptiq.integration.adapter.webhook;

import com.spectrayan.synaptiq.integration.adapter.RouteAdapter;
import com.spectrayan.synaptiq.integration.model.ConnectionTestResult;
import com.spectrayan.synaptiq.integration.model.RouteConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Validates and tests configuration for inbound webhook integrations.
 * <p>
 * Route construction is handled by the {@code webhook-receiver} Camel RouteTemplate
 * defined in {@link com.spectrayan.synaptiq.integration.template.IntegrationRouteTemplates}.
 */
@Slf4j
@Component
public class WebhookRouteAdapter implements RouteAdapter {

    @Override
    public String supportedType() {
        return "WEBHOOK";
    }

    @Override
    public Mono<Void> validateConfig(RouteConfig config) {
        return Mono.fromCallable(() -> {
            String path = config.getParameters().get("webhookPath");
            if (path == null || path.isBlank()) {
                throw new IllegalArgumentException(
                        "Parameter 'webhookPath' is required for WEBHOOK connector");
            }
            if (!path.startsWith("/")) {
                throw new IllegalArgumentException(
                        "webhookPath must start with '/'");
            }
            return true;
        }).then();
    }

    @Override
    public Mono<ConnectionTestResult> testConnection(RouteConfig config) {
        // Webhooks are inbound — no outbound connection to test.
        return validateConfig(config)
                .thenReturn(ConnectionTestResult.success(
                        "Webhook endpoint will be created at: /webhooks/"
                                + config.getTenantId() + config.getParameters().get("webhookPath"),
                        0))
                .onErrorResume(e -> Mono.just(
                        ConnectionTestResult.failure(e.getMessage(), 0)));
    }
}
