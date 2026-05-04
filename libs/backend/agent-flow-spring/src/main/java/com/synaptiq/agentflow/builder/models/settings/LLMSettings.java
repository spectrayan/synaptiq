package com.synaptiq.agentflow.builder.models.settings;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMSettings {
    
    private Provider provider;
    private String model;
    
    @Builder.Default
    private double temperature = 0.0;
    
    private Integer maxTokens;
    
    @Builder.Default
    private boolean streaming = true;
    
    @Builder.Default
    private Map<String, Object> params = new HashMap<>();
    
    public enum Provider {
        VERTEXAI, OPENAI, ANTHROPIC
    }
}
