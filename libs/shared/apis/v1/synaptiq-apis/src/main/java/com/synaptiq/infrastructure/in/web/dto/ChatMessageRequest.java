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
 * ChatMessageRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-08T22:14:16.718368-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ChatMessageRequest {

  private String sessionId;

  private String message;

  private @Nullable String modelOverride;

  public ChatMessageRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ChatMessageRequest(String sessionId, String message) {
    this.sessionId = sessionId;
    this.message = message;
  }

  public ChatMessageRequest sessionId(String sessionId) {
    this.sessionId = sessionId;
    return this;
  }

  /**
   * Get sessionId
   * @return sessionId
   */
  @NotNull 
  @Schema(name = "sessionId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("sessionId")
  public String getSessionId() {
    return sessionId;
  }

  @JsonProperty("sessionId")
  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public ChatMessageRequest message(String message) {
    this.message = message;
    return this;
  }

  /**
   * Get message
   * @return message
   */
  @NotNull 
  @Schema(name = "message", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("message")
  public String getMessage() {
    return message;
  }

  @JsonProperty("message")
  public void setMessage(String message) {
    this.message = message;
  }

  public ChatMessageRequest modelOverride(@Nullable String modelOverride) {
    this.modelOverride = modelOverride;
    return this;
  }

  /**
   * Get modelOverride
   * @return modelOverride
   */
  
  @Schema(name = "modelOverride", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("modelOverride")
  public @Nullable String getModelOverride() {
    return modelOverride;
  }

  @JsonProperty("modelOverride")
  public void setModelOverride(@Nullable String modelOverride) {
    this.modelOverride = modelOverride;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ChatMessageRequest chatMessageRequest = (ChatMessageRequest) o;
    return Objects.equals(this.sessionId, chatMessageRequest.sessionId) &&
        Objects.equals(this.message, chatMessageRequest.message) &&
        Objects.equals(this.modelOverride, chatMessageRequest.modelOverride);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sessionId, message, modelOverride);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ChatMessageRequest {\n");
    sb.append("    sessionId: ").append(toIndentedString(sessionId)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    modelOverride: ").append(toIndentedString(modelOverride)).append("\n");
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

