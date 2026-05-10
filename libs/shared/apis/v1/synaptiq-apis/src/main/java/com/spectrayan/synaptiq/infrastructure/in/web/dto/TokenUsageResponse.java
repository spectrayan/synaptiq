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
 * TokenUsageResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-10T17:15:52.297398600-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class TokenUsageResponse {

  private @Nullable Integer totalTokensInput;

  private @Nullable Integer totalTokensOutput;

  private @Nullable Integer totalTokens;

  private @Nullable Double estimatedCostUsd;

  private @Nullable Integer planTokenLimit;

  private @Nullable Double usagePercent;

  public TokenUsageResponse totalTokensInput(@Nullable Integer totalTokensInput) {
    this.totalTokensInput = totalTokensInput;
    return this;
  }

  /**
   * Get totalTokensInput
   * @return totalTokensInput
   */
  
  @Schema(name = "totalTokensInput", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalTokensInput")
  public @Nullable Integer getTotalTokensInput() {
    return totalTokensInput;
  }

  @JsonProperty("totalTokensInput")
  public void setTotalTokensInput(@Nullable Integer totalTokensInput) {
    this.totalTokensInput = totalTokensInput;
  }

  public TokenUsageResponse totalTokensOutput(@Nullable Integer totalTokensOutput) {
    this.totalTokensOutput = totalTokensOutput;
    return this;
  }

  /**
   * Get totalTokensOutput
   * @return totalTokensOutput
   */
  
  @Schema(name = "totalTokensOutput", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalTokensOutput")
  public @Nullable Integer getTotalTokensOutput() {
    return totalTokensOutput;
  }

  @JsonProperty("totalTokensOutput")
  public void setTotalTokensOutput(@Nullable Integer totalTokensOutput) {
    this.totalTokensOutput = totalTokensOutput;
  }

  public TokenUsageResponse totalTokens(@Nullable Integer totalTokens) {
    this.totalTokens = totalTokens;
    return this;
  }

  /**
   * Get totalTokens
   * @return totalTokens
   */
  
  @Schema(name = "totalTokens", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalTokens")
  public @Nullable Integer getTotalTokens() {
    return totalTokens;
  }

  @JsonProperty("totalTokens")
  public void setTotalTokens(@Nullable Integer totalTokens) {
    this.totalTokens = totalTokens;
  }

  public TokenUsageResponse estimatedCostUsd(@Nullable Double estimatedCostUsd) {
    this.estimatedCostUsd = estimatedCostUsd;
    return this;
  }

  /**
   * Get estimatedCostUsd
   * @return estimatedCostUsd
   */
  
  @Schema(name = "estimatedCostUsd", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("estimatedCostUsd")
  public @Nullable Double getEstimatedCostUsd() {
    return estimatedCostUsd;
  }

  @JsonProperty("estimatedCostUsd")
  public void setEstimatedCostUsd(@Nullable Double estimatedCostUsd) {
    this.estimatedCostUsd = estimatedCostUsd;
  }

  public TokenUsageResponse planTokenLimit(@Nullable Integer planTokenLimit) {
    this.planTokenLimit = planTokenLimit;
    return this;
  }

  /**
   * Get planTokenLimit
   * @return planTokenLimit
   */
  
  @Schema(name = "planTokenLimit", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("planTokenLimit")
  public @Nullable Integer getPlanTokenLimit() {
    return planTokenLimit;
  }

  @JsonProperty("planTokenLimit")
  public void setPlanTokenLimit(@Nullable Integer planTokenLimit) {
    this.planTokenLimit = planTokenLimit;
  }

  public TokenUsageResponse usagePercent(@Nullable Double usagePercent) {
    this.usagePercent = usagePercent;
    return this;
  }

  /**
   * Get usagePercent
   * @return usagePercent
   */
  
  @Schema(name = "usagePercent", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("usagePercent")
  public @Nullable Double getUsagePercent() {
    return usagePercent;
  }

  @JsonProperty("usagePercent")
  public void setUsagePercent(@Nullable Double usagePercent) {
    this.usagePercent = usagePercent;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TokenUsageResponse tokenUsageResponse = (TokenUsageResponse) o;
    return Objects.equals(this.totalTokensInput, tokenUsageResponse.totalTokensInput) &&
        Objects.equals(this.totalTokensOutput, tokenUsageResponse.totalTokensOutput) &&
        Objects.equals(this.totalTokens, tokenUsageResponse.totalTokens) &&
        Objects.equals(this.estimatedCostUsd, tokenUsageResponse.estimatedCostUsd) &&
        Objects.equals(this.planTokenLimit, tokenUsageResponse.planTokenLimit) &&
        Objects.equals(this.usagePercent, tokenUsageResponse.usagePercent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(totalTokensInput, totalTokensOutput, totalTokens, estimatedCostUsd, planTokenLimit, usagePercent);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TokenUsageResponse {\n");
    sb.append("    totalTokensInput: ").append(toIndentedString(totalTokensInput)).append("\n");
    sb.append("    totalTokensOutput: ").append(toIndentedString(totalTokensOutput)).append("\n");
    sb.append("    totalTokens: ").append(toIndentedString(totalTokens)).append("\n");
    sb.append("    estimatedCostUsd: ").append(toIndentedString(estimatedCostUsd)).append("\n");
    sb.append("    planTokenLimit: ").append(toIndentedString(planTokenLimit)).append("\n");
    sb.append("    usagePercent: ").append(toIndentedString(usagePercent)).append("\n");
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

