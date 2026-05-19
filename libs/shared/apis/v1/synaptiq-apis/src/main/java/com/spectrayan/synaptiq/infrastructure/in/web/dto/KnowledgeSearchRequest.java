package com.spectrayan.synaptiq.infrastructure.in.web.dto;

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
 * KnowledgeSearchRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-18T21:07:32.055501800-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class KnowledgeSearchRequest {

  private String query;

  private @Nullable String categoryId;

  @Valid
  private List<String> tags = new ArrayList<>();

  private Integer topK = 5;

  public KnowledgeSearchRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public KnowledgeSearchRequest(String query) {
    this.query = query;
  }

  public KnowledgeSearchRequest query(String query) {
    this.query = query;
    return this;
  }

  /**
   * The search query string
   * @return query
   */
  @NotNull 
  @Schema(name = "query", description = "The search query string", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("query")
  public String getQuery() {
    return query;
  }

  @JsonProperty("query")
  public void setQuery(String query) {
    this.query = query;
  }

  public KnowledgeSearchRequest categoryId(@Nullable String categoryId) {
    this.categoryId = categoryId;
    return this;
  }

  /**
   * Optional category to narrow down search
   * @return categoryId
   */
  
  @Schema(name = "categoryId", description = "Optional category to narrow down search", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("categoryId")
  public @Nullable String getCategoryId() {
    return categoryId;
  }

  @JsonProperty("categoryId")
  public void setCategoryId(@Nullable String categoryId) {
    this.categoryId = categoryId;
  }

  public KnowledgeSearchRequest tags(List<String> tags) {
    this.tags = tags;
    return this;
  }

  public KnowledgeSearchRequest addTagsItem(String tagsItem) {
    if (this.tags == null) {
      this.tags = new ArrayList<>();
    }
    this.tags.add(tagsItem);
    return this;
  }

  /**
   * Optional tags to filter by
   * @return tags
   */
  
  @Schema(name = "tags", description = "Optional tags to filter by", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("tags")
  public List<String> getTags() {
    return tags;
  }

  @JsonProperty("tags")
  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public KnowledgeSearchRequest topK(Integer topK) {
    this.topK = topK;
    return this;
  }

  /**
   * Get topK
   * @return topK
   */
  
  @Schema(name = "topK", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("topK")
  public Integer getTopK() {
    return topK;
  }

  @JsonProperty("topK")
  public void setTopK(Integer topK) {
    this.topK = topK;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KnowledgeSearchRequest knowledgeSearchRequest = (KnowledgeSearchRequest) o;
    return Objects.equals(this.query, knowledgeSearchRequest.query) &&
        Objects.equals(this.categoryId, knowledgeSearchRequest.categoryId) &&
        Objects.equals(this.tags, knowledgeSearchRequest.tags) &&
        Objects.equals(this.topK, knowledgeSearchRequest.topK);
  }

  @Override
  public int hashCode() {
    return Objects.hash(query, categoryId, tags, topK);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class KnowledgeSearchRequest {\n");
    sb.append("    query: ").append(toIndentedString(query)).append("\n");
    sb.append("    categoryId: ").append(toIndentedString(categoryId)).append("\n");
    sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
    sb.append("    topK: ").append(toIndentedString(topK)).append("\n");
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

