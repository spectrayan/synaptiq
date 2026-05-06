package com.synaptiq.infrastructure.in.web.dto;

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
 * PlatformRollupResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-05T21:15:25.464614100-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class PlatformRollupResponse {

  private @Nullable Integer totalTenants;

  private @Nullable Integer activeTenants;

  private @Nullable Integer totalConversations;

  private @Nullable Integer totalMessages;

  private @Nullable Integer totalTokens;

  private @Nullable Double totalEstimatedCostUsd;

  public PlatformRollupResponse totalTenants(@Nullable Integer totalTenants) {
    this.totalTenants = totalTenants;
    return this;
  }

  /**
   * Get totalTenants
   * @return totalTenants
   */
  
  @Schema(name = "totalTenants", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalTenants")
  public @Nullable Integer getTotalTenants() {
    return totalTenants;
  }

  @JsonProperty("totalTenants")
  public void setTotalTenants(@Nullable Integer totalTenants) {
    this.totalTenants = totalTenants;
  }

  public PlatformRollupResponse activeTenants(@Nullable Integer activeTenants) {
    this.activeTenants = activeTenants;
    return this;
  }

  /**
   * Get activeTenants
   * @return activeTenants
   */
  
  @Schema(name = "activeTenants", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("activeTenants")
  public @Nullable Integer getActiveTenants() {
    return activeTenants;
  }

  @JsonProperty("activeTenants")
  public void setActiveTenants(@Nullable Integer activeTenants) {
    this.activeTenants = activeTenants;
  }

  public PlatformRollupResponse totalConversations(@Nullable Integer totalConversations) {
    this.totalConversations = totalConversations;
    return this;
  }

  /**
   * Get totalConversations
   * @return totalConversations
   */
  
  @Schema(name = "totalConversations", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalConversations")
  public @Nullable Integer getTotalConversations() {
    return totalConversations;
  }

  @JsonProperty("totalConversations")
  public void setTotalConversations(@Nullable Integer totalConversations) {
    this.totalConversations = totalConversations;
  }

  public PlatformRollupResponse totalMessages(@Nullable Integer totalMessages) {
    this.totalMessages = totalMessages;
    return this;
  }

  /**
   * Get totalMessages
   * @return totalMessages
   */
  
  @Schema(name = "totalMessages", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalMessages")
  public @Nullable Integer getTotalMessages() {
    return totalMessages;
  }

  @JsonProperty("totalMessages")
  public void setTotalMessages(@Nullable Integer totalMessages) {
    this.totalMessages = totalMessages;
  }

  public PlatformRollupResponse totalTokens(@Nullable Integer totalTokens) {
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

  public PlatformRollupResponse totalEstimatedCostUsd(@Nullable Double totalEstimatedCostUsd) {
    this.totalEstimatedCostUsd = totalEstimatedCostUsd;
    return this;
  }

  /**
   * Get totalEstimatedCostUsd
   * @return totalEstimatedCostUsd
   */
  
  @Schema(name = "totalEstimatedCostUsd", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalEstimatedCostUsd")
  public @Nullable Double getTotalEstimatedCostUsd() {
    return totalEstimatedCostUsd;
  }

  @JsonProperty("totalEstimatedCostUsd")
  public void setTotalEstimatedCostUsd(@Nullable Double totalEstimatedCostUsd) {
    this.totalEstimatedCostUsd = totalEstimatedCostUsd;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PlatformRollupResponse platformRollupResponse = (PlatformRollupResponse) o;
    return Objects.equals(this.totalTenants, platformRollupResponse.totalTenants) &&
        Objects.equals(this.activeTenants, platformRollupResponse.activeTenants) &&
        Objects.equals(this.totalConversations, platformRollupResponse.totalConversations) &&
        Objects.equals(this.totalMessages, platformRollupResponse.totalMessages) &&
        Objects.equals(this.totalTokens, platformRollupResponse.totalTokens) &&
        Objects.equals(this.totalEstimatedCostUsd, platformRollupResponse.totalEstimatedCostUsd);
  }

  @Override
  public int hashCode() {
    return Objects.hash(totalTenants, activeTenants, totalConversations, totalMessages, totalTokens, totalEstimatedCostUsd);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PlatformRollupResponse {\n");
    sb.append("    totalTenants: ").append(toIndentedString(totalTenants)).append("\n");
    sb.append("    activeTenants: ").append(toIndentedString(activeTenants)).append("\n");
    sb.append("    totalConversations: ").append(toIndentedString(totalConversations)).append("\n");
    sb.append("    totalMessages: ").append(toIndentedString(totalMessages)).append("\n");
    sb.append("    totalTokens: ").append(toIndentedString(totalTokens)).append("\n");
    sb.append("    totalEstimatedCostUsd: ").append(toIndentedString(totalEstimatedCostUsd)).append("\n");
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

