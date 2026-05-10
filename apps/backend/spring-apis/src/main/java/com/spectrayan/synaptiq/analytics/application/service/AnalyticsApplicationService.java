package com.spectrayan.synaptiq.analytics.application.service;

import com.spectrayan.synaptiq.analytics.application.port.in.AnalyticsQueryUseCase;
import com.spectrayan.synaptiq.analytics.application.port.out.AnalyticsPersistencePort;
import com.spectrayan.synaptiq.analytics.domain.model.AnalyticsResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AnalyticsApplicationService implements AnalyticsQueryUseCase {

    private final AnalyticsPersistencePort persistence;

    @Override public Mono<AnalyticsResult.Summary> getSummary(String tenantId, Instant from, Instant to) {
        return persistence.getSummary(tenantId, from, to);
    }

    @Override public Mono<AnalyticsResult.TokenUsage> getTokenUsage(String tenantId, Instant from, Instant to) {
        return persistence.getTokenUsage(tenantId, from, to);
    }

    @Override public Mono<AnalyticsResult.Billing> getBilling(String tenantId, Instant from, Instant to) {
        return persistence.getBilling(tenantId, from, to);
    }

    @Override public Mono<AnalyticsResult.PlatformRollup> getPlatformRollup(Instant from, Instant to) {
        return persistence.getPlatformRollup(from, to);
    }
}
