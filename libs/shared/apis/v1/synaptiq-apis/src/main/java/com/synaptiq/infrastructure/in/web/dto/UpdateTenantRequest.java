package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.synaptiq.infrastructure.in.web.dto.AccessMode;
import com.synaptiq.infrastructure.in.web.dto.PlanTier;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Partial update for tenant org-level fields. All fields are optional.
 */

@Schema(name = "UpdateTenantRequest", description = "Partial update for tenant org-level fields. All fields are optional.")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-05T21:15:25.464614100-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class UpdateTenantRequest {

  private @Nullable String name;

  private @Nullable String slug;

  private @Nullable String catalogLabel;

  private @Nullable AccessMode accessMode;

  private @Nullable PlanTier planTier;

  private @Nullable String dbConnectionUri;

  public UpdateTenantRequest name(@Nullable String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
   */
  @Size(min = 1, max = 200) 
  @Schema(name = "name", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("name")
  public @Nullable String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(@Nullable String name) {
    this.name = name;
  }

  public UpdateTenantRequest slug(@Nullable String slug) {
    this.slug = slug;
    return this;
  }

  /**
   * Get slug
   * @return slug
   */
  @Size(min = 1, max = 100) 
  @Schema(name = "slug", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("slug")
  public @Nullable String getSlug() {
    return slug;
  }

  @JsonProperty("slug")
  public void setSlug(@Nullable String slug) {
    this.slug = slug;
  }

  public UpdateTenantRequest catalogLabel(@Nullable String catalogLabel) {
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

  public UpdateTenantRequest accessMode(@Nullable AccessMode accessMode) {
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

  public UpdateTenantRequest planTier(@Nullable PlanTier planTier) {
    this.planTier = planTier;
    return this;
  }

  /**
   * Get planTier
   * @return planTier
   */
  @Valid 
  @Schema(name = "planTier", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("planTier")
  public @Nullable PlanTier getPlanTier() {
    return planTier;
  }

  @JsonProperty("planTier")
  public void setPlanTier(@Nullable PlanTier planTier) {
    this.planTier = planTier;
  }

  public UpdateTenantRequest dbConnectionUri(@Nullable String dbConnectionUri) {
    this.dbConnectionUri = dbConnectionUri;
    return this;
  }

  /**
   * MongoDB connection URI for tenant-isolated database
   * @return dbConnectionUri
   */
  @Size(max = 500) 
  @Schema(name = "dbConnectionUri", description = "MongoDB connection URI for tenant-isolated database", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("dbConnectionUri")
  public @Nullable String getDbConnectionUri() {
    return dbConnectionUri;
  }

  @JsonProperty("dbConnectionUri")
  public void setDbConnectionUri(@Nullable String dbConnectionUri) {
    this.dbConnectionUri = dbConnectionUri;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UpdateTenantRequest updateTenantRequest = (UpdateTenantRequest) o;
    return Objects.equals(this.name, updateTenantRequest.name) &&
        Objects.equals(this.slug, updateTenantRequest.slug) &&
        Objects.equals(this.catalogLabel, updateTenantRequest.catalogLabel) &&
        Objects.equals(this.accessMode, updateTenantRequest.accessMode) &&
        Objects.equals(this.planTier, updateTenantRequest.planTier) &&
        Objects.equals(this.dbConnectionUri, updateTenantRequest.dbConnectionUri);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, slug, catalogLabel, accessMode, planTier, dbConnectionUri);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UpdateTenantRequest {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    slug: ").append(toIndentedString(slug)).append("\n");
    sb.append("    catalogLabel: ").append(toIndentedString(catalogLabel)).append("\n");
    sb.append("    accessMode: ").append(toIndentedString(accessMode)).append("\n");
    sb.append("    planTier: ").append(toIndentedString(planTier)).append("\n");
    sb.append("    dbConnectionUri: ").append(toIndentedString(dbConnectionUri)).append("\n");
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

