package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.FlowSettingsSpec;
import java.util.HashMap;
import java.util.Map;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Request body for executing a workflow
 */

@Schema(name = "ExecuteWorkflowRequest", description = "Request body for executing a workflow")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-10T17:15:52.297398600-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ExecuteWorkflowRequest {

  private FlowSettingsSpec spec;

  private @Nullable String inputText;

  private Boolean dryRun = false;

  private @Nullable String startNodeId;

  private @Nullable String priorContext;

  @Valid
  private Map<String, Object> inputVariables = new HashMap<>();

  public ExecuteWorkflowRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ExecuteWorkflowRequest(FlowSettingsSpec spec) {
    this.spec = spec;
  }

  public ExecuteWorkflowRequest spec(FlowSettingsSpec spec) {
    this.spec = spec;
    return this;
  }

  /**
   * Get spec
   * @return spec
   */
  @NotNull @Valid 
  @Schema(name = "spec", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("spec")
  public FlowSettingsSpec getSpec() {
    return spec;
  }

  @JsonProperty("spec")
  public void setSpec(FlowSettingsSpec spec) {
    this.spec = spec;
  }

  public ExecuteWorkflowRequest inputText(@Nullable String inputText) {
    this.inputText = inputText;
    return this;
  }

  /**
   * Primary text input for the workflow
   * @return inputText
   */
  
  @Schema(name = "inputText", description = "Primary text input for the workflow", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("inputText")
  public @Nullable String getInputText() {
    return inputText;
  }

  @JsonProperty("inputText")
  public void setInputText(@Nullable String inputText) {
    this.inputText = inputText;
  }

  public ExecuteWorkflowRequest dryRun(Boolean dryRun) {
    this.dryRun = dryRun;
    return this;
  }

  /**
   * If true, validate the workflow without executing
   * @return dryRun
   */
  
  @Schema(name = "dryRun", description = "If true, validate the workflow without executing", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("dryRun")
  public Boolean getDryRun() {
    return dryRun;
  }

  @JsonProperty("dryRun")
  public void setDryRun(Boolean dryRun) {
    this.dryRun = dryRun;
  }

  public ExecuteWorkflowRequest startNodeId(@Nullable String startNodeId) {
    this.startNodeId = startNodeId;
    return this;
  }

  /**
   * Optional node ID to start execution from (for partial runs)
   * @return startNodeId
   */
  
  @Schema(name = "startNodeId", description = "Optional node ID to start execution from (for partial runs)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("startNodeId")
  public @Nullable String getStartNodeId() {
    return startNodeId;
  }

  @JsonProperty("startNodeId")
  public void setStartNodeId(@Nullable String startNodeId) {
    this.startNodeId = startNodeId;
  }

  public ExecuteWorkflowRequest priorContext(@Nullable String priorContext) {
    this.priorContext = priorContext;
    return this;
  }

  /**
   * Prior conversation context for the execution
   * @return priorContext
   */
  
  @Schema(name = "priorContext", description = "Prior conversation context for the execution", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("priorContext")
  public @Nullable String getPriorContext() {
    return priorContext;
  }

  @JsonProperty("priorContext")
  public void setPriorContext(@Nullable String priorContext) {
    this.priorContext = priorContext;
  }

  public ExecuteWorkflowRequest inputVariables(Map<String, Object> inputVariables) {
    this.inputVariables = inputVariables;
    return this;
  }

  public ExecuteWorkflowRequest putInputVariablesItem(String key, Object inputVariablesItem) {
    if (this.inputVariables == null) {
      this.inputVariables = new HashMap<>();
    }
    this.inputVariables.put(key, inputVariablesItem);
    return this;
  }

  /**
   * Key-value map of input variables for parameterized execution
   * @return inputVariables
   */
  
  @Schema(name = "inputVariables", description = "Key-value map of input variables for parameterized execution", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("inputVariables")
  public Map<String, Object> getInputVariables() {
    return inputVariables;
  }

  @JsonProperty("inputVariables")
  public void setInputVariables(Map<String, Object> inputVariables) {
    this.inputVariables = inputVariables;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExecuteWorkflowRequest executeWorkflowRequest = (ExecuteWorkflowRequest) o;
    return Objects.equals(this.spec, executeWorkflowRequest.spec) &&
        Objects.equals(this.inputText, executeWorkflowRequest.inputText) &&
        Objects.equals(this.dryRun, executeWorkflowRequest.dryRun) &&
        Objects.equals(this.startNodeId, executeWorkflowRequest.startNodeId) &&
        Objects.equals(this.priorContext, executeWorkflowRequest.priorContext) &&
        Objects.equals(this.inputVariables, executeWorkflowRequest.inputVariables);
  }

  @Override
  public int hashCode() {
    return Objects.hash(spec, inputText, dryRun, startNodeId, priorContext, inputVariables);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExecuteWorkflowRequest {\n");
    sb.append("    spec: ").append(toIndentedString(spec)).append("\n");
    sb.append("    inputText: ").append(toIndentedString(inputText)).append("\n");
    sb.append("    dryRun: ").append(toIndentedString(dryRun)).append("\n");
    sb.append("    startNodeId: ").append(toIndentedString(startNodeId)).append("\n");
    sb.append("    priorContext: ").append(toIndentedString(priorContext)).append("\n");
    sb.append("    inputVariables: ").append(toIndentedString(inputVariables)).append("\n");
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

