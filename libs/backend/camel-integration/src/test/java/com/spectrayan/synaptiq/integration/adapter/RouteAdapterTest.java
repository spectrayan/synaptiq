package com.spectrayan.synaptiq.integration.adapter;

import com.spectrayan.synaptiq.integration.adapter.database.DatabaseRouteAdapter;
import com.spectrayan.synaptiq.integration.adapter.email.EmailRouteAdapter;
import com.spectrayan.synaptiq.integration.adapter.rest.RestRouteAdapter;
import com.spectrayan.synaptiq.integration.adapter.slack.SlackRouteAdapter;
import com.spectrayan.synaptiq.integration.adapter.webhook.WebhookRouteAdapter;
import com.spectrayan.synaptiq.integration.model.ConnectionTestResult;
import com.spectrayan.synaptiq.integration.model.RouteConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for all {@link RouteAdapter} implementations.
 * Validates config validation and connection testing for each adapter type.
 */
@DisplayName("RouteAdapters")
class RouteAdapterTest {

    // ═══════════════════════════════════════════════════════════════
    //  RestRouteAdapter
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("RestRouteAdapter")
    class RestAdapterTests {

        private final RestRouteAdapter adapter = new RestRouteAdapter();

        @Test
        @DisplayName("should report supported type as REST_API")
        void supportedType() {
            assertThat(adapter.supportedType()).isEqualTo("REST_API");
        }

        @Test
        @DisplayName("should validate config with valid URL")
        void validatesValidUrl() {
            RouteConfig config = RouteConfig.builder()
                    .parameters(Map.of("url", "https://api.example.com/data"))
                    .build();

            StepVerifier.create(adapter.validateConfig(config))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should reject config with missing URL")
        void rejectsMissingUrl() {
            RouteConfig config = RouteConfig.builder()
                    .parameters(Map.of())
                    .build();

            StepVerifier.create(adapter.validateConfig(config))
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }

        @Test
        @DisplayName("should reject config with blank URL")
        void rejectsBlankUrl() {
            RouteConfig config = RouteConfig.builder()
                    .parameters(Map.of("url", "  "))
                    .build();

            StepVerifier.create(adapter.validateConfig(config))
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }

        @Test
        @DisplayName("should reject config with invalid URL")
        void rejectsInvalidUrl() {
            RouteConfig config = RouteConfig.builder()
                    .parameters(Map.of("url", "not a url with spaces"))
                    .build();

            StepVerifier.create(adapter.validateConfig(config))
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }

        @Test
        @DisplayName("connection test should fail for unreachable URL")
        void connectionTestFailsForUnreachable() {
            RouteConfig config = RouteConfig.builder()
                    .parameters(Map.of("url", "http://192.0.2.1:19999/unreachable"))
                    .build();

            StepVerifier.create(adapter.testConnection(config))
                    .assertNext(result -> {
                        assertThat(result.isSuccess()).isFalse();
                        assertThat(result.getDurationMs()).isGreaterThanOrEqualTo(0);
                    })
                    .verifyComplete();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  WebhookRouteAdapter
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("WebhookRouteAdapter")
    class WebhookAdapterTests {

        private final WebhookRouteAdapter adapter = new WebhookRouteAdapter();

        @Test
        @DisplayName("should report supported type as WEBHOOK")
        void supportedType() {
            assertThat(adapter.supportedType()).isEqualTo("WEBHOOK");
        }

        @Test
        @DisplayName("should validate config with valid webhookPath")
        void validatesValidPath() {
            RouteConfig config = RouteConfig.builder()
                    .parameters(Map.of("webhookPath", "/my-webhook"))
                    .build();

            StepVerifier.create(adapter.validateConfig(config))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should reject config with missing webhookPath")
        void rejectsMissingPath() {
            RouteConfig config = RouteConfig.builder()
                    .parameters(Map.of())
                    .build();

            StepVerifier.create(adapter.validateConfig(config))
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }

        @Test
        @DisplayName("should reject config with webhookPath not starting with /")
        void rejectsPathWithoutSlash() {
            RouteConfig config = RouteConfig.builder()
                    .parameters(Map.of("webhookPath", "no-slash"))
                    .build();

            StepVerifier.create(adapter.validateConfig(config))
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }

        @Test
        @DisplayName("connection test should return success for valid webhook config")
        void connectionTestForWebhook() {
            RouteConfig config = RouteConfig.builder()
                    .tenantId("tenant-abc")
                    .parameters(Map.of("webhookPath", "/events"))
                    .build();

            StepVerifier.create(adapter.testConnection(config))
                    .assertNext(result -> {
                        assertThat(result.isSuccess()).isTrue();
                        assertThat(result.getMessage())
                                .contains("/webhooks/tenant-abc/events");
                    })
                    .verifyComplete();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  SlackRouteAdapter
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("SlackRouteAdapter")
    class SlackAdapterTests {

        private final SlackRouteAdapter adapter = new SlackRouteAdapter();

        @Test
        @DisplayName("should report supported type as SLACK")
        void supportedType() {
            assertThat(adapter.supportedType()).isEqualTo("SLACK");
        }

        @Test
        @DisplayName("should validate config with channel and credential")
        void validatesValidConfig() {
            RouteConfig config = RouteConfig.builder()
                    .parameters(Map.of("channel", "#general"))
                    .credentialRef("credential/slack-webhook")
                    .build();

            StepVerifier.create(adapter.validateConfig(config))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should reject config with missing channel")
        void rejectsMissingChannel() {
            RouteConfig config = RouteConfig.builder()
                    .parameters(Map.of())
                    .credentialRef("credential/slack-webhook")
                    .build();

            StepVerifier.create(adapter.validateConfig(config))
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }

        @Test
        @DisplayName("should reject config with missing credential")
        void rejectsMissingCredential() {
            RouteConfig config = RouteConfig.builder()
                    .parameters(Map.of("channel", "#general"))
                    .build();

            StepVerifier.create(adapter.validateConfig(config))
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }

        @Test
        @DisplayName("connection test should return success with channel info")
        void connectionTestReturnsSuccess() {
            RouteConfig config = RouteConfig.builder()
                    .parameters(Map.of("channel", "#ops-alerts"))
                    .credentialRef("cred/slack")
                    .build();

            StepVerifier.create(adapter.testConnection(config))
                    .assertNext(result -> {
                        assertThat(result.isSuccess()).isTrue();
                        assertThat(result.getMessage()).contains("#ops-alerts");
                    })
                    .verifyComplete();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  EmailRouteAdapter
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("EmailRouteAdapter")
    class EmailAdapterTests {

        private final EmailRouteAdapter adapter = new EmailRouteAdapter();

        @Test
        @DisplayName("should report supported type as EMAIL")
        void supportedType() {
            assertThat(adapter.supportedType()).isEqualTo("EMAIL");
        }

        @Test
        @DisplayName("should validate config with to and smtpHost")
        void validatesValidConfig() {
            RouteConfig config = RouteConfig.builder()
                    .parameters(Map.of(
                            "to", "alerts@example.com",
                            "smtpHost", "smtp.gmail.com"
                    ))
                    .build();

            StepVerifier.create(adapter.validateConfig(config))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should reject config with missing 'to'")
        void rejectsMissingTo() {
            RouteConfig config = RouteConfig.builder()
                    .parameters(Map.of("smtpHost", "smtp.gmail.com"))
                    .build();

            StepVerifier.create(adapter.validateConfig(config))
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }

        @Test
        @DisplayName("should reject config with missing smtpHost")
        void rejectsMissingSmtpHost() {
            RouteConfig config = RouteConfig.builder()
                    .parameters(Map.of("to", "alerts@example.com"))
                    .build();

            StepVerifier.create(adapter.validateConfig(config))
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }

        @Test
        @DisplayName("connection test should return success")
        void connectionTestReturnsSuccess() {
            RouteConfig config = RouteConfig.builder()
                    .parameters(Map.of(
                            "to", "test@example.com",
                            "smtpHost", "smtp.example.com"
                    ))
                    .build();

            StepVerifier.create(adapter.testConnection(config))
                    .assertNext(result -> {
                        assertThat(result.isSuccess()).isTrue();
                        assertThat(result.getMessage()).contains("test@example.com");
                    })
                    .verifyComplete();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  DatabaseRouteAdapter
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DatabaseRouteAdapter")
    class DatabaseAdapterTests {

        private final DatabaseRouteAdapter adapter = new DatabaseRouteAdapter();

        @Test
        @DisplayName("should report supported type as DATABASE")
        void supportedType() {
            assertThat(adapter.supportedType()).isEqualTo("DATABASE");
        }

        @Test
        @DisplayName("should validate config with query and credential")
        void validatesValidConfig() {
            RouteConfig config = RouteConfig.builder()
                    .parameters(Map.of("query", "SELECT * FROM orders"))
                    .credentialRef("credential/jdbc-url")
                    .build();

            StepVerifier.create(adapter.validateConfig(config))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should reject config with missing query")
        void rejectsMissingQuery() {
            RouteConfig config = RouteConfig.builder()
                    .parameters(Map.of())
                    .credentialRef("credential/jdbc-url")
                    .build();

            StepVerifier.create(adapter.validateConfig(config))
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }

        @Test
        @DisplayName("should reject config with missing credential")
        void rejectsMissingCredential() {
            RouteConfig config = RouteConfig.builder()
                    .parameters(Map.of("query", "SELECT 1"))
                    .build();

            StepVerifier.create(adapter.validateConfig(config))
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }

        @Test
        @DisplayName("connection test should return success placeholder")
        void connectionTestReturnsPlaceholder() {
            RouteConfig config = RouteConfig.builder()
                    .parameters(Map.of("query", "SELECT 1"))
                    .credentialRef("credential/db")
                    .build();

            StepVerifier.create(adapter.testConnection(config))
                    .assertNext(result -> {
                        assertThat(result.isSuccess()).isTrue();
                        assertThat(result.getMessage()).contains("Database connector configured");
                    })
                    .verifyComplete();
        }
    }
}
