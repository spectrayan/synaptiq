package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.synaptiq.infrastructure.in.web.dto.WorkflowRunNodeDetail;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Detailed result of a workflow execution run including per-node results
 */

@Schema(name = "WorkflowRunDetail", description = "Detailed result of a workflow execution run including per-node results")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-05T21:15:25.464614100-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class WorkflowRunDetail {

  private String runId;

  private String workflowId;

  /**
   * Gets or Sets status
   */
  public enum StatusEnum {
    RUNNING("RUNNING"),
    
    COMPLETED("COMPLETED"),
    
    ERROR("ERROR"),
    
    CANCELLED("CANCELLED");

    private final String value;

    StatusEnum(String value) {
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
    public static StatusEnum fromValue(String value) {
      for (StatusEnum b : StatusEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private StatusEnum status;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime startedAt;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime completedAt;

  private @Nullable String currentNode;

  private @Nullable Long totalDurationMs;

  private @Nullable String result;

  @Valid
  private Map<String, WorkflowRunNodeDetail> nodes = new HashMap<>();

  public WorkflowRunDetail() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public WorkflowRunDetail(String runId, String workflowId, StatusEnum status, OffsetDateTime startedAt) {
    this.runId = runId;
    this.workflowId = workflowId;
    this.status = status;
    this.startedAt = startedAt;
  }

  public WorkflowRunDetail runId(String runId) {
    this.runId = runId;
    return this;
  }

  /**
   * Get runId
   * @return runId
   */
  @NotNull 
  @Schema(name = "runId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("runId")
  public String getRunId() {
    return runId;
  }

  @JsonProperty("runId")
  public void setRunId(String runId) {
    this.runId = runId;
  }

  public WorkflowRunDetail workflowId(String workflowId) {
    this.workflowId = workflowId;
    return this;
  }

  /**
   * Get workflowId
   * @return workflowId
   */
  @NotNull 
  @Schema(name = "workflowId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("workflowId")
  public String getWorkflowId() {
    return workflowId;
  }

  @JsonProperty("workflowId")
  public void setWorkflowId(String workflowId) {
    this.workflowId = workflowId;
  }

  public WorkflowRunDetail status(StatusEnum status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
   */
  @NotNull 
  @Schema(name = "status", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  public WorkflowRunDetail startedAt(OffsetDateTime startedAt) {
    this.startedAt = startedAt;
    return this;
  }

  /**
   * Get startedAt
   * @return startedAt
   */
  @NotNull @Valid 
  @Schema(name = "startedAt", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("startedAt")
  public OffsetDateTime getStartedAt() {
    return startedAt;
  }

  @JsonProperty("startedAt")
  public void setStartedAt(OffsetDateTime startedAt) {
    this.startedAt = startedAt;
  }

  public WorkflowRunDetail completedAt(@Nullable OffsetDateTime completedAt) {
    this.completedAt = completedAt;
    return this;
  }

  /**
   * Get completedAt
   * @return completedAt
   */
  @Valid 
  @Schema(name = "completedAt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("completedAt")
  public @Nullable OffsetDateTime getCompletedAt() {
    return completedAt;
  }

  @JsonProperty("completedAt")
  public void setCompletedAt(@Nullable OffsetDateTime completedAt) {
    this.completedAt = completedAt;
  }

  public WorkflowRunDetail currentNode(@Nullable String currentNode) {
    this.currentNode = currentNode;
    return this;
  }

  /**
   * Currently executing node ID (when status is RUNNING)
   * @return currentNode
   */
  
  @Schema(name = "currentNode", description = "Currently executing node ID (when status is RUNNING)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("currentNode")
  public @Nullable String getCurrentNode() {
    return currentNode;
  }

  @JsonProperty("currentNode")
  public void setCurrentNode(@Nullable String currentNode) {
    this.currentNode = currentNode;
  }

  public WorkflowRunDetail totalDurationMs(@Nullable Long totalDurationMs) {
    this.totalDurationMs = totalDurationMs;
    return this;
  }

  /**
   * Get totalDurationMs
   * @return totalDurationMs
   */
  
  @Schema(name = "totalDurationMs", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalDurationMs")
  public @Nullable Long getTotalDurationMs() {
    return totalDurationMs;
  }

  @JsonProperty("totalDurationMs")
  public void setTotalDurationMs(@Nullable Long totalDurationMs) {
    this.totalDurationMs = totalDurationMs;
  }

  public WorkflowRunDetail result(@Nullable String result) {
    this.result = result;
    return this;
  }

  /**
   * Final output of the workflow execution
   * @return result
   */
  
  @Schema(name = "result", description = "Final output of the workflow execution", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("result")
  public @Nullable String getResult() {
    return result;
  }

  @JsonProperty("result")
  public void setResult(@Nullable String result) {
    this.result = result;
  }

  public WorkflowRunDetail nodes(Map<String, WorkflowRunNodeDetail> nodes) {
    this.nodes = nodes;
    return this;
  }

  public WorkflowRunDetail putNodesItem(String key, WorkflowRunNodeDetail nodesItem) {
    if (this.nodes == null) {
      this.nodes = new HashMap<>();
    }
    this.nodes.put(key, nodesItem);
    return this;
  }

  /**
   * Per-node execution details keyed by node ID
   * @return nodes
   */
  @Valid 
  @Schema(name = "nodes", description = "Per-node execution details keyed by node ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("nodes")
  public Map<String, WorkflowRunNodeDetail> getNodes() {
    return nodes;
  }

  @JsonProperty("nodes")
  public void setNodes(Map<String, WorkflowRunNodeDetail> nodes) {
    this.nodes = nodes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WorkflowRunDetail workflowRunDetail = (WorkflowRunDetail) o;
    return Objects.equals(this.runId, workflowRunDetail.runId) &&
        Objects.equals(this.workflowId, workflowRunDetail.workflowId) &&
        Objects.equals(this.status, workflowRunDetail.status) &&
        Objects.equals(this.startedAt, workflowRunDetail.startedAt) &&
        Objects.equals(this.completedAt, workflowRunDetail.completedAt) &&
        Objects.equals(this.currentNode, workflowRunDetail.currentNode) &&
        Objects.equals(this.totalDurationMs, workflowRunDetail.totalDurationMs) &&
        Objects.equals(this.result, workflowRunDetail.result) &&
        Objects.equals(this.nodes, workflowRunDetail.nodes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(runId, workflowId, status, startedAt, completedAt, currentNode, totalDurationMs, result, nodes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WorkflowRunDetail {\n");
    sb.append("    runId: ").append(toIndentedString(runId)).append("\n");
    sb.append("    workflowId: ").append(toIndentedString(workflowId)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    startedAt: ").append(toIndentedString(startedAt)).append("\n");
    sb.append("    completedAt: ").append(toIndentedString(completedAt)).append("\n");
    sb.append("    currentNode: ").append(toIndentedString(currentNode)).append("\n");
    sb.append("    totalDurationMs: ").append(toIndentedString(totalDurationMs)).append("\n");
    sb.append("    result: ").append(toIndentedString(result)).append("\n");
    sb.append("    nodes: ").append(toIndentedString(nodes)).append("\n");
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

