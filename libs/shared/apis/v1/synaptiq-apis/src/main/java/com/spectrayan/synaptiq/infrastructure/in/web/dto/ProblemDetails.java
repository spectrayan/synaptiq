package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.ValidationError;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * RFC 9457 Problem Details for HTTP APIs
 */

@Schema(name = "ProblemDetails", description = "RFC 9457 Problem Details for HTTP APIs")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-18T21:07:32.055501800-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ProblemDetails {

  private URI type;

  private String title;

  private Integer status;

  private @Nullable String detail;

  private @Nullable URI instance;

  private @Nullable String code;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime timestamp;

  private @Nullable String traceId;

  @Valid
  private List<@Valid ValidationError> errors = new ArrayList<>();

  public ProblemDetails() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ProblemDetails(URI type, String title, Integer status) {
    this.type = type;
    this.title = title;
    this.status = status;
  }

  public ProblemDetails type(URI type) {
    this.type = type;
    return this;
  }

  /**
   * URI reference identifying the problem type
   * @return type
   */
  @NotNull @Valid 
  @Schema(name = "type", description = "URI reference identifying the problem type", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("type")
  public URI getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(URI type) {
    this.type = type;
  }

  public ProblemDetails title(String title) {
    this.title = title;
    return this;
  }

  /**
   * Short human-readable summary
   * @return title
   */
  @NotNull 
  @Schema(name = "title", description = "Short human-readable summary", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
  }

  public ProblemDetails status(Integer status) {
    this.status = status;
    return this;
  }

  /**
   * HTTP status code
   * @return status
   */
  @NotNull 
  @Schema(name = "status", description = "HTTP status code", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("status")
  public Integer getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(Integer status) {
    this.status = status;
  }

  public ProblemDetails detail(@Nullable String detail) {
    this.detail = detail;
    return this;
  }

  /**
   * Human-readable explanation
   * @return detail
   */
  
  @Schema(name = "detail", description = "Human-readable explanation", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("detail")
  public @Nullable String getDetail() {
    return detail;
  }

  @JsonProperty("detail")
  public void setDetail(@Nullable String detail) {
    this.detail = detail;
  }

  public ProblemDetails instance(@Nullable URI instance) {
    this.instance = instance;
    return this;
  }

  /**
   * URI reference identifying the specific occurrence
   * @return instance
   */
  @Valid 
  @Schema(name = "instance", description = "URI reference identifying the specific occurrence", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("instance")
  public @Nullable URI getInstance() {
    return instance;
  }

  @JsonProperty("instance")
  public void setInstance(@Nullable URI instance) {
    this.instance = instance;
  }

  public ProblemDetails code(@Nullable String code) {
    this.code = code;
    return this;
  }

  /**
   * Machine-readable error code for frontend translation
   * @return code
   */
  
  @Schema(name = "code", description = "Machine-readable error code for frontend translation", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("code")
  public @Nullable String getCode() {
    return code;
  }

  @JsonProperty("code")
  public void setCode(@Nullable String code) {
    this.code = code;
  }

  public ProblemDetails timestamp(@Nullable OffsetDateTime timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  /**
   * ISO 8601 timestamp of the error
   * @return timestamp
   */
  @Valid 
  @Schema(name = "timestamp", description = "ISO 8601 timestamp of the error", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("timestamp")
  public @Nullable OffsetDateTime getTimestamp() {
    return timestamp;
  }

  @JsonProperty("timestamp")
  public void setTimestamp(@Nullable OffsetDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public ProblemDetails traceId(@Nullable String traceId) {
    this.traceId = traceId;
    return this;
  }

  /**
   * Unique identifier for request tracing
   * @return traceId
   */
  
  @Schema(name = "traceId", description = "Unique identifier for request tracing", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("traceId")
  public @Nullable String getTraceId() {
    return traceId;
  }

  @JsonProperty("traceId")
  public void setTraceId(@Nullable String traceId) {
    this.traceId = traceId;
  }

  public ProblemDetails errors(List<@Valid ValidationError> errors) {
    this.errors = errors;
    return this;
  }

  public ProblemDetails addErrorsItem(ValidationError errorsItem) {
    if (this.errors == null) {
      this.errors = new ArrayList<>();
    }
    this.errors.add(errorsItem);
    return this;
  }

  /**
   * Get errors
   * @return errors
   */
  @Valid 
  @Schema(name = "errors", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("errors")
  public List<@Valid ValidationError> getErrors() {
    return errors;
  }

  @JsonProperty("errors")
  public void setErrors(List<@Valid ValidationError> errors) {
    this.errors = errors;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProblemDetails problemDetails = (ProblemDetails) o;
    return Objects.equals(this.type, problemDetails.type) &&
        Objects.equals(this.title, problemDetails.title) &&
        Objects.equals(this.status, problemDetails.status) &&
        Objects.equals(this.detail, problemDetails.detail) &&
        Objects.equals(this.instance, problemDetails.instance) &&
        Objects.equals(this.code, problemDetails.code) &&
        Objects.equals(this.timestamp, problemDetails.timestamp) &&
        Objects.equals(this.traceId, problemDetails.traceId) &&
        Objects.equals(this.errors, problemDetails.errors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, title, status, detail, instance, code, timestamp, traceId, errors);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProblemDetails {\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    detail: ").append(toIndentedString(detail)).append("\n");
    sb.append("    instance: ").append(toIndentedString(instance)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("    traceId: ").append(toIndentedString(traceId)).append("\n");
    sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
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

