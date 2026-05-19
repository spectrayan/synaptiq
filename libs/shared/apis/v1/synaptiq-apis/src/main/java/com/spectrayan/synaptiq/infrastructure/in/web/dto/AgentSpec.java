package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.LLMSpec;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.ToolSpec;
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
 * Agent node configuration within a workflow
 */

@Schema(name = "AgentSpec", description = "Agent node configuration within a workflow")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-18T21:07:32.055501800-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class AgentSpec {

  private String id;

  private String name;

  private @Nullable String systemPrompt;

  private @Nullable String description;

  private @Nullable String instructions;

  private @Nullable LLMSpec llm;

  @Valid
  private List<@Valid ToolSpec> tools = new ArrayList<>();

  public AgentSpec() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public AgentSpec(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public AgentSpec id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
   */
  @NotNull 
  @Schema(name = "id", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  public AgentSpec name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
   */
  @NotNull 
  @Schema(name = "name", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  public AgentSpec systemPrompt(@Nullable String systemPrompt) {
    this.systemPrompt = systemPrompt;
    return this;
  }

  /**
   * Get systemPrompt
   * @return systemPrompt
   */
  
  @Schema(name = "systemPrompt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("systemPrompt")
  public @Nullable String getSystemPrompt() {
    return systemPrompt;
  }

  @JsonProperty("systemPrompt")
  public void setSystemPrompt(@Nullable String systemPrompt) {
    this.systemPrompt = systemPrompt;
  }

  public AgentSpec description(@Nullable String description) {
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

  public AgentSpec instructions(@Nullable String instructions) {
    this.instructions = instructions;
    return this;
  }

  /**
   * Get instructions
   * @return instructions
   */
  
  @Schema(name = "instructions", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("instructions")
  public @Nullable String getInstructions() {
    return instructions;
  }

  @JsonProperty("instructions")
  public void setInstructions(@Nullable String instructions) {
    this.instructions = instructions;
  }

  public AgentSpec llm(@Nullable LLMSpec llm) {
    this.llm = llm;
    return this;
  }

  /**
   * Get llm
   * @return llm
   */
  @Valid 
  @Schema(name = "llm", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("llm")
  public @Nullable LLMSpec getLlm() {
    return llm;
  }

  @JsonProperty("llm")
  public void setLlm(@Nullable LLMSpec llm) {
    this.llm = llm;
  }

  public AgentSpec tools(List<@Valid ToolSpec> tools) {
    this.tools = tools;
    return this;
  }

  public AgentSpec addToolsItem(ToolSpec toolsItem) {
    if (this.tools == null) {
      this.tools = new ArrayList<>();
    }
    this.tools.add(toolsItem);
    return this;
  }

  /**
   * Get tools
   * @return tools
   */
  @Valid 
  @Schema(name = "tools", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("tools")
  public List<@Valid ToolSpec> getTools() {
    return tools;
  }

  @JsonProperty("tools")
  public void setTools(List<@Valid ToolSpec> tools) {
    this.tools = tools;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentSpec agentSpec = (AgentSpec) o;
    return Objects.equals(this.id, agentSpec.id) &&
        Objects.equals(this.name, agentSpec.name) &&
        Objects.equals(this.systemPrompt, agentSpec.systemPrompt) &&
        Objects.equals(this.description, agentSpec.description) &&
        Objects.equals(this.instructions, agentSpec.instructions) &&
        Objects.equals(this.llm, agentSpec.llm) &&
        Objects.equals(this.tools, agentSpec.tools);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, systemPrompt, description, instructions, llm, tools);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentSpec {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    systemPrompt: ").append(toIndentedString(systemPrompt)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    instructions: ").append(toIndentedString(instructions)).append("\n");
    sb.append("    llm: ").append(toIndentedString(llm)).append("\n");
    sb.append("    tools: ").append(toIndentedString(tools)).append("\n");
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

