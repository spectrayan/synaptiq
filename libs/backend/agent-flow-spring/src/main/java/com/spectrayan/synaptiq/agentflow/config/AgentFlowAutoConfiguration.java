package com.spectrayan.synaptiq.agentflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spectrayan.synaptiq.agentflow.builder.FlowBuilder;
import com.spectrayan.synaptiq.agentflow.executor.FlowExecutor;
import com.spectrayan.synaptiq.agentflow.provider.adk.AdkFlowProvider;
import com.spectrayan.synaptiq.agentflow.spi.AgentFlowProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot auto-configuration for the agent-flow engine.
 * <p>
 * Automatically wires the correct {@link AgentFlowProvider} based on
 * classpath detection:
 * <ul>
 *   <li>If {@code com.google.adk.agents.LlmAgent} is present → {@link AdkFlowProvider}</li>
 *   <li>Custom providers can be registered as beans — they take precedence
 *       via {@code @ConditionalOnMissingBean}</li>
 * </ul>
 */
@Configuration
public class AgentFlowAutoConfiguration {

    /**
     * Default provider: Google ADK (activates when ADK is on the classpath).
     * If a custom provider bean is already registered, this is skipped.
     */
    @Bean
    @ConditionalOnMissingBean(AgentFlowProvider.class)
    @ConditionalOnClass(name = "com.google.adk.agents.LlmAgent")
    public AgentFlowProvider adkFlowProvider(ObjectProvider<ApplicationContext> appCtx) {
        return new AdkFlowProvider(appCtx.getIfAvailable());
    }

    /**
     * Flow builder — delegates to the active provider.
     */
    @Bean
    @ConditionalOnMissingBean(FlowBuilder.class)
    public FlowBuilder flowBuilder(AgentFlowProvider provider, ObjectMapper objectMapper) {
        return new FlowBuilder(provider, objectMapper);
    }

    /**
     * Flow executor — manages run lifecycle.
     */
    @Bean
    @ConditionalOnMissingBean(FlowExecutor.class)
    public FlowExecutor flowExecutor(FlowBuilder flowBuilder) {
        return new FlowExecutor(flowBuilder);
    }
}
