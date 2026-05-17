package com.spectrayan.synaptiq.agentflow.spi;

import com.spectrayan.synaptiq.agentflow.builder.models.settings.FlowSettings;

/**
 * SPI for pluggable agent runtime backends.
 * <p>
 * Implementations compile declarative {@link FlowSettings} into an executable
 * {@link CompiledFlow}. The active provider is selected via Spring
 * auto-configuration ({@code @ConditionalOnClass} / {@code @ConditionalOnMissingBean}).
 * <p>
 * Built-in providers:
 * <ul>
 *   <li>{@code google-adk} — Google Agent Development Kit (default)</li>
 * </ul>
 * Future providers: {@code langgraph4j}, etc.
 */
public interface AgentFlowProvider {

    /**
     * Human-readable provider identifier, e.g. {@code "google-adk"}.
     */
    String name();

    /**
     * Compile the declarative flow settings into a ready-to-run flow.
     * This is where provider-specific agent/graph construction happens.
     *
     * @param settings the parsed flow definition
     * @return an executable compiled flow
     */
    CompiledFlow compile(FlowSettings settings);
}
