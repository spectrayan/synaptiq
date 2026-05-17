package com.spectrayan.synaptiq.agentflow.provider.adk;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.LoopAgent;
import com.google.adk.agents.ParallelAgent;
import com.google.adk.agents.SequentialAgent;
import com.spectrayan.synaptiq.agentflow.builder.models.settings.AgentSettings;
import com.spectrayan.synaptiq.agentflow.builder.models.settings.FlowSettings;
import com.spectrayan.synaptiq.agentflow.builder.models.settings.ToolSettings;
import com.spectrayan.synaptiq.agentflow.spi.AgentFlowProvider;
import com.spectrayan.synaptiq.agentflow.spi.CompiledFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Google ADK implementation of {@link AgentFlowProvider}.
 * <p>
 * Compiles declarative {@link FlowSettings} into an executable
 * {@link AdkCompiledFlow} by:
 * <ol>
 *   <li>Mapping each {@link AgentSettings} → ADK {@link LlmAgent}</li>
 *   <li>Mapping {@link ToolSettings} → ADK tools (including native MCP via McpToolset)</li>
 *   <li>Assembling agents into the appropriate workflow topology
 *       ({@link SequentialAgent}, {@link ParallelAgent}, {@link LoopAgent})</li>
 *   <li>Configuring context caching and session persistence</li>
 * </ol>
 */
public class AdkFlowProvider implements AgentFlowProvider {

    private static final Logger log = LoggerFactory.getLogger(AdkFlowProvider.class);

    private final ApplicationContext applicationContext;

    public AdkFlowProvider(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public String name() {
        return "google-adk";
    }

    @Override
    public CompiledFlow compile(FlowSettings settings) {
        log.info("Compiling flow '{}' (type: {}) using Google ADK provider",
            settings.getName(), settings.getFlowType());

        // 1. Resolve MCP toolsets globally (shared across agents)
        List<Object> mcpToolsets = new ArrayList<>();
        if (settings.getMcpServers() != null && !settings.getMcpServers().isEmpty()) {
            mcpToolsets = AdkToolFactory.resolveMcpToolsets(settings.getMcpServers());
        }

        // 2. Build individual LlmAgent instances
        Map<String, LlmAgent> agentMap = buildAgents(settings, mcpToolsets);

        // 3. Assemble the execution graph based on flow type and edges
        BaseAgent rootAgent = assembleGraph(settings, agentMap);

        log.info("Flow '{}' compiled: root agent='{}', total agents={}",
            settings.getName(), rootAgent.name(), agentMap.size());

        // 4. Wrap in compiled flow with session and cache config
        return new AdkCompiledFlow(settings.getName(), rootAgent, settings);
    }

    /**
     * Build all LlmAgent instances from the flow settings.
     */
    private Map<String, LlmAgent> buildAgents(FlowSettings settings, List<Object> mcpToolsets) {
        Map<String, LlmAgent> agentMap = new LinkedHashMap<>();

        for (AgentSettings agentSettings : settings.getAgents()) {
            // Build agent-specific tools (includes MCP toolsets)
            List<Object> agentTools = new ArrayList<>(mcpToolsets);

            if (agentSettings.getTools() != null) {
                for (ToolSettings toolSettings : agentSettings.getTools()) {
                    if (toolSettings.getType() != ToolSettings.ToolType.MCP) {
                        agentTools.add(AdkToolFactory.createTool(toolSettings, applicationContext));
                    }
                    // MCP tools are already in mcpToolsets
                }
            }

            LlmAgent agent = AdkAgentFactory.createLlmAgent(agentSettings, agentTools);
            String key = agentSettings.getId() != null ? agentSettings.getId() : agentSettings.getName();
            agentMap.put(key, agent);

            log.debug("Built agent '{}' with {} tools", agentSettings.getName(), agentTools.size());
        }

        return agentMap;
    }

    /**
     * Assemble agents into the correct ADK workflow topology based on
     * flow type and parallelization flags.
     */
    private BaseAgent assembleGraph(FlowSettings settings, Map<String, LlmAgent> agentMap) {
        List<BaseAgent> agents = new ArrayList<>(agentMap.values());

        if (agents.size() == 1) {
            return agents.getFirst();
        }

        return switch (settings.getFlowType()) {
            case STATIC -> assembleStaticGraph(settings, agents);
            case DYNAMIC -> assembleDynamicGraph(settings, agents);
            case HYBRID -> assembleHybridGraph(settings, agents);
        };
    }

    /**
     * Static flow: use parallelizable flags to determine topology.
     */
    private BaseAgent assembleStaticGraph(FlowSettings settings, List<BaseAgent> agents) {
        boolean allParallel = settings.getAgents().stream()
            .allMatch(AgentSettings::isParallelizable);

        if (allParallel) {
            log.info("All agents marked parallelizable — using ParallelAgent");
            return ParallelAgent.builder()
                .name(settings.getName() + "_parallel")
                .subAgents(agents)
                .build();
        }

        // Mixed: group parallelizable agents together
        List<BaseAgent> pipeline = buildMixedPipeline(settings, agents);

        return SequentialAgent.builder()
            .name(settings.getName() + "_sequential")
            .subAgents(pipeline)
            .build();
    }

    /**
     * Dynamic flow: root LlmAgent with sub-agents for autonomous orchestration.
     */
    private BaseAgent assembleDynamicGraph(FlowSettings settings, List<BaseAgent> agents) {
        log.info("Dynamic flow — root agent will orchestrate {} sub-agents", agents.size());

        BaseAgent rootCandidate = agents.getFirst();
        if (settings.getEntrypoint() != null) {
            rootCandidate = agents.stream()
                .filter(a -> a.name().equals(settings.getEntrypoint()))
                .findFirst()
                .orElse(agents.getFirst());
        }

        // For dynamic flows, rebuild the root agent with sub-agents attached
        if (rootCandidate instanceof LlmAgent) {
            final BaseAgent root = rootCandidate;
            List<BaseAgent> subAgents = agents.stream()
                .filter(a -> a != root)
                .toList();

            // Rebuild with sub-agents for agent transfer
            return LlmAgent.builder()
                .name(root.name())
                .model(AdkAgentFactory.resolveModel(
                    settings.getAgents().getFirst().getLlm()))
                .instruction(AdkAgentFactory.buildInstruction(
                    settings.getAgents().getFirst()))
                .subAgents(subAgents)
                .build();
        }

        return rootCandidate;
    }

    /**
     * Hybrid flow: LoopAgent wrapping a SequentialAgent for iterative patterns.
     */
    private BaseAgent assembleHybridGraph(FlowSettings settings, List<BaseAgent> agents) {
        log.info("Hybrid flow — using LoopAgent with {} agents", agents.size());

        SequentialAgent inner = SequentialAgent.builder()
            .name(settings.getName() + "_inner")
            .subAgents(agents)
            .build();

        return LoopAgent.builder()
            .name(settings.getName() + "_loop")
            .subAgents(inner)
            .build();
    }

    /**
     * Build a mixed pipeline where consecutive parallelizable agents are
     * grouped into {@link ParallelAgent} nodes within a sequential flow.
     */
    private List<BaseAgent> buildMixedPipeline(FlowSettings settings, List<BaseAgent> agents) {
        List<BaseAgent> pipeline = new ArrayList<>();
        List<BaseAgent> parallelBatch = new ArrayList<>();

        for (int i = 0; i < settings.getAgents().size(); i++) {
            AgentSettings agentSettings = settings.getAgents().get(i);
            BaseAgent agent = agents.get(i);

            if (agentSettings.isParallelizable()) {
                parallelBatch.add(agent);
            } else {
                // Flush any pending parallel batch
                if (!parallelBatch.isEmpty()) {
                    flushParallelBatch(pipeline, parallelBatch, settings.getName());
                    parallelBatch = new ArrayList<>();
                }
                pipeline.add(agent);
            }
        }

        // Flush remaining
        if (!parallelBatch.isEmpty()) {
            flushParallelBatch(pipeline, parallelBatch, settings.getName());
        }

        return pipeline;
    }

    private void flushParallelBatch(List<BaseAgent> pipeline, List<BaseAgent> batch, String flowName) {
        if (batch.size() == 1) {
            pipeline.add(batch.getFirst());
        } else {
            pipeline.add(ParallelAgent.builder()
                .name(flowName + "_parallel_group_" + pipeline.size())
                .subAgents(new ArrayList<>(batch))
                .build());
        }
    }
}
