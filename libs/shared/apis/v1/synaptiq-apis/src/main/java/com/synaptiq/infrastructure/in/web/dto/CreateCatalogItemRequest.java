package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.synaptiq.infrastructure.in.web.dto.CatalogItemStatus;
import java.util.HashMap;
import java.util.Map;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CreateCatalogItemRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-05T21:15:25.464614100-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class CreateCatalogItemRequest {

  @Valid
  private Map<String, Object> data = new HashMap<>();

  private @Nullable CatalogItemStatus status;

  public CreateCatalogItemRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CreateCatalogItemRequest(Map<String, Object> data) {
    this.data = data;
  }

  public CreateCatalogItemRequest data(Map<String, Object> data) {
    this.data = data;
    return this;
  }

  public CreateCatalogItemRequest putDataItem(String key, Object dataItem) {
    if (this.data == null) {
      this.data = new HashMap<>();
    }
    this.data.put(key, dataItem);
    return this;
  }

  /**
   * Get data
   * @return data
   */
  @NotNull 
  @Schema(name = "data", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("data")
  public Map<String, Object> getData() {
    return data;
  }

  @JsonProperty("data")
  public void setData(Map<String, Object> data) {
    this.data = data;
  }

  public CreateCatalogItemRequest status(@Nullable CatalogItemStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
   */
  @Valid 
  @Schema(name = "status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public @Nullable CatalogItemStatus getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(@Nullable CatalogItemStatus status) {
    this.status = status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateCatalogItemRequest createCatalogItemRequest = (CreateCatalogItemRequest) o;
    return Objects.equals(this.data, createCatalogItemRequest.data) &&
        Objects.equals(this.status, createCatalogItemRequest.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateCatalogItemRequest {\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
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

