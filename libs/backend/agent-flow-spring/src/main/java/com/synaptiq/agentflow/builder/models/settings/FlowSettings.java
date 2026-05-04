package com.synaptiq.agentflow.builder.models.settings;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowSettings {
    
    private String id;
    private String name;
    private String entrypoint;
    
    @Builder.Default
    private List<AgentSettings> agents = new ArrayList<>();
    
    @Builder.Default
    private List<EdgeSpec> edges = new ArrayList<>();
    
    @Builder.Default
    private List<MCPServerConfig> mcpServers = new ArrayList<>();
    
    @Builder.Default
    private ExecutionPolicy policy = new ExecutionPolicy();
    
    @Builder.Default
    private FlowType flowType = FlowType.STATIC;
    
    public enum FlowType {
        STATIC, DYNAMIC, HYBRID
    }
}
