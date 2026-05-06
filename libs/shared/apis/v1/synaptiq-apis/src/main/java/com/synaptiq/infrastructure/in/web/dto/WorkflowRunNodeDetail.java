package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.synaptiq.infrastructure.in.web.dto.NodeExecutionStatus;
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
 * Execution details for a single node within a workflow run
 */

@Schema(name = "WorkflowRunNodeDetail", description = "Execution details for a single node within a workflow run")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-05T21:15:25.464614100-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class WorkflowRunNodeDetail {

  private @Nullable NodeExecutionStatus status;

  private @Nullable Long durationMs;

  private @Nullable String output;

  private @Nullable String error;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime startedAt;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime completedAt;

  public WorkflowRunNodeDetail status(@Nullable NodeExecutionStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
   */
  @Valid 
  @Schema(name = "status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public @Nullable NodeExecutionStatus getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(@Nullable NodeExecutionStatus status) {
    this.status = status;
  }

  public WorkflowRunNodeDetail durationMs(@Nullable Long durationMs) {
    this.durationMs = durationMs;
    return this;
  }

  /**
   * Execution duration in milliseconds
   * @return durationMs
   */
  
  @Schema(name = "durationMs", description = "Execution duration in milliseconds", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("durationMs")
  public @Nullable Long getDurationMs() {
    return durationMs;
  }

  @JsonProperty("durationMs")
  public void setDurationMs(@Nullable Long durationMs) {
    this.durationMs = durationMs;
  }

  public WorkflowRunNodeDetail output(@Nullable String output) {
    this.output = output;
    return this;
  }

  /**
   * Node output text
   * @return output
   */
  
  @Schema(name = "output", description = "Node output text", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("output")
  public @Nullable String getOutput() {
    return output;
  }

  @JsonProperty("output")
  public void setOutput(@Nullable String output) {
    this.output = output;
  }

  public WorkflowRunNodeDetail error(@Nullable String error) {
    this.error = error;
    return this;
  }

  /**
   * Error message if the node failed
   * @return error
   */
  
  @Schema(name = "error", description = "Error message if the node failed", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("error")
  public @Nullable String getError() {
    return error;
  }

  @JsonProperty("error")
  public void setError(@Nullable String error) {
    this.error = error;
  }

  public WorkflowRunNodeDetail startedAt(@Nullable OffsetDateTime startedAt) {
    this.startedAt = startedAt;
    return this;
  }

  /**
   * Get startedAt
   * @return startedAt
   */
  @Valid 
  @Schema(name = "startedAt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("startedAt")
  public @Nullable OffsetDateTime getStartedAt() {
    return startedAt;
  }

  @JsonProperty("startedAt")
  public void setStartedAt(@Nullable OffsetDateTime startedAt) {
    this.startedAt = startedAt;
  }

  public WorkflowRunNodeDetail completedAt(@Nullable OffsetDateTime completedAt) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WorkflowRunNodeDetail workflowRunNodeDetail = (WorkflowRunNodeDetail) o;
    return Objects.equals(this.status, workflowRunNodeDetail.status) &&
        Objects.equals(this.durationMs, workflowRunNodeDetail.durationMs) &&
        Objects.equals(this.output, workflowRunNodeDetail.output) &&
        Objects.equals(this.error, workflowRunNodeDetail.error) &&
        Objects.equals(this.startedAt, workflowRunNodeDetail.startedAt) &&
        Objects.equals(this.completedAt, workflowRunNodeDetail.completedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, durationMs, output, error, startedAt, completedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WorkflowRunNodeDetail {\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    durationMs: ").append(toIndentedString(durationMs)).append("\n");
    sb.append("    output: ").append(toIndentedString(output)).append("\n");
    sb.append("    error: ").append(toIndentedString(error)).append("\n");
    sb.append("    startedAt: ").append(toIndentedString(startedAt)).append("\n");
    sb.append("    completedAt: ").append(toIndentedString(completedAt)).append("\n");
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

