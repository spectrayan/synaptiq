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
 * Resource limits for workflow execution
 */

@Schema(name = "ResourceLimitsSpec", description = "Resource limits for workflow execution")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-10T17:15:52.297398600-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ResourceLimitsSpec {

  private @Nullable Integer totalTimeoutMs;

  private @Nullable Integer stepTimeoutMs;

  private @Nullable Integer maxTokens;

  private @Nullable Double rateLimitRps;

  public ResourceLimitsSpec totalTimeoutMs(@Nullable Integer totalTimeoutMs) {
    this.totalTimeoutMs = totalTimeoutMs;
    return this;
  }

  /**
   * Get totalTimeoutMs
   * @return totalTimeoutMs
   */
  
  @Schema(name = "totalTimeoutMs", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalTimeoutMs")
  public @Nullable Integer getTotalTimeoutMs() {
    return totalTimeoutMs;
  }

  @JsonProperty("totalTimeoutMs")
  public void setTotalTimeoutMs(@Nullable Integer totalTimeoutMs) {
    this.totalTimeoutMs = totalTimeoutMs;
  }

  public ResourceLimitsSpec stepTimeoutMs(@Nullable Integer stepTimeoutMs) {
    this.stepTimeoutMs = stepTimeoutMs;
    return this;
  }

  /**
   * Get stepTimeoutMs
   * @return stepTimeoutMs
   */
  
  @Schema(name = "stepTimeoutMs", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("stepTimeoutMs")
  public @Nullable Integer getStepTimeoutMs() {
    return stepTimeoutMs;
  }

  @JsonProperty("stepTimeoutMs")
  public void setStepTimeoutMs(@Nullable Integer stepTimeoutMs) {
    this.stepTimeoutMs = stepTimeoutMs;
  }

  public ResourceLimitsSpec maxTokens(@Nullable Integer maxTokens) {
    this.maxTokens = maxTokens;
    return this;
  }

  /**
   * Get maxTokens
   * @return maxTokens
   */
  
  @Schema(name = "maxTokens", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("maxTokens")
  public @Nullable Integer getMaxTokens() {
    return maxTokens;
  }

  @JsonProperty("maxTokens")
  public void setMaxTokens(@Nullable Integer maxTokens) {
    this.maxTokens = maxTokens;
  }

  public ResourceLimitsSpec rateLimitRps(@Nullable Double rateLimitRps) {
    this.rateLimitRps = rateLimitRps;
    return this;
  }

  /**
   * Get rateLimitRps
   * @return rateLimitRps
   */
  
  @Schema(name = "rateLimitRps", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("rateLimitRps")
  public @Nullable Double getRateLimitRps() {
    return rateLimitRps;
  }

  @JsonProperty("rateLimitRps")
  public void setRateLimitRps(@Nullable Double rateLimitRps) {
    this.rateLimitRps = rateLimitRps;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourceLimitsSpec resourceLimitsSpec = (ResourceLimitsSpec) o;
    return Objects.equals(this.totalTimeoutMs, resourceLimitsSpec.totalTimeoutMs) &&
        Objects.equals(this.stepTimeoutMs, resourceLimitsSpec.stepTimeoutMs) &&
        Objects.equals(this.maxTokens, resourceLimitsSpec.maxTokens) &&
        Objects.equals(this.rateLimitRps, resourceLimitsSpec.rateLimitRps);
  }

  @Override
  public int hashCode() {
    return Objects.hash(totalTimeoutMs, stepTimeoutMs, maxTokens, rateLimitRps);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResourceLimitsSpec {\n");
    sb.append("    totalTimeoutMs: ").append(toIndentedString(totalTimeoutMs)).append("\n");
    sb.append("    stepTimeoutMs: ").append(toIndentedString(stepTimeoutMs)).append("\n");
    sb.append("    maxTokens: ").append(toIndentedString(maxTokens)).append("\n");
    sb.append("    rateLimitRps: ").append(toIndentedString(rateLimitRps)).append("\n");
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

