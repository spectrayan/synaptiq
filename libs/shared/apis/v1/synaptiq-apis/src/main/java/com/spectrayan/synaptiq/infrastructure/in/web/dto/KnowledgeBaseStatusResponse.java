package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.KnowledgeCategoryResponse;
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
 * KnowledgeBaseStatusResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-18T21:07:32.055501800-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class KnowledgeBaseStatusResponse {

  private @Nullable Integer totalDocuments;

  private @Nullable Integer totalCategories;

  private @Nullable Long totalStorageBytes;

  @Valid
  private List<@Valid KnowledgeCategoryResponse> categories = new ArrayList<>();

  public KnowledgeBaseStatusResponse totalDocuments(@Nullable Integer totalDocuments) {
    this.totalDocuments = totalDocuments;
    return this;
  }

  /**
   * Get totalDocuments
   * @return totalDocuments
   */
  
  @Schema(name = "totalDocuments", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalDocuments")
  public @Nullable Integer getTotalDocuments() {
    return totalDocuments;
  }

  @JsonProperty("totalDocuments")
  public void setTotalDocuments(@Nullable Integer totalDocuments) {
    this.totalDocuments = totalDocuments;
  }

  public KnowledgeBaseStatusResponse totalCategories(@Nullable Integer totalCategories) {
    this.totalCategories = totalCategories;
    return this;
  }

  /**
   * Get totalCategories
   * @return totalCategories
   */
  
  @Schema(name = "totalCategories", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalCategories")
  public @Nullable Integer getTotalCategories() {
    return totalCategories;
  }

  @JsonProperty("totalCategories")
  public void setTotalCategories(@Nullable Integer totalCategories) {
    this.totalCategories = totalCategories;
  }

  public KnowledgeBaseStatusResponse totalStorageBytes(@Nullable Long totalStorageBytes) {
    this.totalStorageBytes = totalStorageBytes;
    return this;
  }

  /**
   * Get totalStorageBytes
   * @return totalStorageBytes
   */
  
  @Schema(name = "totalStorageBytes", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalStorageBytes")
  public @Nullable Long getTotalStorageBytes() {
    return totalStorageBytes;
  }

  @JsonProperty("totalStorageBytes")
  public void setTotalStorageBytes(@Nullable Long totalStorageBytes) {
    this.totalStorageBytes = totalStorageBytes;
  }

  public KnowledgeBaseStatusResponse categories(List<@Valid KnowledgeCategoryResponse> categories) {
    this.categories = categories;
    return this;
  }

  public KnowledgeBaseStatusResponse addCategoriesItem(KnowledgeCategoryResponse categoriesItem) {
    if (this.categories == null) {
      this.categories = new ArrayList<>();
    }
    this.categories.add(categoriesItem);
    return this;
  }

  /**
   * Get categories
   * @return categories
   */
  @Valid 
  @Schema(name = "categories", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("categories")
  public List<@Valid KnowledgeCategoryResponse> getCategories() {
    return categories;
  }

  @JsonProperty("categories")
  public void setCategories(List<@Valid KnowledgeCategoryResponse> categories) {
    this.categories = categories;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KnowledgeBaseStatusResponse knowledgeBaseStatusResponse = (KnowledgeBaseStatusResponse) o;
    return Objects.equals(this.totalDocuments, knowledgeBaseStatusResponse.totalDocuments) &&
        Objects.equals(this.totalCategories, knowledgeBaseStatusResponse.totalCategories) &&
        Objects.equals(this.totalStorageBytes, knowledgeBaseStatusResponse.totalStorageBytes) &&
        Objects.equals(this.categories, knowledgeBaseStatusResponse.categories);
  }

  @Override
  public int hashCode() {
    return Objects.hash(totalDocuments, totalCategories, totalStorageBytes, categories);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class KnowledgeBaseStatusResponse {\n");
    sb.append("    totalDocuments: ").append(toIndentedString(totalDocuments)).append("\n");
    sb.append("    totalCategories: ").append(toIndentedString(totalCategories)).append("\n");
    sb.append("    totalStorageBytes: ").append(toIndentedString(totalStorageBytes)).append("\n");
    sb.append("    categories: ").append(toIndentedString(categories)).append("\n");
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

