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
 * KnowledgeCategoryResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-18T21:07:32.055501800-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class KnowledgeCategoryResponse {

  private String id;

  private String name;

  private @Nullable String description;

  public KnowledgeCategoryResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public KnowledgeCategoryResponse(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public KnowledgeCategoryResponse id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Unique identifier for the category
   * @return id
   */
  @NotNull 
  @Schema(name = "id", description = "Unique identifier for the category", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  public KnowledgeCategoryResponse name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Name of the category (e.g. HR, Engineering)
   * @return name
   */
  @NotNull 
  @Schema(name = "name", description = "Name of the category (e.g. HR, Engineering)", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  public KnowledgeCategoryResponse description(@Nullable String description) {
    this.description = description;
    return this;
  }

  /**
   * Optional description of the category
   * @return description
   */
  
  @Schema(name = "description", description = "Optional description of the category", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("description")
  public @Nullable String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(@Nullable String description) {
    this.description = description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KnowledgeCategoryResponse knowledgeCategoryResponse = (KnowledgeCategoryResponse) o;
    return Objects.equals(this.id, knowledgeCategoryResponse.id) &&
        Objects.equals(this.name, knowledgeCategoryResponse.name) &&
        Objects.equals(this.description, knowledgeCategoryResponse.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class KnowledgeCategoryResponse {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

