package com.spectrayan.synaptiq.agentflow.provider.adk;

import com.google.adk.tools.BaseTool;
import com.google.adk.tools.FunctionTool;
import com.google.adk.tools.mcp.McpToolset;
import com.google.adk.tools.mcp.SseServerParameters;
import com.spectrayan.synaptiq.agentflow.builder.models.settings.MCPServerConfig;
import com.spectrayan.synaptiq.agentflow.builder.models.settings.ToolSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory for mapping {@link ToolSettings} to Google ADK tool instances.
 * <p>
 * Supports:
 * <ul>
 *   <li>{@code JAVA} — wraps a Java method as a {@link FunctionTool}</li>
 *   <li>{@code SPRING_BEAN} — resolves a Spring bean via {@link ApplicationContext}</li>
 *   <li>{@code MCP} — ADK's native {@link McpToolset} (resolved as toolset at agent level)</li>
 * </ul>
 */
public final class AdkToolFactory {

    private static final Logger log = LoggerFactory.getLogger(AdkToolFactory.class);

    private AdkToolFactory() {
        // utility class
    }

    /**
     * Create an ADK tool from the given settings.
     *
     * @param settings   tool definition
     * @param appContext Spring application context (nullable, needed for SPRING_BEAN)
     * @return the ADK tool instance
     */
    public static Object createTool(ToolSettings settings, ApplicationContext appContext) {
        return switch (settings.getType()) {
            case JAVA -> createJavaFunctionTool(settings);
            case SPRING_BEAN -> createSpringBeanTool(settings, appContext);
            case MCP -> throw new UnsupportedOperationException(
                "MCP tools are resolved at the flow level via McpToolset, not individually."
            );
            case PYTHON, LANGCHAIN -> throw new UnsupportedOperationException(
                "Tool type not yet supported in Java ADK provider: " + settings.getType()
            );
        };
    }

    /**
     * Create {@link McpToolset} instances from the given MCP server configurations.
     * These are passed directly into the agent's tools list since ADK
     * handles toolset resolution internally.
     *
     * @param mcpServers MCP server configurations from the flow spec
     * @return list of McpToolset instances (each is a BaseToolset)
     */
    public static List<Object> resolveMcpToolsets(List<MCPServerConfig> mcpServers) {
        List<Object> toolsets = new ArrayList<>();

        for (MCPServerConfig server : mcpServers) {
            try {
                log.info("Connecting to MCP server: {} (transport: {})", server.getId(), server.getTransport());

                switch (server.getTransport()) {
                    case HTTP, WS -> {
                        SseServerParameters params = SseServerParameters.builder()
                            .url(server.getUrl())
                            .build();
                        McpToolset toolset = new McpToolset(params);
                        toolsets.add(toolset);
                        log.info("Created McpToolset for server '{}'", server.getId());
                    }
                    case STDIO -> {
                        log.warn("STDIO MCP transport is not yet fully supported in this version. "
                            + "Server '{}' will be skipped.", server.getId());
                    }
                }
            } catch (Exception e) {
                log.error("Failed to connect to MCP server '{}': {}", server.getId(), e.getMessage(), e);
            }
        }

        return toolsets;
    }

    /**
     * Create a FunctionTool by reflectively loading a Java class/method.
     */
    private static FunctionTool createJavaFunctionTool(ToolSettings settings) {
        log.debug("Creating Java FunctionTool: name={}, importPath={}", settings.getName(), settings.getImportPath());

        if (settings.getImportPath() == null || settings.getImportPath().isBlank()) {
            throw new IllegalArgumentException(
                "Tool '" + settings.getName() + "' of type JAVA requires 'importPath' to be set."
            );
        }

        try {
            Class<?> toolClass = Class.forName(settings.getImportPath());
            return FunctionTool.create(toolClass, settings.getName());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(
                "Cannot resolve Java tool '" + settings.getName() + "' from path: " + settings.getImportPath(), e
            );
        }
    }

    /**
     * Create a FunctionTool by resolving a Spring bean via {@link ApplicationContext}.
     */
    private static FunctionTool createSpringBeanTool(ToolSettings settings, ApplicationContext appContext) {
        log.debug("Creating Spring Bean tool: name={}, importPath={}", settings.getName(), settings.getImportPath());

        if (appContext == null) {
            throw new IllegalStateException(
                "Spring ApplicationContext is required for SPRING_BEAN tools but is not available."
            );
        }

        if (settings.getImportPath() == null || settings.getImportPath().isBlank()) {
            throw new IllegalArgumentException(
                "Tool '" + settings.getName() + "' of type SPRING_BEAN requires 'importPath' (bean class name) to be set."
            );
        }

        try {
            Class<?> beanClass = Class.forName(settings.getImportPath());
            Object bean = appContext.getBean(beanClass);
            return FunctionTool.create(bean, settings.getName());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(
                "Cannot resolve Spring bean tool '" + settings.getName() + "': " + e.getMessage(), e
            );
        }
    }
}
