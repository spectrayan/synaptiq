package com.synaptiq.agentflow.builder.models.settings;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceLimits {
    
    private Integer totalTimeoutMs;
    private Integer stepTimeoutMs;
    private Integer maxTokens;
    private Double rateLimitRps;
}
