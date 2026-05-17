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
public class AgentSettings {
    
    private String id;
    private String name;
    private String systemPrompt;
    private String instructions;
    private LLMSettings llm;
    
    @Builder.Default
    private List<ToolSettings> tools = new ArrayList<>();

    /** Whether this agent can be executed in parallel with siblings. */
    @Builder.Default
    private boolean parallelizable = false;

    /** Sub-agents for hierarchical composition. */
    @Builder.Default
    private List<AgentSettings> subAgents = new ArrayList<>();
}

