package com.spectrayan.synaptiq.agentflow.provider.adk;

import com.spectrayan.synaptiq.agentflow.builder.models.settings.ToolSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AdkToolFactory}.
 */
class AdkToolFactoryTest {

    @Test
    void createTool_mcpThrowsUnsupported() {
        ToolSettings settings = ToolSettings.builder()
            .type(ToolSettings.ToolType.MCP)
            .name("mcp-tool")
            .build();

        assertThrows(UnsupportedOperationException.class,
            () -> AdkToolFactory.createTool(settings, null));
    }

    @Test
    void createTool_pythonThrowsUnsupported() {
        ToolSettings settings = ToolSettings.builder()
            .type(ToolSettings.ToolType.PYTHON)
            .name("python-tool")
            .build();

        assertThrows(UnsupportedOperationException.class,
            () -> AdkToolFactory.createTool(settings, null));
    }

    @Test
    void createTool_langchainThrowsUnsupported() {
        ToolSettings settings = ToolSettings.builder()
            .type(ToolSettings.ToolType.LANGCHAIN)
            .name("langchain-tool")
            .build();

        assertThrows(UnsupportedOperationException.class,
            () -> AdkToolFactory.createTool(settings, null));
    }

    @Test
    void createTool_javaMissingImportPath() {
        ToolSettings settings = ToolSettings.builder()
            .type(ToolSettings.ToolType.JAVA)
            .name("java-tool")
            .build();

        assertThrows(IllegalArgumentException.class,
            () -> AdkToolFactory.createTool(settings, null));
    }

    @Test
    void createTool_springBeanNullContext() {
        ToolSettings settings = ToolSettings.builder()
            .type(ToolSettings.ToolType.SPRING_BEAN)
            .name("bean-tool")
            .importPath("com.example.SomeBean")
            .build();

        assertThrows(IllegalStateException.class,
            () -> AdkToolFactory.createTool(settings, null));
    }

    @Test
    void resolveMcpToolsets_emptyList() {
        var result = AdkToolFactory.resolveMcpToolsets(java.util.List.of());
        assertTrue(result.isEmpty());
    }
}
