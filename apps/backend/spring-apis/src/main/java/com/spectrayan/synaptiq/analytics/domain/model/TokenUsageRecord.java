package com.spectrayan.synaptiq.analytics.domain.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@Document(collection = "token_usages")
public class TokenUsageRecord {
    @Id
    private String id;
    private String tenantId;
    private String appId;
    private String sessionId;
    private String model;
    private long inputTokens;
    private long outputTokens;
    private Instant timestamp;
}
