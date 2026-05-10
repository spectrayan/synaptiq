package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.synaptiq.infrastructure.in.web.dto.AgentSpec;
import com.synaptiq.infrastructure.in.web.dto.EdgeSpec;
import com.synaptiq.infrastructure.in.web.dto.ExecutionPolicySpec;
import com.synaptiq.infrastructure.in.web.dto.MCPServerSpec;
import com.synaptiq.infrastructure.in.web.dto.WorkflowInput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Flow specification for a workflow
 */

@Schema(name = "FlowSettingsSpec", description = "Flow specification for a workflow")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-08T22:14:16.718368-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class FlowSettingsSpec {

  private @Nullable String id;

  private @Nullable String name;

  private @Nullable String description;

  private @Nullable String entrypoint;

  @Valid
  private List<@Valid AgentSpec> agents = new ArrayList<>();

  @Valid
  private List<@Valid EdgeSpec> edges = new ArrayList<>();

  @Valid
  private List<@Valid MCPServerSpec> mcpServers = new ArrayList<>();

  private @Nullable ExecutionPolicySpec policy;

  /**
   * Gets or Sets flowType
   */
  public enum FlowTypeEnum {
    STATIC("STATIC"),
    
    DYNAMIC("DYNAMIC"),
    
    HYBRID("HYBRID");

    private final String value;

    FlowTypeEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static FlowTypeEnum fromValue(String value) {
      for (FlowTypeEnum b : FlowTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private @Nullable FlowTypeEnum flowType;

  @Valid
  private List<@Valid WorkflowInput> inputs = new ArrayList<>();

  public FlowSettingsSpec id(@Nullable String id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
   */
  
  @Schema(name = "id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("id")
  public @Nullable String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(@Nullable String id) {
    this.id = id;
  }

  public FlowSettingsSpec name(@Nullable String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
   */
  
  @Schema(name = "name", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("name")
  public @Nullable String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(@Nullable String name) {
    this.name = name;
  }

  public FlowSettingsSpec description(@Nullable String description) {
    this.description = description;
    return this;
  }

  /**
   * Get description
   * @return description
   */
  
  @Schema(name = "description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("description")
  public @Nullable String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(@Nullable String description) {
    this.description = description;
  }

  public FlowSettingsSpec entrypoint(@Nullable String entrypoint) {
    this.entrypoint = entrypoint;
    return this;
  }

  /**
   * Get entrypoint
   * @return entrypoint
   */
  
  @Schema(name = "entrypoint", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("entrypoint")
  public @Nullable String getEntrypoint() {
    return entrypoint;
  }

  @JsonProperty("entrypoint")
  public void setEntrypoint(@Nullable String entrypoint) {
    this.entrypoint = entrypoint;
  }

  public FlowSettingsSpec agents(List<@Valid AgentSpec> agents) {
    this.agents = agents;
    return this;
  }

  public FlowSettingsSpec addAgentsItem(AgentSpec agentsItem) {
    if (this.agents == null) {
      this.agents = new ArrayList<>();
    }
    this.agents.add(agentsItem);
    return this;
  }

  /**
   * Get agents
   * @return agents
   */
  @Valid 
  @Schema(name = "agents", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("agents")
  public List<@Valid AgentSpec> getAgents() {
    return agents;
  }

  @JsonProperty("agents")
  public void setAgents(List<@Valid AgentSpec> agents) {
    this.agents = agents;
  }

  public FlowSettingsSpec edges(List<@Valid EdgeSpec> edges) {
    this.edges = edges;
    return this;
  }

  public FlowSettingsSpec addEdgesItem(EdgeSpec edgesItem) {
    if (this.edges == null) {
      this.edges = new ArrayList<>();
    }
    this.edges.add(edgesItem);
    return this;
  }

  /**
   * Get edges
   * @return edges
   */
  @Valid 
  @Schema(name = "edges", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("edges")
  public List<@Valid EdgeSpec> getEdges() {
    return edges;
  }

  @JsonProperty("edges")
  public void setEdges(List<@Valid EdgeSpec> edges) {
    this.edges = edges;
  }

  public FlowSettingsSpec mcpServers(List<@Valid MCPServerSpec> mcpServers) {
    this.mcpServers = mcpServers;
    return this;
  }

  public FlowSettingsSpec addMcpServersItem(MCPServerSpec mcpServersItem) {
    if (this.mcpServers == null) {
      this.mcpServers = new ArrayList<>();
    }
    this.mcpServers.add(mcpServersItem);
    return this;
  }

  /**
   * Get mcpServers
   * @return mcpServers
   */
  @Valid 
  @Schema(name = "mcpServers", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("mcpServers")
  public List<@Valid MCPServerSpec> getMcpServers() {
    return mcpServers;
  }

  @JsonProperty("mcpServers")
  public void setMcpServers(List<@Valid MCPServerSpec> mcpServers) {
    this.mcpServers = mcpServers;
  }

  public FlowSettingsSpec policy(@Nullable ExecutionPolicySpec policy) {
    this.policy = policy;
    return this;
  }

  /**
   * Get policy
   * @return policy
   */
  @Valid 
  @Schema(name = "policy", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("policy")
  public @Nullable ExecutionPolicySpec getPolicy() {
    return policy;
  }

  @JsonProperty("policy")
  public void setPolicy(@Nullable ExecutionPolicySpec policy) {
    this.policy = policy;
  }

  public FlowSettingsSpec flowType(@Nullable FlowTypeEnum flowType) {
    this.flowType = flowType;
    return this;
  }

  /**
   * Get flowType
   * @return flowType
   */
  
  @Schema(name = "flowType", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("flowType")
  public @Nullable FlowTypeEnum getFlowType() {
    return flowType;
  }

  @JsonProperty("flowType")
  public void setFlowType(@Nullable FlowTypeEnum flowType) {
    this.flowType = flowType;
  }

  public FlowSettingsSpec inputs(List<@Valid WorkflowInput> inputs) {
    this.inputs = inputs;
    return this;
  }

  public FlowSettingsSpec addInputsItem(WorkflowInput inputsItem) {
    if (this.inputs == null) {
      this.inputs = new ArrayList<>();
    }
    this.inputs.add(inputsItem);
    return this;
  }

  /**
   * Input fields for parameterized workflow execution
   * @return inputs
   */
  @Valid 
  @Schema(name = "inputs", description = "Input fields for parameterized workflow execution", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("inputs")
  public List<@Valid WorkflowInput> getInputs() {
    return inputs;
  }

  @JsonProperty("inputs")
  public void setInputs(List<@Valid WorkflowInput> inputs) {
    this.inputs = inputs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FlowSettingsSpec flowSettingsSpec = (FlowSettingsSpec) o;
    return Objects.equals(this.id, flowSettingsSpec.id) &&
        Objects.equals(this.name, flowSettingsSpec.name) &&
        Objects.equals(this.description, flowSettingsSpec.description) &&
        Objects.equals(this.entrypoint, flowSettingsSpec.entrypoint) &&
        Objects.equals(this.agents, flowSettingsSpec.agents) &&
        Objects.equals(this.edges, flowSettingsSpec.edges) &&
        Objects.equals(this.mcpServers, flowSettingsSpec.mcpServers) &&
        Objects.equals(this.policy, flowSettingsSpec.policy) &&
        Objects.equals(this.flowType, flowSettingsSpec.flowType) &&
        Objects.equals(this.inputs, flowSettingsSpec.inputs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, entrypoint, agents, edges, mcpServers, policy, flowType, inputs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FlowSettingsSpec {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    entrypoint: ").append(toIndentedString(entrypoint)).append("\n");
    sb.append("    agents: ").append(toIndentedString(agents)).append("\n");
    sb.append("    edges: ").append(toIndentedString(edges)).append("\n");
    sb.append("    mcpServers: ").append(toIndentedString(mcpServers)).append("\n");
    sb.append("    policy: ").append(toIndentedString(policy)).append("\n");
    sb.append("    flowType: ").append(toIndentedString(flowType)).append("\n");
    sb.append("    inputs: ").append(toIndentedString(inputs)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(@Nullable Object o) {
    return o == null ? "null" : o.toString().replace("\n", "\n    ");
  }
}

