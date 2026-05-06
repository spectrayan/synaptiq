package com.synaptiq.infrastructure.in.web.dto;

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
 * HealthResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-05T21:15:25.464614100-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class HealthResponse {

  private @Nullable String status;

  private @Nullable String mongo;

  private @Nullable String redis;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime timestamp;

  public HealthResponse status(@Nullable String status) {
    this.status = status;
    return this;
  }

  /**
   * Health status (UP or DOWN)
   * @return status
   */
  
  @Schema(name = "status", description = "Health status (UP or DOWN)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public @Nullable String getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(@Nullable String status) {
    this.status = status;
  }

  public HealthResponse mongo(@Nullable String mongo) {
    this.mongo = mongo;
    return this;
  }

  /**
   * Get mongo
   * @return mongo
   */
  
  @Schema(name = "mongo", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("mongo")
  public @Nullable String getMongo() {
    return mongo;
  }

  @JsonProperty("mongo")
  public void setMongo(@Nullable String mongo) {
    this.mongo = mongo;
  }

  public HealthResponse redis(@Nullable String redis) {
    this.redis = redis;
    return this;
  }

  /**
   * Get redis
   * @return redis
   */
  
  @Schema(name = "redis", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("redis")
  public @Nullable String getRedis() {
    return redis;
  }

  @JsonProperty("redis")
  public void setRedis(@Nullable String redis) {
    this.redis = redis;
  }

  public HealthResponse timestamp(@Nullable OffsetDateTime timestamp) {
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
    HealthResponse healthResponse = (HealthResponse) o;
    return Objects.equals(this.status, healthResponse.status) &&
        Objects.equals(this.mongo, healthResponse.mongo) &&
        Objects.equals(this.redis, healthResponse.redis) &&
        Objects.equals(this.timestamp, healthResponse.timestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, mongo, redis, timestamp);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class HealthResponse {\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    mongo: ").append(toIndentedString(mongo)).append("\n");
    sb.append("    redis: ").append(toIndentedString(redis)).append("\n");
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

