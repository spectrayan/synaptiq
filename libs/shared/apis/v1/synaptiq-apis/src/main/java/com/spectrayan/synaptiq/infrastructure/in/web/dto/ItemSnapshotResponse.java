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
 * Snapshot of a saved item at the time it was bookmarked
 */

@Schema(name = "ItemSnapshotResponse", description = "Snapshot of a saved item at the time it was bookmarked")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-10T17:15:52.297398600-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ItemSnapshotResponse {

  private @Nullable String title;

  private @Nullable String subtitle;

  private @Nullable String imageUrl;

  private @Nullable String description;

  private @Nullable String price;

  private @Nullable String category;

  private @Nullable String dataSourceId;

  private @Nullable String sourceItemId;

  public ItemSnapshotResponse title(@Nullable String title) {
    this.title = title;
    return this;
  }

  /**
   * Get title
   * @return title
   */
  
  @Schema(name = "title", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("title")
  public @Nullable String getTitle() {
    return title;
  }

  @JsonProperty("title")
  public void setTitle(@Nullable String title) {
    this.title = title;
  }

  public ItemSnapshotResponse subtitle(@Nullable String subtitle) {
    this.subtitle = subtitle;
    return this;
  }

  /**
   * Get subtitle
   * @return subtitle
   */
  
  @Schema(name = "subtitle", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("subtitle")
  public @Nullable String getSubtitle() {
    return subtitle;
  }

  @JsonProperty("subtitle")
  public void setSubtitle(@Nullable String subtitle) {
    this.subtitle = subtitle;
  }

  public ItemSnapshotResponse imageUrl(@Nullable String imageUrl) {
    this.imageUrl = imageUrl;
    return this;
  }

  /**
   * Get imageUrl
   * @return imageUrl
   */
  
  @Schema(name = "imageUrl", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("imageUrl")
  public @Nullable String getImageUrl() {
    return imageUrl;
  }

  @JsonProperty("imageUrl")
  public void setImageUrl(@Nullable String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public ItemSnapshotResponse description(@Nullable String description) {
    this.description = description;
    return this;
  }

  /**
   * Get description
   * @return description
   */
  
  @Schema(name = "description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("description")
  public @Nullable String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(@Nullable String description) {
    this.description = description;
  }

  public ItemSnapshotResponse price(@Nullable String price) {
    this.price = price;
    return this;
  }

  /**
   * Get price
   * @return price
   */
  
  @Schema(name = "price", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("price")
  public @Nullable String getPrice() {
    return price;
  }

  @JsonProperty("price")
  public void setPrice(@Nullable String price) {
    this.price = price;
  }

  public ItemSnapshotResponse category(@Nullable String category) {
    this.category = category;
    return this;
  }

  /**
   * Get category
   * @return category
   */
  
  @Schema(name = "category", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("category")
  public @Nullable String getCategory() {
    return category;
  }

  @JsonProperty("category")
  public void setCategory(@Nullable String category) {
    this.category = category;
  }

  public ItemSnapshotResponse dataSourceId(@Nullable String dataSourceId) {
    this.dataSourceId = dataSourceId;
    return this;
  }

  /**
   * Get dataSourceId
   * @return dataSourceId
   */
  
  @Schema(name = "dataSourceId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("dataSourceId")
  public @Nullable String getDataSourceId() {
    return dataSourceId;
  }

  @JsonProperty("dataSourceId")
  public void setDataSourceId(@Nullable String dataSourceId) {
    this.dataSourceId = dataSourceId;
  }

  public ItemSnapshotResponse sourceItemId(@Nullable String sourceItemId) {
    this.sourceItemId = sourceItemId;
    return this;
  }

  /**
   * Get sourceItemId
   * @return sourceItemId
   */
  
  @Schema(name = "sourceItemId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("sourceItemId")
  public @Nullable String getSourceItemId() {
    return sourceItemId;
  }

  @JsonProperty("sourceItemId")
  public void setSourceItemId(@Nullable String sourceItemId) {
    this.sourceItemId = sourceItemId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ItemSnapshotResponse itemSnapshotResponse = (ItemSnapshotResponse) o;
    return Objects.equals(this.title, itemSnapshotResponse.title) &&
        Objects.equals(this.subtitle, itemSnapshotResponse.subtitle) &&
        Objects.equals(this.imageUrl, itemSnapshotResponse.imageUrl) &&
        Objects.equals(this.description, itemSnapshotResponse.description) &&
        Objects.equals(this.price, itemSnapshotResponse.price) &&
        Objects.equals(this.category, itemSnapshotResponse.category) &&
        Objects.equals(this.dataSourceId, itemSnapshotResponse.dataSourceId) &&
        Objects.equals(this.sourceItemId, itemSnapshotResponse.sourceItemId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, subtitle, imageUrl, description, price, category, dataSourceId, sourceItemId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ItemSnapshotResponse {\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    subtitle: ").append(toIndentedString(subtitle)).append("\n");
    sb.append("    imageUrl: ").append(toIndentedString(imageUrl)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    price: ").append(toIndentedString(price)).append("\n");
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    dataSourceId: ").append(toIndentedString(dataSourceId)).append("\n");
    sb.append("    sourceItemId: ").append(toIndentedString(sourceItemId)).append("\n");
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

