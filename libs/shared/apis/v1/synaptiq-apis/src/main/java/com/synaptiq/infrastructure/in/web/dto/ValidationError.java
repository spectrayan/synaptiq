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
 * ValidationError
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-08T22:14:16.718368-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ValidationError {

  private @Nullable String field;

  private @Nullable String message;

  private @Nullable String rejectedValue;

  private @Nullable String code;

  public ValidationError field(@Nullable String field) {
    this.field = field;
    return this;
  }

  /**
   * Get field
   * @return field
   */
  
  @Schema(name = "field", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("field")
  public @Nullable String getField() {
    return field;
  }

  @JsonProperty("field")
  public void setField(@Nullable String field) {
    this.field = field;
  }

  public ValidationError message(@Nullable String message) {
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

  public ValidationError rejectedValue(@Nullable String rejectedValue) {
    this.rejectedValue = rejectedValue;
    return this;
  }

  /**
   * Get rejectedValue
   * @return rejectedValue
   */
  
  @Schema(name = "rejectedValue", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("rejectedValue")
  public @Nullable String getRejectedValue() {
    return rejectedValue;
  }

  @JsonProperty("rejectedValue")
  public void setRejectedValue(@Nullable String rejectedValue) {
    this.rejectedValue = rejectedValue;
  }

  public ValidationError code(@Nullable String code) {
    this.code = code;
    return this;
  }

  /**
   * Get code
   * @return code
   */
  
  @Schema(name = "code", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("code")
  public @Nullable String getCode() {
    return code;
  }

  @JsonProperty("code")
  public void setCode(@Nullable String code) {
    this.code = code;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ValidationError validationError = (ValidationError) o;
    return Objects.equals(this.field, validationError.field) &&
        Objects.equals(this.message, validationError.message) &&
        Objects.equals(this.rejectedValue, validationError.rejectedValue) &&
        Objects.equals(this.code, validationError.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(field, message, rejectedValue, code);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ValidationError {\n");
    sb.append("    field: ").append(toIndentedString(field)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    rejectedValue: ").append(toIndentedString(rejectedValue)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
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

