package com.spectrayan.synaptiq.agentflow.builder.models.settings;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

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

    /** Context caching configuration for reducing LLM token costs. */
    @Builder.Default
    private CacheConfig cacheConfig = new CacheConfig();

    /** Session storage backend configuration. */
    @Builder.Default
    private SessionConfig sessionConfig = new SessionConfig();

    public enum FlowType {
        STATIC, DYNAMIC, HYBRID
    }
}

