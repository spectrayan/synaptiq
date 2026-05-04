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
public class ToolSettings {
    
    private String id;
    private ToolType type;
    private String name;
    private String importPath;
    private String mcpServer;
    private String mcpTool;
    
    @Builder.Default
    private Map<String, Object> params = new HashMap<>();
    
    public enum ToolType {
        PYTHON, JAVA, MCP, LANGCHAIN, SPRING_BEAN
    }
}
