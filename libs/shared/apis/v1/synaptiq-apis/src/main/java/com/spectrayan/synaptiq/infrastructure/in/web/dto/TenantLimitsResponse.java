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
 * TenantLimitsResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-10T17:15:52.297398600-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class TenantLimitsResponse {

  private @Nullable Integer maxCatalogItems;

  private @Nullable Integer maxMonthlyTokens;

  private @Nullable Integer maxUsers;

  private @Nullable Integer maxRequestsPerMinute;

  private @Nullable Integer seatCap;

  public TenantLimitsResponse maxCatalogItems(@Nullable Integer maxCatalogItems) {
    this.maxCatalogItems = maxCatalogItems;
    return this;
  }

  /**
   * Get maxCatalogItems
   * @return maxCatalogItems
   */
  
  @Schema(name = "maxCatalogItems", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("maxCatalogItems")
  public @Nullable Integer getMaxCatalogItems() {
    return maxCatalogItems;
  }

  @JsonProperty("maxCatalogItems")
  public void setMaxCatalogItems(@Nullable Integer maxCatalogItems) {
    this.maxCatalogItems = maxCatalogItems;
  }

  public TenantLimitsResponse maxMonthlyTokens(@Nullable Integer maxMonthlyTokens) {
    this.maxMonthlyTokens = maxMonthlyTokens;
    return this;
  }

  /**
   * Get maxMonthlyTokens
   * @return maxMonthlyTokens
   */
  
  @Schema(name = "maxMonthlyTokens", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("maxMonthlyTokens")
  public @Nullable Integer getMaxMonthlyTokens() {
    return maxMonthlyTokens;
  }

  @JsonProperty("maxMonthlyTokens")
  public void setMaxMonthlyTokens(@Nullable Integer maxMonthlyTokens) {
    this.maxMonthlyTokens = maxMonthlyTokens;
  }

  public TenantLimitsResponse maxUsers(@Nullable Integer maxUsers) {
    this.maxUsers = maxUsers;
    return this;
  }

  /**
   * Get maxUsers
   * @return maxUsers
   */
  
  @Schema(name = "maxUsers", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("maxUsers")
  public @Nullable Integer getMaxUsers() {
    return maxUsers;
  }

  @JsonProperty("maxUsers")
  public void setMaxUsers(@Nullable Integer maxUsers) {
    this.maxUsers = maxUsers;
  }

  public TenantLimitsResponse maxRequestsPerMinute(@Nullable Integer maxRequestsPerMinute) {
    this.maxRequestsPerMinute = maxRequestsPerMinute;
    return this;
  }

  /**
   * Get maxRequestsPerMinute
   * @return maxRequestsPerMinute
   */
  
  @Schema(name = "maxRequestsPerMinute", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("maxRequestsPerMinute")
  public @Nullable Integer getMaxRequestsPerMinute() {
    return maxRequestsPerMinute;
  }

  @JsonProperty("maxRequestsPerMinute")
  public void setMaxRequestsPerMinute(@Nullable Integer maxRequestsPerMinute) {
    this.maxRequestsPerMinute = maxRequestsPerMinute;
  }

  public TenantLimitsResponse seatCap(@Nullable Integer seatCap) {
    this.seatCap = seatCap;
    return this;
  }

  /**
   * Get seatCap
   * @return seatCap
   */
  
  @Schema(name = "seatCap", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("seatCap")
  public @Nullable Integer getSeatCap() {
    return seatCap;
  }

  @JsonProperty("seatCap")
  public void setSeatCap(@Nullable Integer seatCap) {
    this.seatCap = seatCap;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TenantLimitsResponse tenantLimitsResponse = (TenantLimitsResponse) o;
    return Objects.equals(this.maxCatalogItems, tenantLimitsResponse.maxCatalogItems) &&
        Objects.equals(this.maxMonthlyTokens, tenantLimitsResponse.maxMonthlyTokens) &&
        Objects.equals(this.maxUsers, tenantLimitsResponse.maxUsers) &&
        Objects.equals(this.maxRequestsPerMinute, tenantLimitsResponse.maxRequestsPerMinute) &&
        Objects.equals(this.seatCap, tenantLimitsResponse.seatCap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(maxCatalogItems, maxMonthlyTokens, maxUsers, maxRequestsPerMinute, seatCap);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TenantLimitsResponse {\n");
    sb.append("    maxCatalogItems: ").append(toIndentedString(maxCatalogItems)).append("\n");
    sb.append("    maxMonthlyTokens: ").append(toIndentedString(maxMonthlyTokens)).append("\n");
    sb.append("    maxUsers: ").append(toIndentedString(maxUsers)).append("\n");
    sb.append("    maxRequestsPerMinute: ").append(toIndentedString(maxRequestsPerMinute)).append("\n");
    sb.append("    seatCap: ").append(toIndentedString(seatCap)).append("\n");
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

