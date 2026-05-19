package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
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
 * Result of a connection test
 */

@Schema(name = "ConnectionTestResponse", description = "Result of a connection test")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-18T21:07:32.055501800-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ConnectionTestResponse {

  private @Nullable Boolean success;

  private @Nullable String message;

  private @Nullable Long durationMs;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime testedAt;

  public ConnectionTestResponse success(@Nullable Boolean success) {
    this.success = success;
    return this;
  }

  /**
   * Get success
   * @return success
   */
  
  @Schema(name = "success", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("success")
  public @Nullable Boolean getSuccess() {
    return success;
  }

  @JsonProperty("success")
  public void setSuccess(@Nullable Boolean success) {
    this.success = success;
  }

  public ConnectionTestResponse message(@Nullable String message) {
    this.message = message;
    return this;
  }

  /**
   * Get message
   * @return message
   */
  
  @Schema(name = "message", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("message")
  public @Nullable String getMessage() {
    return message;
  }

  @JsonProperty("message")
  public void setMessage(@Nullable String message) {
    this.message = message;
  }

  public ConnectionTestResponse durationMs(@Nullable Long durationMs) {
    this.durationMs = durationMs;
    return this;
  }

  /**
   * Get durationMs
   * @return durationMs
   */
  
  @Schema(name = "durationMs", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("durationMs")
  public @Nullable Long getDurationMs() {
    return durationMs;
  }

  @JsonProperty("durationMs")
  public void setDurationMs(@Nullable Long durationMs) {
    this.durationMs = durationMs;
  }

  public ConnectionTestResponse testedAt(@Nullable OffsetDateTime testedAt) {
    this.testedAt = testedAt;
    return this;
  }

  /**
   * Get testedAt
   * @return testedAt
   */
  @Valid 
  @Schema(name = "testedAt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("testedAt")
  public @Nullable OffsetDateTime getTestedAt() {
    return testedAt;
  }

  @JsonProperty("testedAt")
  public void setTestedAt(@Nullable OffsetDateTime testedAt) {
    this.testedAt = testedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConnectionTestResponse connectionTestResponse = (ConnectionTestResponse) o;
    return Objects.equals(this.success, connectionTestResponse.success) &&
        Objects.equals(this.message, connectionTestResponse.message) &&
        Objects.equals(this.durationMs, connectionTestResponse.durationMs) &&
        Objects.equals(this.testedAt, connectionTestResponse.testedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(success, message, durationMs, testedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConnectionTestResponse {\n");
    sb.append("    success: ").append(toIndentedString(success)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    durationMs: ").append(toIndentedString(durationMs)).append("\n");
    sb.append("    testedAt: ").append(toIndentedString(testedAt)).append("\n");
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

