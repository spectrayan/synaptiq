package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.synaptiq.infrastructure.in.web.dto.ConversationRole;
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
 * ConversationTurnResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-08T22:14:16.718368-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ConversationTurnResponse {

  private @Nullable String turnId;

  private @Nullable ConversationRole role;

  private @Nullable String content;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime timestamp;

  public ConversationTurnResponse turnId(@Nullable String turnId) {
    this.turnId = turnId;
    return this;
  }

  /**
   * Get turnId
   * @return turnId
   */
  
  @Schema(name = "turnId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("turnId")
  public @Nullable String getTurnId() {
    return turnId;
  }

  @JsonProperty("turnId")
  public void setTurnId(@Nullable String turnId) {
    this.turnId = turnId;
  }

  public ConversationTurnResponse role(@Nullable ConversationRole role) {
    this.role = role;
    return this;
  }

  /**
   * Get role
   * @return role
   */
  @Valid 
  @Schema(name = "role", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("role")
  public @Nullable ConversationRole getRole() {
    return role;
  }

  @JsonProperty("role")
  public void setRole(@Nullable ConversationRole role) {
    this.role = role;
  }

  public ConversationTurnResponse content(@Nullable String content) {
    this.content = content;
    return this;
  }

  /**
   * Get content
   * @return content
   */
  
  @Schema(name = "content", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("content")
  public @Nullable String getContent() {
    return content;
  }

  @JsonProperty("content")
  public void setContent(@Nullable String content) {
    this.content = content;
  }

  public ConversationTurnResponse timestamp(@Nullable OffsetDateTime timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  /**
   * Get timestamp
   * @return timestamp
   */
  @Valid 
  @Schema(name = "timestamp", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("timestamp")
  public @Nullable OffsetDateTime getTimestamp() {
    return timestamp;
  }

  @JsonProperty("timestamp")
  public void setTimestamp(@Nullable OffsetDateTime timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConversationTurnResponse conversationTurnResponse = (ConversationTurnResponse) o;
    return Objects.equals(this.turnId, conversationTurnResponse.turnId) &&
        Objects.equals(this.role, conversationTurnResponse.role) &&
        Objects.equals(this.content, conversationTurnResponse.content) &&
        Objects.equals(this.timestamp, conversationTurnResponse.timestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(turnId, role, content, timestamp);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConversationTurnResponse {\n");
    sb.append("    turnId: ").append(toIndentedString(turnId)).append("\n");
    sb.append("    role: ").append(toIndentedString(role)).append("\n");
    sb.append("    content: ").append(toIndentedString(content)).append("\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
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

