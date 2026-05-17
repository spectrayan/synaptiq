package com.spectrayan.synaptiq.integration.core;

import com.spectrayan.synaptiq.integration.adapter.RouteAdapter;
import com.spectrayan.synaptiq.integration.model.ConnectionTestResult;
import com.spectrayan.synaptiq.integration.model.RouteConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link RouteAdapterRegistry}.
 */
@DisplayName("RouteAdapterRegistry")
class RouteAdapterRegistryTest {

    /**
     * Simple test adapter for testing registry behavior.
     */
    static class TestAdapter implements RouteAdapter {
        private final String type;

        TestAdapter(String type) {
            this.type = type;
        }

        @Override
        public String supportedType() {
            return type;
        }

        @Override
        public Mono<Void> validateConfig(RouteConfig config) {
            return Mono.empty();
        }

        @Override
        public Mono<ConnectionTestResult> testConnection(RouteConfig config) {
            return Mono.just(ConnectionTestResult.success("Test OK", 0));
        }
    }

    @Nested
    @DisplayName("Registration and lookup")
    class Registration {

        @Test
        @DisplayName("should register all adapters from list")
        void registersAllAdapters() {
            var registry = new RouteAdapterRegistry(List.of(
                    new TestAdapter("REST_API"),
                    new TestAdapter("SLACK"),
                    new TestAdapter("WEBHOOK")
            ));

            assertThat(registry.registeredTypes())
                    .containsExactlyInAnyOrder("REST_API", "SLACK", "WEBHOOK");
        }

        @Test
        @DisplayName("should find adapter by type")
        void findsAdapterByType() {
            var registry = new RouteAdapterRegistry(List.of(new TestAdapter("REST_API")));

            assertThat(registry.findAdapter("REST_API")).isPresent();
            assertThat(registry.findAdapter("REST_API").get().supportedType())
                    .isEqualTo("REST_API");
        }

        @Test
        @DisplayName("should return empty for unknown type")
        void returnsEmptyForUnknown() {
            var registry = new RouteAdapterRegistry(List.of(new TestAdapter("REST_API")));

            assertThat(registry.findAdapter("UNKNOWN_TYPE")).isEmpty();
        }

        @Test
        @DisplayName("getAdapter() should throw for unknown type")
        void getAdapterThrowsForUnknown() {
            var registry = new RouteAdapterRegistry(List.of(new TestAdapter("REST_API")));

            assertThatThrownBy(() -> registry.getAdapter("MISSING"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("MISSING");
        }

        @Test
        @DisplayName("should handle empty adapter list")
        void handlesEmptyList() {
            var registry = new RouteAdapterRegistry(List.of());

            assertThat(registry.registeredTypes()).isEmpty();
            assertThat(registry.findAdapter("REST_API")).isEmpty();
        }
    }
}
