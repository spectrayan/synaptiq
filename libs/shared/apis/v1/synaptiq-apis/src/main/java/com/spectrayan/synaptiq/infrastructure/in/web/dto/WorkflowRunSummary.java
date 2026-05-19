package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Summary of a workflow execution run
 */

@Schema(name = "WorkflowRunSummary", description = "Summary of a workflow execution run")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-18T21:07:32.055501800-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class WorkflowRunSummary {

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

  private @Nullable Long durationMs;

  private @Nullable Long totalDurationMs;

  public WorkflowRunSummary() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public WorkflowRunSummary(String runId, String workflowId, StatusEnum status, OffsetDateTime startedAt) {
    this.runId = runId;
    this.workflowId = workflowId;
    this.status = status;
    this.startedAt = startedAt;
  }

  public WorkflowRunSummary runId(String runId) {
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

  public WorkflowRunSummary workflowId(String workflowId) {
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

  public WorkflowRunSummary status(StatusEnum status) {
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

  public WorkflowRunSummary startedAt(OffsetDateTime startedAt) {
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

  public WorkflowRunSummary completedAt(@Nullable OffsetDateTime completedAt) {
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

  public WorkflowRunSummary durationMs(@Nullable Long durationMs) {
    this.durationMs = durationMs;
    return this;
  }

  /**
   * Duration of the most recent node execution
   * @return durationMs
   */
  
  @Schema(name = "durationMs", description = "Duration of the most recent node execution", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("durationMs")
  public @Nullable Long getDurationMs() {
    return durationMs;
  }

  @JsonProperty("durationMs")
  public void setDurationMs(@Nullable Long durationMs) {
    this.durationMs = durationMs;
  }

  public WorkflowRunSummary totalDurationMs(@Nullable Long totalDurationMs) {
    this.totalDurationMs = totalDurationMs;
    return this;
  }

  /**
   * Total duration of the entire run
   * @return totalDurationMs
   */
  
  @Schema(name = "totalDurationMs", description = "Total duration of the entire run", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalDurationMs")
  public @Nullable Long getTotalDurationMs() {
    return totalDurationMs;
  }

  @JsonProperty("totalDurationMs")
  public void setTotalDurationMs(@Nullable Long totalDurationMs) {
    this.totalDurationMs = totalDurationMs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WorkflowRunSummary workflowRunSummary = (WorkflowRunSummary) o;
    return Objects.equals(this.runId, workflowRunSummary.runId) &&
        Objects.equals(this.workflowId, workflowRunSummary.workflowId) &&
        Objects.equals(this.status, workflowRunSummary.status) &&
        Objects.equals(this.startedAt, workflowRunSummary.startedAt) &&
        Objects.equals(this.completedAt, workflowRunSummary.completedAt) &&
        Objects.equals(this.durationMs, workflowRunSummary.durationMs) &&
        Objects.equals(this.totalDurationMs, workflowRunSummary.totalDurationMs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(runId, workflowId, status, startedAt, completedAt, durationMs, totalDurationMs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WorkflowRunSummary {\n");
    sb.append("    runId: ").append(toIndentedString(runId)).append("\n");
    sb.append("    workflowId: ").append(toIndentedString(workflowId)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    startedAt: ").append(toIndentedString(startedAt)).append("\n");
    sb.append("    completedAt: ").append(toIndentedString(completedAt)).append("\n");
    sb.append("    durationMs: ").append(toIndentedString(durationMs)).append("\n");
    sb.append("    totalDurationMs: ").append(toIndentedString(totalDurationMs)).append("\n");
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

