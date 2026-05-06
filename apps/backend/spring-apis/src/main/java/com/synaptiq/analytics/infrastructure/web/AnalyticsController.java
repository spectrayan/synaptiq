package com.synaptiq.analytics.infrastructure.web;

import com.synaptiq.analytics.application.port.in.AnalyticsQueryUseCase;
import com.synaptiq.infrastructure.in.web.api.AnalyticsApi;
import com.synaptiq.infrastructure.in.web.dto.AnalyticsSummaryResponse;
import com.synaptiq.infrastructure.in.web.dto.BillingResponse;
import com.synaptiq.infrastructure.in.web.dto.PlatformRollupResponse;
import com.synaptiq.infrastructure.in.web.dto.TokenUsageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.*;

@RestController
@RequiredArgsConstructor
public class AnalyticsController implements AnalyticsApi {
    private final AnalyticsQueryUseCase analyticsUseCase;

    @Override
    public Mono<ResponseEntity<AnalyticsSummaryResponse>> getAnalyticsSummary(
            String xTenantID, LocalDate from, LocalDate to, ServerWebExchange exchange) {
        return analyticsUseCase.getSummary(xTenantID, toInstant(from, 30), toInstant(to, 0))
            .map(s -> ResponseEntity.ok(new AnalyticsSummaryResponse()
                .totalConversations((int) s.totalSessions())
                .totalMessages(0)
                .totalTokensInput((int) s.totalTokens())
                .totalTokensOutput(0)
                .totalActions(0)
                .uniqueUsers((int) s.uniqueUsers())
                .avgMessagesPerSession(0.0)));
    }

    @Override
    public Mono<ResponseEntity<TokenUsageResponse>> getTokenUsage(
            String xTenantID, LocalDate from, LocalDate to, ServerWebExchange exchange) {
        return analyticsUseCase.getTokenUsage(xTenantID, toInstant(from, 30), toInstant(to, 0))
            .map(t -> ResponseEntity.ok(new TokenUsageResponse()
                .totalTokensInput((int) t.promptTokens())
                .totalTokensOutput((int) t.completionTokens())
                .totalTokens((int) t.totalTokens())
                .estimatedCostUsd((t.promptTokens() * 0.0001) + (t.completionTokens() * 0.0002))
                .planTokenLimit(1_000_000)
                .usagePercent((t.totalTokens() / 1_000_000.0) * 100)));
    }

    @Override
    public Mono<ResponseEntity<BillingResponse>> getBilling(
            String xTenantID, LocalDate from, LocalDate to, ServerWebExchange exchange) {
        return analyticsUseCase.getBilling(xTenantID, toInstant(from, 30), toInstant(to, 0))
            .map(b -> ResponseEntity.ok(new BillingResponse()
                .seatCount(1)
                .totalTokens((int) b.totalTokens())
                .estimatedCostUsd(b.totalCost())));
    }

    @Override
    public Mono<ResponseEntity<PlatformRollupResponse>> getPlatformRollup(
            LocalDate from, LocalDate to, ServerWebExchange exchange) {
        return analyticsUseCase.getPlatformRollup(toInstant(from, 30), toInstant(to, 0))
            .map(r -> ResponseEntity.ok(new PlatformRollupResponse()
                .totalTenants((int) r.totalTenants())
                .activeTenants((int) r.totalTenants())
                .totalConversations((int) r.totalSessions())
                .totalMessages(0)
                .totalTokens((int) r.totalTokens())
                .totalEstimatedCostUsd(r.totalCost())));
    }

    private Instant toInstant(LocalDate d, int defaultDaysAgo) {
        if (d != null) return d.atStartOfDay(ZoneOffset.UTC).toInstant();
        return Instant.now().minus(Duration.ofDays(defaultDaysAgo));
    }
}
