package com.spectrayan.synaptiq.integration.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for model classes: {@link RouteConfig}, {@link ExecutionResult},
 * {@link ConnectionTestResult}, {@link TemplateDescriptor}.
 */
@DisplayName("Models")
class ModelTest {

    @Nested
    @DisplayName("RouteConfig")
    class RouteConfigTests {

        @Test
        @DisplayName("should build with defaults")
        void buildsWithDefaults() {
            RouteConfig config = RouteConfig.builder()
                    .routeConfigId("rc-1")
                    .tenantId("t-1")
                    .name("Test Route")
                    .build();

            assertThat(config.getStatus()).isEqualTo(RouteStatus.PENDING);
            assertThat(config.getParameters()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("should build with all fields")
        void buildsWithAllFields() {
            RouteConfig config = RouteConfig.builder()
                    .routeConfigId("rc-2")
                    .tenantId("t-1")
                    .name("Full Route")
                    .description("A complete test route")
                    .connectorType("REST_API")
                    .templateId("rest-api-poll")
                    .parameters(Map.of("url", "https://example.com"))
                    .routeYaml(null)
                    .credentialRef("cred-ref")
                    .status(RouteStatus.ACTIVE)
                    .camelRouteId("t-1__rc-2")
                    .build();

            assertThat(config.getStatus()).isEqualTo(RouteStatus.ACTIVE);
            assertThat(config.getConnectorType()).isEqualTo("REST_API");
            assertThat(config.getParameters()).containsEntry("url", "https://example.com");
            assertThat(config.getCamelRouteId()).isEqualTo("t-1__rc-2");
        }
    }

    @Nested
    @DisplayName("ExecutionResult")
    class ExecutionResultTests {

        @Test
        @DisplayName("success() should create a SUCCESS result")
        void successResult() {
            ExecutionResult result = ExecutionResult.success(
                    "rc-1", "tenant-1", "tenant-1__rc-1", 150,
                    Map.of("key", "value"));

            assertThat(result.getStatus()).isEqualTo(ExecutionResult.ExecutionStatus.SUCCESS);
            assertThat(result.getDurationMs()).isEqualTo(150);
            assertThat(result.getOutput()).containsEntry("key", "value");
            assertThat(result.getErrorMessage()).isNull();
            assertThat(result.getExecutedAt()).isNotNull();
        }

        @Test
        @DisplayName("failure() should create a FAILURE result")
        void failureResult() {
            ExecutionResult result = ExecutionResult.failure(
                    "rc-1", "tenant-1", "tenant-1__rc-1", 250,
                    "Connection refused");

            assertThat(result.getStatus()).isEqualTo(ExecutionResult.ExecutionStatus.FAILURE);
            assertThat(result.getDurationMs()).isEqualTo(250);
            assertThat(result.getErrorMessage()).isEqualTo("Connection refused");
            assertThat(result.getOutput()).isNull();
        }

        @Test
        @DisplayName("default status should be SUCCESS")
        void defaultStatus() {
            ExecutionResult result = ExecutionResult.builder().build();
            assertThat(result.getStatus()).isEqualTo(ExecutionResult.ExecutionStatus.SUCCESS);
        }

        @Test
        @DisplayName("all execution statuses should be present")
        void allStatuses() {
            assertThat(ExecutionResult.ExecutionStatus.values())
                    .containsExactlyInAnyOrder(
                            ExecutionResult.ExecutionStatus.SUCCESS,
                            ExecutionResult.ExecutionStatus.FAILURE,
                            ExecutionResult.ExecutionStatus.TIMEOUT,
                            ExecutionResult.ExecutionStatus.RATE_LIMITED
                    );
        }
    }

    @Nested
    @DisplayName("ConnectionTestResult")
    class ConnectionTestResultTests {

        @Test
        @DisplayName("success() should create a successful result")
        void successResult() {
            ConnectionTestResult result = ConnectionTestResult.success("HTTP 200 OK", 42);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).isEqualTo("HTTP 200 OK");
            assertThat(result.getDurationMs()).isEqualTo(42);
            assertThat(result.getTestedAt()).isNotNull();
        }

        @Test
        @DisplayName("failure() should create a failed result")
        void failureResult() {
            ConnectionTestResult result = ConnectionTestResult.failure("Connection refused", 100);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getMessage()).isEqualTo("Connection refused");
            assertThat(result.getDurationMs()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("ConnectorType enum")
    class ConnectorTypeTests {

        @Test
        @DisplayName("should contain all expected values")
        void containsAllValues() {
            assertThat(ConnectorType.values()).containsExactlyInAnyOrder(
                    ConnectorType.REST_API,
                    ConnectorType.WEBHOOK,
                    ConnectorType.DATABASE,
                    ConnectorType.SLACK,
                    ConnectorType.EMAIL,
                    ConnectorType.MCP_SERVER,
                    ConnectorType.MESSAGE_QUEUE,
                    ConnectorType.FILE_STORAGE,
                    ConnectorType.CUSTOM_YAML
            );
        }
    }

    @Nested
    @DisplayName("RouteStatus enum")
    class RouteStatusTests {

        @Test
        @DisplayName("should contain all lifecycle states")
        void containsAllStates() {
            assertThat(RouteStatus.values()).containsExactlyInAnyOrder(
                    RouteStatus.PENDING,
                    RouteStatus.ACTIVE,
                    RouteStatus.ERROR,
                    RouteStatus.DISABLED
            );
        }
    }

    @Nested
    @DisplayName("TemplateDescriptor.ParameterDefinition")
    class ParameterDefinitionTests {

        @Test
        @DisplayName("should build with defaults")
        void buildsWithDefaults() {
            var param = TemplateDescriptor.ParameterDefinition.builder()
                    .name("url")
                    .displayName("API URL")
                    .type("string")
                    .build();

            assertThat(param.isRequired()).isTrue(); // default is true
            assertThat(param.getDefaultValue()).isNull();
        }

        @Test
        @DisplayName("should build optional param with default value")
        void buildsOptional() {
            var param = TemplateDescriptor.ParameterDefinition.builder()
                    .name("method")
                    .displayName("HTTP Method")
                    .type("string")
                    .required(false)
                    .defaultValue("GET")
                    .build();

            assertThat(param.isRequired()).isFalse();
            assertThat(param.getDefaultValue()).isEqualTo("GET");
        }
    }
}
