package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CollectionListResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-08T22:14:16.718368-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class CollectionListResponse {

  @Valid
  private List<String> collections = new ArrayList<>();

  public CollectionListResponse collections(List<String> collections) {
    this.collections = collections;
    return this;
  }

  public CollectionListResponse addCollectionsItem(String collectionsItem) {
    if (this.collections == null) {
      this.collections = new ArrayList<>();
    }
    this.collections.add(collectionsItem);
    return this;
  }

  /**
   * Get collections
   * @return collections
   */
  
  @Schema(name = "collections", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("collections")
  public List<String> getCollections() {
    return collections;
  }

  @JsonProperty("collections")
  public void setCollections(List<String> collections) {
    this.collections = collections;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CollectionListResponse collectionListResponse = (CollectionListResponse) o;
    return Objects.equals(this.collections, collectionListResponse.collections);
  }

  @Override
  public int hashCode() {
    return Objects.hash(collections);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CollectionListResponse {\n");
    sb.append("    collections: ").append(toIndentedString(collections)).append("\n");
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

