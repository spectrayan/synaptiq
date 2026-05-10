package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.synaptiq.infrastructure.in.web.dto.AccessMode;
import com.synaptiq.infrastructure.in.web.dto.TenantStatus;
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
 * TenantResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-08T22:14:16.718368-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class TenantResponse {

  private @Nullable String tenantId;

  private @Nullable String name;

  private @Nullable String slug;

  private @Nullable TenantStatus status;

  private @Nullable AccessMode accessMode;

  private @Nullable String catalogLabel;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime createdAt;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime updatedAt;

  public TenantResponse tenantId(@Nullable String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  /**
   * Get tenantId
   * @return tenantId
   */
  
  @Schema(name = "tenantId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("tenantId")
  public @Nullable String getTenantId() {
    return tenantId;
  }

  @JsonProperty("tenantId")
  public void setTenantId(@Nullable String tenantId) {
    this.tenantId = tenantId;
  }

  public TenantResponse name(@Nullable String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
   */
  
  @Schema(name = "name", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("name")
  public @Nullable String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(@Nullable String name) {
    this.name = name;
  }

  public TenantResponse slug(@Nullable String slug) {
    this.slug = slug;
    return this;
  }

  /**
   * Get slug
   * @return slug
   */
  
  @Schema(name = "slug", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("slug")
  public @Nullable String getSlug() {
    return slug;
  }

  @JsonProperty("slug")
  public void setSlug(@Nullable String slug) {
    this.slug = slug;
  }

  public TenantResponse status(@Nullable TenantStatus status) {
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
  public @Nullable TenantStatus getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(@Nullable TenantStatus status) {
    this.status = status;
  }

  public TenantResponse accessMode(@Nullable AccessMode accessMode) {
    this.accessMode = accessMode;
    return this;
  }

  /**
   * Get accessMode
   * @return accessMode
   */
  @Valid 
  @Schema(name = "accessMode", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("accessMode")
  public @Nullable AccessMode getAccessMode() {
    return accessMode;
  }

  @JsonProperty("accessMode")
  public void setAccessMode(@Nullable AccessMode accessMode) {
    this.accessMode = accessMode;
  }

  public TenantResponse catalogLabel(@Nullable String catalogLabel) {
    this.catalogLabel = catalogLabel;
    return this;
  }

  /**
   * Get catalogLabel
   * @return catalogLabel
   */
  
  @Schema(name = "catalogLabel", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("catalogLabel")
  public @Nullable String getCatalogLabel() {
    return catalogLabel;
  }

  @JsonProperty("catalogLabel")
  public void setCatalogLabel(@Nullable String catalogLabel) {
    this.catalogLabel = catalogLabel;
  }

  public TenantResponse createdAt(@Nullable OffsetDateTime createdAt) {
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

  public TenantResponse updatedAt(@Nullable OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  /**
   * Get updatedAt
   * @return updatedAt
   */
  @Valid 
  @Schema(name = "updatedAt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("updatedAt")
  public @Nullable OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  @JsonProperty("updatedAt")
  public void setUpdatedAt(@Nullable OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TenantResponse tenantResponse = (TenantResponse) o;
    return Objects.equals(this.tenantId, tenantResponse.tenantId) &&
        Objects.equals(this.name, tenantResponse.name) &&
        Objects.equals(this.slug, tenantResponse.slug) &&
        Objects.equals(this.status, tenantResponse.status) &&
        Objects.equals(this.accessMode, tenantResponse.accessMode) &&
        Objects.equals(this.catalogLabel, tenantResponse.catalogLabel) &&
        Objects.equals(this.createdAt, tenantResponse.createdAt) &&
        Objects.equals(this.updatedAt, tenantResponse.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tenantId, name, slug, status, accessMode, catalogLabel, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TenantResponse {\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    slug: ").append(toIndentedString(slug)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    accessMode: ").append(toIndentedString(accessMode)).append("\n");
    sb.append("    catalogLabel: ").append(toIndentedString(catalogLabel)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
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

