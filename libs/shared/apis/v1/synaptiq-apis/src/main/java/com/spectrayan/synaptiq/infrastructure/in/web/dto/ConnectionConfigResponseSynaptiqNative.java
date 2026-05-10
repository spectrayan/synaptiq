package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ConnectionConfigResponseSynaptiqNative
 */

@JsonTypeName("ConnectionConfigResponse_synaptiqNative")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-10T17:15:52.297398600-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ConnectionConfigResponseSynaptiqNative {

  private @Nullable String collectionName;

  public ConnectionConfigResponseSynaptiqNative collectionName(@Nullable String collectionName) {
    this.collectionName = collectionName;
    return this;
  }

  /**
   * Get collectionName
   * @return collectionName
   */
  
  @Schema(name = "collectionName", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("collectionName")
  public @Nullable String getCollectionName() {
    return collectionName;
  }

  @JsonProperty("collectionName")
  public void setCollectionName(@Nullable String collectionName) {
    this.collectionName = collectionName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConnectionConfigResponseSynaptiqNative connectionConfigResponseSynaptiqNative = (ConnectionConfigResponseSynaptiqNative) o;
    return Objects.equals(this.collectionName, connectionConfigResponseSynaptiqNative.collectionName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(collectionName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConnectionConfigResponseSynaptiqNative {\n");
    sb.append("    collectionName: ").append(toIndentedString(collectionName)).append("\n");
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

