package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.synaptiq.infrastructure.in.web.dto.AccessMode;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CreateTenantRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-08T22:14:16.718368-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class CreateTenantRequest {

  private String tenantId;

  private String name;

  private String slug;

  private @Nullable String catalogLabel;

  private @Nullable AccessMode accessMode;

  public CreateTenantRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CreateTenantRequest(String tenantId, String name, String slug) {
    this.tenantId = tenantId;
    this.name = name;
    this.slug = slug;
  }

  public CreateTenantRequest tenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  /**
   * Get tenantId
   * @return tenantId
   */
  @NotNull @Size(min = 1, max = 100) 
  @Schema(name = "tenantId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("tenantId")
  public String getTenantId() {
    return tenantId;
  }

  @JsonProperty("tenantId")
  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public CreateTenantRequest name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
   */
  @NotNull @Size(min = 1, max = 200) 
  @Schema(name = "name", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  public CreateTenantRequest slug(String slug) {
    this.slug = slug;
    return this;
  }

  /**
   * Get slug
   * @return slug
   */
  @NotNull @Size(min = 1, max = 100) 
  @Schema(name = "slug", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("slug")
  public String getSlug() {
    return slug;
  }

  @JsonProperty("slug")
  public void setSlug(String slug) {
    this.slug = slug;
  }

  public CreateTenantRequest catalogLabel(@Nullable String catalogLabel) {
    this.catalogLabel = catalogLabel;
    return this;
  }

  /**
   * Get catalogLabel
   * @return catalogLabel
   */
  @Size(max = 100) 
  @Schema(name = "catalogLabel", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("catalogLabel")
  public @Nullable String getCatalogLabel() {
    return catalogLabel;
  }

  @JsonProperty("catalogLabel")
  public void setCatalogLabel(@Nullable String catalogLabel) {
    this.catalogLabel = catalogLabel;
  }

  public CreateTenantRequest accessMode(@Nullable AccessMode accessMode) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateTenantRequest createTenantRequest = (CreateTenantRequest) o;
    return Objects.equals(this.tenantId, createTenantRequest.tenantId) &&
        Objects.equals(this.name, createTenantRequest.name) &&
        Objects.equals(this.slug, createTenantRequest.slug) &&
        Objects.equals(this.catalogLabel, createTenantRequest.catalogLabel) &&
        Objects.equals(this.accessMode, createTenantRequest.accessMode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tenantId, name, slug, catalogLabel, accessMode);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateTenantRequest {\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    slug: ").append(toIndentedString(slug)).append("\n");
    sb.append("    catalogLabel: ").append(toIndentedString(catalogLabel)).append("\n");
    sb.append("    accessMode: ").append(toIndentedString(accessMode)).append("\n");
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

