package com.spectrayan.synaptiq.workflow.infrastructure.config;

import com.spectrayan.synaptiq.agentflow.config.AgentFlowAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Workflow module configuration.
 * <p>
 * Imports {@link AgentFlowAutoConfiguration} which provides
 * {@code FlowBuilder}, {@code FlowExecutor}, and the active
 * {@code AgentFlowProvider} beans.
 */
@Configuration
@Import(AgentFlowAutoConfiguration.class)
public class WorkflowConfig {
    // FlowExecutor, FlowBuilder, and AgentFlowProvider beans are
    // provided by AgentFlowAutoConfiguration
}
