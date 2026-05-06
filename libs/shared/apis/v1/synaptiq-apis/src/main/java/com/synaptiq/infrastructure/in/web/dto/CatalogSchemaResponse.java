package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.synaptiq.infrastructure.in.web.dto.SchemaFieldResponse;
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
 * CatalogSchemaResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-05T21:15:25.464614100-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class CatalogSchemaResponse {

  private @Nullable String id;

  private @Nullable String tenantId;

  private @Nullable String name;

  @Valid
  private List<@Valid SchemaFieldResponse> fields = new ArrayList<>();

  private @Nullable Integer schemaVersion;

  private @Nullable Boolean active;

  public CatalogSchemaResponse id(@Nullable String id) {
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

  public CatalogSchemaResponse tenantId(@Nullable String tenantId) {
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

  public CatalogSchemaResponse name(@Nullable String name) {
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

  public CatalogSchemaResponse fields(List<@Valid SchemaFieldResponse> fields) {
    this.fields = fields;
    return this;
  }

  public CatalogSchemaResponse addFieldsItem(SchemaFieldResponse fieldsItem) {
    if (this.fields == null) {
      this.fields = new ArrayList<>();
    }
    this.fields.add(fieldsItem);
    return this;
  }

  /**
   * Get fields
   * @return fields
   */
  @Valid 
  @Schema(name = "fields", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("fields")
  public List<@Valid SchemaFieldResponse> getFields() {
    return fields;
  }

  @JsonProperty("fields")
  public void setFields(List<@Valid SchemaFieldResponse> fields) {
    this.fields = fields;
  }

  public CatalogSchemaResponse schemaVersion(@Nullable Integer schemaVersion) {
    this.schemaVersion = schemaVersion;
    return this;
  }

  /**
   * Get schemaVersion
   * @return schemaVersion
   */
  
  @Schema(name = "schemaVersion", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("schemaVersion")
  public @Nullable Integer getSchemaVersion() {
    return schemaVersion;
  }

  @JsonProperty("schemaVersion")
  public void setSchemaVersion(@Nullable Integer schemaVersion) {
    this.schemaVersion = schemaVersion;
  }

  public CatalogSchemaResponse active(@Nullable Boolean active) {
    this.active = active;
    return this;
  }

  /**
   * Get active
   * @return active
   */
  
  @Schema(name = "active", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("active")
  public @Nullable Boolean getActive() {
    return active;
  }

  @JsonProperty("active")
  public void setActive(@Nullable Boolean active) {
    this.active = active;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CatalogSchemaResponse catalogSchemaResponse = (CatalogSchemaResponse) o;
    return Objects.equals(this.id, catalogSchemaResponse.id) &&
        Objects.equals(this.tenantId, catalogSchemaResponse.tenantId) &&
        Objects.equals(this.name, catalogSchemaResponse.name) &&
        Objects.equals(this.fields, catalogSchemaResponse.fields) &&
        Objects.equals(this.schemaVersion, catalogSchemaResponse.schemaVersion) &&
        Objects.equals(this.active, catalogSchemaResponse.active);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, tenantId, name, fields, schemaVersion, active);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CatalogSchemaResponse {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    fields: ").append(toIndentedString(fields)).append("\n");
    sb.append("    schemaVersion: ").append(toIndentedString(schemaVersion)).append("\n");
    sb.append("    active: ").append(toIndentedString(active)).append("\n");
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

