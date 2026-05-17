package com.spectrayan.synaptiq.agentflow.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spectrayan.synaptiq.agentflow.builder.models.settings.FlowSettings;
import com.spectrayan.synaptiq.agentflow.spi.AgentFlowProvider;
import com.spectrayan.synaptiq.agentflow.spi.CompiledFlow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrator that delegates flow compilation to the active
 * {@link AgentFlowProvider}.
 * <p>
 * This class is provider-agnostic — the actual agent/graph construction
 * is performed by whichever provider is configured (Google ADK, LangGraph4J, etc.).
 */
@Slf4j
@RequiredArgsConstructor
public class FlowBuilder {

    private final AgentFlowProvider provider;
    private final ObjectMapper objectMapper;

    /**
     * Compile the given flow settings into an executable flow using
     * the active provider.
     *
     * @param settings the parsed flow definition
     * @return a compiled, ready-to-run flow
     */
    public CompiledFlow build(FlowSettings settings) {
        log.info("Building flow '{}' ({}) using provider: {}",
            settings.getName(), settings.getId(), provider.name());
        return provider.compile(settings);
    }

    /**
     * Parse JSON into FlowSettings and compile.
     *
     * @param settingsJson JSON string representing the flow definition
     * @return a compiled, ready-to-run flow
     * @throws Exception if JSON parsing fails
     */
    public CompiledFlow buildFromJson(String settingsJson) throws Exception {
        FlowSettings settings = objectMapper.readValue(settingsJson, FlowSettings.class);
        return build(settings);
    }
}
