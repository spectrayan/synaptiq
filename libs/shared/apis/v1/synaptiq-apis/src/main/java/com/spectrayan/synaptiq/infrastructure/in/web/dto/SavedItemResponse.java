package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.ItemSnapshotResponse;
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
 * SavedItemResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-10T17:15:52.297398600-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class SavedItemResponse {

  private @Nullable String id;

  private @Nullable String itemId;

  private @Nullable String sessionId;

  private @Nullable ItemSnapshotResponse itemSnapshot;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime createdAt;

  public SavedItemResponse id(@Nullable String id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
   */
  
  @Schema(name = "id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("id")
  public @Nullable String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(@Nullable String id) {
    this.id = id;
  }

  public SavedItemResponse itemId(@Nullable String itemId) {
    this.itemId = itemId;
    return this;
  }

  /**
   * Get itemId
   * @return itemId
   */
  
  @Schema(name = "itemId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("itemId")
  public @Nullable String getItemId() {
    return itemId;
  }

  @JsonProperty("itemId")
  public void setItemId(@Nullable String itemId) {
    this.itemId = itemId;
  }

  public SavedItemResponse sessionId(@Nullable String sessionId) {
    this.sessionId = sessionId;
    return this;
  }

  /**
   * Get sessionId
   * @return sessionId
   */
  
  @Schema(name = "sessionId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("sessionId")
  public @Nullable String getSessionId() {
    return sessionId;
  }

  @JsonProperty("sessionId")
  public void setSessionId(@Nullable String sessionId) {
    this.sessionId = sessionId;
  }

  public SavedItemResponse itemSnapshot(@Nullable ItemSnapshotResponse itemSnapshot) {
    this.itemSnapshot = itemSnapshot;
    return this;
  }

  /**
   * Get itemSnapshot
   * @return itemSnapshot
   */
  @Valid 
  @Schema(name = "itemSnapshot", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("itemSnapshot")
  public @Nullable ItemSnapshotResponse getItemSnapshot() {
    return itemSnapshot;
  }

  @JsonProperty("itemSnapshot")
  public void setItemSnapshot(@Nullable ItemSnapshotResponse itemSnapshot) {
    this.itemSnapshot = itemSnapshot;
  }

  public SavedItemResponse createdAt(@Nullable OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * Get createdAt
   * @return createdAt
   */
  @Valid 
  @Schema(name = "createdAt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("createdAt")
  public @Nullable OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  @JsonProperty("createdAt")
  public void setCreatedAt(@Nullable OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SavedItemResponse savedItemResponse = (SavedItemResponse) o;
    return Objects.equals(this.id, savedItemResponse.id) &&
        Objects.equals(this.itemId, savedItemResponse.itemId) &&
        Objects.equals(this.sessionId, savedItemResponse.sessionId) &&
        Objects.equals(this.itemSnapshot, savedItemResponse.itemSnapshot) &&
        Objects.equals(this.createdAt, savedItemResponse.createdAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, itemId, sessionId, itemSnapshot, createdAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SavedItemResponse {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    itemId: ").append(toIndentedString(itemId)).append("\n");
    sb.append("    sessionId: ").append(toIndentedString(sessionId)).append("\n");
    sb.append("    itemSnapshot: ").append(toIndentedString(itemSnapshot)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
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

