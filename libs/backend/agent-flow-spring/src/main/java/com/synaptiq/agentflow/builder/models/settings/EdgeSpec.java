package com.synaptiq.agentflow.builder.models.settings;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EdgeSpec {
    
    private String source;
    private String target;
    
    @Builder.Default
    private EdgeCondition condition = EdgeCondition.ALWAYS;
    
    public enum EdgeCondition {
        ALWAYS, ON_TOOL_CALLS, NEVER
    }
}
