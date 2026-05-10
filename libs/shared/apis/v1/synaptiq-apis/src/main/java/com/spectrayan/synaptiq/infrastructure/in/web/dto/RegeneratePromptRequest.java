package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Request body for AI-powered prompt regeneration
 */

@Schema(name = "RegeneratePromptRequest", description = "Request body for AI-powered prompt regeneration")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-10T17:15:52.297398600-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class RegeneratePromptRequest {

  private String nodeId;

  private @Nullable String nodeLabel;

  private @Nullable String nodeDescription;

  private @Nullable String currentPrompt;

  private @Nullable String instruction;

  public RegeneratePromptRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public RegeneratePromptRequest(String nodeId) {
    this.nodeId = nodeId;
  }

  public RegeneratePromptRequest nodeId(String nodeId) {
    this.nodeId = nodeId;
    return this;
  }

  /**
   * Get nodeId
   * @return nodeId
   */
  @NotNull 
  @Schema(name = "nodeId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("nodeId")
  public String getNodeId() {
    return nodeId;
  }

  @JsonProperty("nodeId")
  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  public RegeneratePromptRequest nodeLabel(@Nullable String nodeLabel) {
    this.nodeLabel = nodeLabel;
    return this;
  }

  /**
   * Get nodeLabel
   * @return nodeLabel
   */
  
  @Schema(name = "nodeLabel", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("nodeLabel")
  public @Nullable String getNodeLabel() {
    return nodeLabel;
  }

  @JsonProperty("nodeLabel")
  public void setNodeLabel(@Nullable String nodeLabel) {
    this.nodeLabel = nodeLabel;
  }

  public RegeneratePromptRequest nodeDescription(@Nullable String nodeDescription) {
    this.nodeDescription = nodeDescription;
    return this;
  }

  /**
   * Get nodeDescription
   * @return nodeDescription
   */
  
  @Schema(name = "nodeDescription", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("nodeDescription")
  public @Nullable String getNodeDescription() {
    return nodeDescription;
  }

  @JsonProperty("nodeDescription")
  public void setNodeDescription(@Nullable String nodeDescription) {
    this.nodeDescription = nodeDescription;
  }

  public RegeneratePromptRequest currentPrompt(@Nullable String currentPrompt) {
    this.currentPrompt = currentPrompt;
    return this;
  }

  /**
   * Get currentPrompt
   * @return currentPrompt
   */
  
  @Schema(name = "currentPrompt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("currentPrompt")
  public @Nullable String getCurrentPrompt() {
    return currentPrompt;
  }

  @JsonProperty("currentPrompt")
  public void setCurrentPrompt(@Nullable String currentPrompt) {
    this.currentPrompt = currentPrompt;
  }

  public RegeneratePromptRequest instruction(@Nullable String instruction) {
    this.instruction = instruction;
    return this;
  }

  /**
   * User instruction for how the prompt should be regenerated
   * @return instruction
   */
  
  @Schema(name = "instruction", description = "User instruction for how the prompt should be regenerated", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("instruction")
  public @Nullable String getInstruction() {
    return instruction;
  }

  @JsonProperty("instruction")
  public void setInstruction(@Nullable String instruction) {
    this.instruction = instruction;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RegeneratePromptRequest regeneratePromptRequest = (RegeneratePromptRequest) o;
    return Objects.equals(this.nodeId, regeneratePromptRequest.nodeId) &&
        Objects.equals(this.nodeLabel, regeneratePromptRequest.nodeLabel) &&
        Objects.equals(this.nodeDescription, regeneratePromptRequest.nodeDescription) &&
        Objects.equals(this.currentPrompt, regeneratePromptRequest.currentPrompt) &&
        Objects.equals(this.instruction, regeneratePromptRequest.instruction);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nodeId, nodeLabel, nodeDescription, currentPrompt, instruction);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RegeneratePromptRequest {\n");
    sb.append("    nodeId: ").append(toIndentedString(nodeId)).append("\n");
    sb.append("    nodeLabel: ").append(toIndentedString(nodeLabel)).append("\n");
    sb.append("    nodeDescription: ").append(toIndentedString(nodeDescription)).append("\n");
    sb.append("    currentPrompt: ").append(toIndentedString(currentPrompt)).append("\n");
    sb.append("    instruction: ").append(toIndentedString(instruction)).append("\n");
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

