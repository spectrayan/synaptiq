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
 * AnalyticsSummaryResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-18T21:07:32.055501800-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class AnalyticsSummaryResponse {

  private @Nullable Integer totalConversations;

  private @Nullable Integer totalMessages;

  private @Nullable Integer totalTokensInput;

  private @Nullable Integer totalTokensOutput;

  private @Nullable Integer totalActions;

  private @Nullable Integer uniqueUsers;

  private @Nullable Double avgMessagesPerSession;

  public AnalyticsSummaryResponse totalConversations(@Nullable Integer totalConversations) {
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

  public AnalyticsSummaryResponse totalMessages(@Nullable Integer totalMessages) {
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

  public AnalyticsSummaryResponse totalTokensInput(@Nullable Integer totalTokensInput) {
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

  public AnalyticsSummaryResponse totalTokensOutput(@Nullable Integer totalTokensOutput) {
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

  public AnalyticsSummaryResponse totalActions(@Nullable Integer totalActions) {
    this.totalActions = totalActions;
    return this;
  }

  /**
   * Get totalActions
   * @return totalActions
   */
  
  @Schema(name = "totalActions", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalActions")
  public @Nullable Integer getTotalActions() {
    return totalActions;
  }

  @JsonProperty("totalActions")
  public void setTotalActions(@Nullable Integer totalActions) {
    this.totalActions = totalActions;
  }

  public AnalyticsSummaryResponse uniqueUsers(@Nullable Integer uniqueUsers) {
    this.uniqueUsers = uniqueUsers;
    return this;
  }

  /**
   * Get uniqueUsers
   * @return uniqueUsers
   */
  
  @Schema(name = "uniqueUsers", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("uniqueUsers")
  public @Nullable Integer getUniqueUsers() {
    return uniqueUsers;
  }

  @JsonProperty("uniqueUsers")
  public void setUniqueUsers(@Nullable Integer uniqueUsers) {
    this.uniqueUsers = uniqueUsers;
  }

  public AnalyticsSummaryResponse avgMessagesPerSession(@Nullable Double avgMessagesPerSession) {
    this.avgMessagesPerSession = avgMessagesPerSession;
    return this;
  }

  /**
   * Get avgMessagesPerSession
   * @return avgMessagesPerSession
   */
  
  @Schema(name = "avgMessagesPerSession", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("avgMessagesPerSession")
  public @Nullable Double getAvgMessagesPerSession() {
    return avgMessagesPerSession;
  }

  @JsonProperty("avgMessagesPerSession")
  public void setAvgMessagesPerSession(@Nullable Double avgMessagesPerSession) {
    this.avgMessagesPerSession = avgMessagesPerSession;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AnalyticsSummaryResponse analyticsSummaryResponse = (AnalyticsSummaryResponse) o;
    return Objects.equals(this.totalConversations, analyticsSummaryResponse.totalConversations) &&
        Objects.equals(this.totalMessages, analyticsSummaryResponse.totalMessages) &&
        Objects.equals(this.totalTokensInput, analyticsSummaryResponse.totalTokensInput) &&
        Objects.equals(this.totalTokensOutput, analyticsSummaryResponse.totalTokensOutput) &&
        Objects.equals(this.totalActions, analyticsSummaryResponse.totalActions) &&
        Objects.equals(this.uniqueUsers, analyticsSummaryResponse.uniqueUsers) &&
        Objects.equals(this.avgMessagesPerSession, analyticsSummaryResponse.avgMessagesPerSession);
  }

  @Override
  public int hashCode() {
    return Objects.hash(totalConversations, totalMessages, totalTokensInput, totalTokensOutput, totalActions, uniqueUsers, avgMessagesPerSession);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AnalyticsSummaryResponse {\n");
    sb.append("    totalConversations: ").append(toIndentedString(totalConversations)).append("\n");
    sb.append("    totalMessages: ").append(toIndentedString(totalMessages)).append("\n");
    sb.append("    totalTokensInput: ").append(toIndentedString(totalTokensInput)).append("\n");
    sb.append("    totalTokensOutput: ").append(toIndentedString(totalTokensOutput)).append("\n");
    sb.append("    totalActions: ").append(toIndentedString(totalActions)).append("\n");
    sb.append("    uniqueUsers: ").append(toIndentedString(uniqueUsers)).append("\n");
    sb.append("    avgMessagesPerSession: ").append(toIndentedString(avgMessagesPerSession)).append("\n");
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

