package com.spectrayan.synaptiq.workflow.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model for a tool available in the platform tool registry.
 * These are the tools agents can use during workflow execution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolDefinition {
    private String id;
    private String name;
    private String description;
    private String category;
    private String icon;
}
