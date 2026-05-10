package com.spectrayan.synaptiq.integration.adapter.email;

import com.spectrayan.synaptiq.integration.adapter.RouteAdapter;
import com.spectrayan.synaptiq.integration.model.ConnectionTestResult;
import com.spectrayan.synaptiq.integration.model.RouteConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Validates and tests configuration for email (SMTP) integrations.
 * <p>
 * Route construction is handled by the {@code email-notify} Camel RouteTemplate
 * defined in {@link com.spectrayan.synaptiq.integration.template.IntegrationRouteTemplates}.
 * Only activated if {@code camel-mail} is on the classpath.
 */
@Slf4j
@Component
@ConditionalOnClass(name = "org.apache.camel.component.mail.MailComponent")
public class EmailRouteAdapter implements RouteAdapter {

    @Override
    public String supportedType() {
        return "EMAIL";
    }

    @Override
    public Mono<Void> validateConfig(RouteConfig config) {
        return Mono.fromCallable(() -> {
            String to = config.getParameters().get("to");
            if (to == null || to.isBlank()) {
                throw new IllegalArgumentException(
                        "Parameter 'to' is required for EMAIL connector");
            }
            String smtpHost = config.getParameters().get("smtpHost");
            if (smtpHost == null || smtpHost.isBlank()) {
                throw new IllegalArgumentException(
                        "Parameter 'smtpHost' is required for EMAIL connector");
            }
            return true;
        }).then();
    }

    @Override
    public Mono<ConnectionTestResult> testConnection(RouteConfig config) {
        return Mono.just(ConnectionTestResult.success(
                "Email connector configured for: " + config.getParameters().get("to"), 0));
    }
}
