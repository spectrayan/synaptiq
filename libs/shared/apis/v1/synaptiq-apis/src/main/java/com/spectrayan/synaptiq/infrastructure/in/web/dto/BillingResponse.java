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
 * BillingResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-18T21:07:32.055501800-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class BillingResponse {

  private @Nullable Integer seatCount;

  private @Nullable Integer totalTokens;

  private @Nullable Double estimatedCostUsd;

  public BillingResponse seatCount(@Nullable Integer seatCount) {
    this.seatCount = seatCount;
    return this;
  }

  /**
   * Get seatCount
   * @return seatCount
   */
  
  @Schema(name = "seatCount", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("seatCount")
  public @Nullable Integer getSeatCount() {
    return seatCount;
  }

  @JsonProperty("seatCount")
  public void setSeatCount(@Nullable Integer seatCount) {
    this.seatCount = seatCount;
  }

  public BillingResponse totalTokens(@Nullable Integer totalTokens) {
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

  public BillingResponse estimatedCostUsd(@Nullable Double estimatedCostUsd) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BillingResponse billingResponse = (BillingResponse) o;
    return Objects.equals(this.seatCount, billingResponse.seatCount) &&
        Objects.equals(this.totalTokens, billingResponse.totalTokens) &&
        Objects.equals(this.estimatedCostUsd, billingResponse.estimatedCostUsd);
  }

  @Override
  public int hashCode() {
    return Objects.hash(seatCount, totalTokens, estimatedCostUsd);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BillingResponse {\n");
    sb.append("    seatCount: ").append(toIndentedString(seatCount)).append("\n");
    sb.append("    totalTokens: ").append(toIndentedString(totalTokens)).append("\n");
    sb.append("    estimatedCostUsd: ").append(toIndentedString(estimatedCostUsd)).append("\n");
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

