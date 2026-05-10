package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.synaptiq.infrastructure.in.web.dto.ResourceLimitsSpec;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Execution policy for workflow runs
 */

@Schema(name = "ExecutionPolicySpec", description = "Execution policy for workflow runs")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-08T22:14:16.718368-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ExecutionPolicySpec {

  private Boolean deterministic = false;

  private @Nullable Integer seed;

  private @Nullable ResourceLimitsSpec resources;

  public ExecutionPolicySpec deterministic(Boolean deterministic) {
    this.deterministic = deterministic;
    return this;
  }

  /**
   * Get deterministic
   * @return deterministic
   */
  
  @Schema(name = "deterministic", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("deterministic")
  public Boolean getDeterministic() {
    return deterministic;
  }

  @JsonProperty("deterministic")
  public void setDeterministic(Boolean deterministic) {
    this.deterministic = deterministic;
  }

  public ExecutionPolicySpec seed(@Nullable Integer seed) {
    this.seed = seed;
    return this;
  }

  /**
   * Get seed
   * @return seed
   */
  
  @Schema(name = "seed", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("seed")
  public @Nullable Integer getSeed() {
    return seed;
  }

  @JsonProperty("seed")
  public void setSeed(@Nullable Integer seed) {
    this.seed = seed;
  }

  public ExecutionPolicySpec resources(@Nullable ResourceLimitsSpec resources) {
    this.resources = resources;
    return this;
  }

  /**
   * Get resources
   * @return resources
   */
  @Valid 
  @Schema(name = "resources", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("resources")
  public @Nullable ResourceLimitsSpec getResources() {
    return resources;
  }

  @JsonProperty("resources")
  public void setResources(@Nullable ResourceLimitsSpec resources) {
    this.resources = resources;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExecutionPolicySpec executionPolicySpec = (ExecutionPolicySpec) o;
    return Objects.equals(this.deterministic, executionPolicySpec.deterministic) &&
        Objects.equals(this.seed, executionPolicySpec.seed) &&
        Objects.equals(this.resources, executionPolicySpec.resources);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deterministic, seed, resources);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExecutionPolicySpec {\n");
    sb.append("    deterministic: ").append(toIndentedString(deterministic)).append("\n");
    sb.append("    seed: ").append(toIndentedString(seed)).append("\n");
    sb.append("    resources: ").append(toIndentedString(resources)).append("\n");
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

