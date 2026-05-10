package com.spectrayan.synaptiq.agentflow.builder.models.settings;

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
public class MCPServerConfig {
    
    private String id;
    
    @Builder.Default
    private MCPTransport transport = MCPTransport.STDIO;
    
    private String command;
    
    @Builder.Default
    private List<String> args = new ArrayList<>();
    
    @Builder.Default
    private Map<String, String> env = new HashMap<>();
    
    private String url;
    
    public enum MCPTransport {
        STDIO, HTTP, WS
    }
}
