package com.synaptiq.shared.event;

import java.time.Instant;

public record TokenUsageEvent(
    String tenantId,
    String sessionId,
    String model,
    long inputTokens,
    long outputTokens,
    Instant timestamp
) {}
