package com.spectrayan.synaptiq.integration.adapter.slack;

import com.spectrayan.synaptiq.integration.adapter.RouteAdapter;
import com.spectrayan.synaptiq.integration.model.ConnectionTestResult;
import com.spectrayan.synaptiq.integration.model.RouteConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Validates and tests configuration for Slack integrations.
 * <p>
 * Route construction is handled by the {@code slack-notify} Camel RouteTemplate
 * defined in {@link com.spectrayan.synaptiq.integration.template.IntegrationRouteTemplates}.
 * Only activated if {@code camel-slack} is on the classpath.
 */
@Slf4j
@Component
@ConditionalOnClass(name = "org.apache.camel.component.slack.SlackComponent")
public class SlackRouteAdapter implements RouteAdapter {

    @Override
    public String supportedType() {
        return "SLACK";
    }

    @Override
    public Mono<Void> validateConfig(RouteConfig config) {
        return Mono.fromCallable(() -> {
            String channel = config.getParameters().get("channel");
            if (channel == null || channel.isBlank()) {
                throw new IllegalArgumentException(
                        "Parameter 'channel' is required for SLACK connector");
            }
            if (config.getCredentialRef() == null || config.getCredentialRef().isBlank()) {
                throw new IllegalArgumentException(
                        "Credential reference (webhook URL) is required for SLACK connector");
            }
            return true;
        }).then();
    }

    @Override
    public Mono<ConnectionTestResult> testConnection(RouteConfig config) {
        return Mono.just(ConnectionTestResult.success(
                "Slack webhook configured for channel: "
                        + config.getParameters().get("channel"), 0));
    }
}
