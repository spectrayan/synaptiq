package com.synaptiq.analytics.application.service;

import com.synaptiq.analytics.application.port.out.AnalyticsPersistencePort;
import com.synaptiq.analytics.domain.model.TokenUsageRecord;
import com.synaptiq.shared.event.TokenUsageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenUsageListener {
    private final AnalyticsPersistencePort persistence;

    @ApplicationModuleListener
    public void onTokenUsageEvent(TokenUsageEvent event) {
        log.info("Analytics module received TokenUsageEvent: tenantId={}, sessionId={}, model={}, inputTokens={}, outputTokens={}",
                event.tenantId(), event.sessionId(), event.model(), event.inputTokens(), event.outputTokens());
        
        persistence.saveTokenUsage(TokenUsageRecord.builder()
                .tenantId(event.tenantId())
                .sessionId(event.sessionId())
                .model(event.model())
                .inputTokens(event.inputTokens())
                .outputTokens(event.outputTokens())
                .timestamp(event.timestamp())
                .build()).subscribe();
    }
}
