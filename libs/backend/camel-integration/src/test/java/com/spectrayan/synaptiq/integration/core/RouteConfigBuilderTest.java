package com.spectrayan.synaptiq.integration.core;

import com.spectrayan.synaptiq.integration.model.RouteConfig;
import com.spectrayan.synaptiq.integration.model.RouteStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link RouteConfigBuilder}.
 */
@DisplayName("RouteConfigBuilder")
class RouteConfigBuilderTest {

    @Nested
    @DisplayName("Successful builds")
    class SuccessfulBuilds {

        @Test
        @DisplayName("should build a template-based config")
        void buildsTemplateBasedConfig() {
            RouteConfig config = RouteConfigBuilder.forTenant("tenant-123")
                    .name("Slack Alerts")
                    .connectorType("SLACK")
                    .templateId("slack-notify")
                    .param("channel", "#ops-alerts")
                    .param("webhookUrl", "https://hooks.slack.com/...")
                    .credentialRef("credential/slack-token")
                    .build();

            assertThat(config.getTenantId()).isEqualTo("tenant-123");
            assertThat(config.getName()).isEqualTo("Slack Alerts");
            assertThat(config.getConnectorType()).isEqualTo("SLACK");
            assertThat(config.getTemplateId()).isEqualTo("slack-notify");
            assertThat(config.getParameters()).containsEntry("channel", "#ops-alerts");
            assertThat(config.getParameters()).containsEntry("webhookUrl", "https://hooks.slack.com/...");
            assertThat(config.getCredentialRef()).isEqualTo("credential/slack-token");
            assertThat(config.getStatus()).isEqualTo(RouteStatus.PENDING);
            assertThat(config.getRouteConfigId()).isNotBlank(); // Auto-generated UUID
            assertThat(config.getCreatedAt()).isNotNull();
            assertThat(config.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should build a YAML-based config")
        void buildsYamlBasedConfig() {
            String yaml = """
                    - route:
                        from:
                          uri: "timer:test?period=5000"
                        steps:
                          - log:
                              message: "Hello from YAML"
                    """;

            RouteConfig config = RouteConfigBuilder.forTenant("tenant-456")
                    .name("Custom Timer Route")
                    .connectorType("CUSTOM_YAML")
                    .routeYaml(yaml)
                    .build();

            assertThat(config.getTemplateId()).isNull();
            assertThat(config.getRouteYaml()).isEqualTo(yaml);
            assertThat(config.getConnectorType()).isEqualTo("CUSTOM_YAML");
        }

        @Test
        @DisplayName("should allow custom routeConfigId")
        void allowsCustomId() {
            RouteConfig config = RouteConfigBuilder.forTenant("tenant-1")
                    .routeConfigId("my-custom-id")
                    .name("Test")
                    .connectorType("REST_API")
                    .templateId("rest-api-poll")
                    .build();

            assertThat(config.getRouteConfigId()).isEqualTo("my-custom-id");
        }

        @Test
        @DisplayName("should accept bulk parameters via params()")
        void acceptsBulkParams() {
            Map<String, String> params = Map.of(
                    "url", "https://api.example.com",
                    "method", "POST"
            );

            RouteConfig config = RouteConfigBuilder.forTenant("t1")
                    .name("Bulk")
                    .connectorType("REST_API")
                    .params(params)
                    .templateId("rest-api-poll")
                    .build();

            assertThat(config.getParameters()).containsAllEntriesOf(params);
        }
    }

    @Nested
    @DisplayName("Validation failures")
    class ValidationFailures {

        @Test
        @DisplayName("should throw when tenantId is null")
        void throwsOnNullTenantId() {
            assertThatThrownBy(() -> RouteConfigBuilder.forTenant(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("tenantId");
        }

        @Test
        @DisplayName("should throw when name is missing")
        void throwsOnMissingName() {
            assertThatThrownBy(() ->
                    RouteConfigBuilder.forTenant("t1")
                            .connectorType("REST_API")
                            .templateId("rest-api-poll")
                            .build()
            ).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("should throw when connectorType is missing")
        void throwsOnMissingConnectorType() {
            assertThatThrownBy(() ->
                    RouteConfigBuilder.forTenant("t1")
                            .name("Test")
                            .templateId("rest-api-poll")
                            .build()
            ).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("connectorType");
        }

        @Test
        @DisplayName("should throw when no templateId, routeYaml, or parameters")
        void throwsOnMissingRouteDefinition() {
            assertThatThrownBy(() ->
                    RouteConfigBuilder.forTenant("t1")
                            .name("Test")
                            .connectorType("REST_API")
                            .build()
            ).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("templateId");
        }
    }
}
