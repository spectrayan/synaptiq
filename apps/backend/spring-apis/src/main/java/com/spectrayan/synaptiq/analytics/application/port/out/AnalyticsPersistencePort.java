package com.spectrayan.synaptiq.analytics.application.port.out;

import com.spectrayan.synaptiq.analytics.domain.model.AnalyticsResult;
import com.spectrayan.synaptiq.analytics.domain.model.TokenUsageRecord;
import reactor.core.publisher.Mono;
import java.time.Instant;

public interface AnalyticsPersistencePort {
    Mono<TokenUsageRecord> saveTokenUsage(TokenUsageRecord record);
    Mono<AnalyticsResult.Summary> getSummary(String tenantId, Instant from, Instant to);
    Mono<AnalyticsResult.TokenUsage> getTokenUsage(String tenantId, Instant from, Instant to);
    Mono<AnalyticsResult.Billing> getBilling(String tenantId, Instant from, Instant to);
    Mono<AnalyticsResult.PlatformRollup> getPlatformRollup(Instant from, Instant to);
}
