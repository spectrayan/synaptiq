package com.spectrayan.synaptiq.integration.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TenantRouteNamingStrategy}.
 */
@DisplayName("TenantRouteNamingStrategy")
class TenantRouteNamingStrategyTest {

    @Nested
    @DisplayName("buildRouteId()")
    class BuildRouteId {

        @Test
        @DisplayName("should combine tenant and route config IDs with separator")
        void combinesIdsWithSeparator() {
            String result = TenantRouteNamingStrategy.buildRouteId("tenant-abc", "route-123");
            assertThat(result).isEqualTo("tenant-abc__route-123");
        }

        @Test
        @DisplayName("should handle UUIDs")
        void handlesUuids() {
            String result = TenantRouteNamingStrategy.buildRouteId(
                    "a1b2c3d4", "550e8400-e29b-41d4-a716-446655440000");
            assertThat(result).isEqualTo("a1b2c3d4__550e8400-e29b-41d4-a716-446655440000");
        }
    }

    @Nested
    @DisplayName("extractTenantId()")
    class ExtractTenantId {

        @Test
        @DisplayName("should extract tenant ID from valid route ID")
        void extractsFromValid() {
            assertThat(TenantRouteNamingStrategy.extractTenantId("tenant-abc__route-123"))
                    .isEqualTo("tenant-abc");
        }

        @Test
        @DisplayName("should return null for route ID without separator")
        void returnsNullWithoutSeparator() {
            assertThat(TenantRouteNamingStrategy.extractTenantId("no-separator-here"))
                    .isNull();
        }

        @Test
        @DisplayName("should return null for null input")
        void returnsNullForNull() {
            assertThat(TenantRouteNamingStrategy.extractTenantId(null))
                    .isNull();
        }

        @Test
        @DisplayName("should handle route ID with multiple separators")
        void handlesMultipleSeparators() {
            // First occurrence should be used
            assertThat(TenantRouteNamingStrategy.extractTenantId("tenant__config__extra"))
                    .isEqualTo("tenant");
        }
    }

    @Nested
    @DisplayName("extractRouteConfigId()")
    class ExtractRouteConfigId {

        @Test
        @DisplayName("should extract route config ID from valid route ID")
        void extractsFromValid() {
            assertThat(TenantRouteNamingStrategy.extractRouteConfigId("tenant-abc__route-123"))
                    .isEqualTo("route-123");
        }

        @Test
        @DisplayName("should return null for route ID without separator")
        void returnsNullWithoutSeparator() {
            assertThat(TenantRouteNamingStrategy.extractRouteConfigId("no-separator"))
                    .isNull();
        }

        @Test
        @DisplayName("should return null for null input")
        void returnsNullForNull() {
            assertThat(TenantRouteNamingStrategy.extractRouteConfigId(null))
                    .isNull();
        }

        @Test
        @DisplayName("should handle multiple separators — returns everything after first")
        void handlesMultipleSeparators() {
            assertThat(TenantRouteNamingStrategy.extractRouteConfigId("tenant__config__extra"))
                    .isEqualTo("config__extra");
        }
    }

    @Nested
    @DisplayName("directEndpoint()")
    class DirectEndpoint {

        @Test
        @DisplayName("should build a direct endpoint scoped to tenant")
        void buildsDirectEndpoint() {
            String result = TenantRouteNamingStrategy.directEndpoint("tenant-abc", "process");
            assertThat(result).isEqualTo("direct:tenant-abc__process");
        }
    }
}
