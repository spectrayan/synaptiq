package com.synaptiq.analytics.domain.model;

import java.util.Map;

/**
 * Typed result records for analytics queries.
 * Used instead of Map<String, Object> throughout the analytics domain.
 */
public sealed interface AnalyticsResult {

    record Summary(
        long totalSessions,
        long totalTokens,
        double totalCost,
        long uniqueUsers
    ) implements AnalyticsResult {}

    record TokenUsage(
        long promptTokens,
        long completionTokens,
        long totalTokens,
        Map<String, Long> byModel
    ) implements AnalyticsResult {}

    record Billing(
        double totalCost,
        long totalTokens,
        double costPerToken,
        Map<String, Double> costByModel
    ) implements AnalyticsResult {}

    record PlatformRollup(
        long totalTenants,
        long totalSessions,
        long totalTokens,
        double totalCost,
        Map<String, Long> tokensByTenant
    ) implements AnalyticsResult {}
}
