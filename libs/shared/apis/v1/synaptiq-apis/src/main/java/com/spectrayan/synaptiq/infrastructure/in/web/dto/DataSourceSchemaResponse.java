package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.DataSourceSchemaResponseFieldsInner;
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
 * Data source schema definition (inferred or manually defined)
 */

@Schema(name = "DataSourceSchemaResponse", description = "Data source schema definition (inferred or manually defined)")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-10T17:15:52.297398600-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class DataSourceSchemaResponse {

  private @Nullable String schemaName;

  @Valid
  private List<@Valid DataSourceSchemaResponseFieldsInner> fields = new ArrayList<>();

  private @Nullable Integer schemaVersion;

  private @Nullable Boolean autoInferred;

  public DataSourceSchemaResponse schemaName(@Nullable String schemaName) {
    this.schemaName = schemaName;
    return this;
  }

  /**
   * Get schemaName
   * @return schemaName
   */
  
  @Schema(name = "schemaName", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("schemaName")
  public @Nullable String getSchemaName() {
    return schemaName;
  }

  @JsonProperty("schemaName")
  public void setSchemaName(@Nullable String schemaName) {
    this.schemaName = schemaName;
  }

  public DataSourceSchemaResponse fields(List<@Valid DataSourceSchemaResponseFieldsInner> fields) {
    this.fields = fields;
    return this;
  }

  public DataSourceSchemaResponse addFieldsItem(DataSourceSchemaResponseFieldsInner fieldsItem) {
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
  public List<@Valid DataSourceSchemaResponseFieldsInner> getFields() {
    return fields;
  }

  @JsonProperty("fields")
  public void setFields(List<@Valid DataSourceSchemaResponseFieldsInner> fields) {
    this.fields = fields;
  }

  public DataSourceSchemaResponse schemaVersion(@Nullable Integer schemaVersion) {
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

  public DataSourceSchemaResponse autoInferred(@Nullable Boolean autoInferred) {
    this.autoInferred = autoInferred;
    return this;
  }

  /**
   * Get autoInferred
   * @return autoInferred
   */
  
  @Schema(name = "autoInferred", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("autoInferred")
  public @Nullable Boolean getAutoInferred() {
    return autoInferred;
  }

  @JsonProperty("autoInferred")
  public void setAutoInferred(@Nullable Boolean autoInferred) {
    this.autoInferred = autoInferred;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DataSourceSchemaResponse dataSourceSchemaResponse = (DataSourceSchemaResponse) o;
    return Objects.equals(this.schemaName, dataSourceSchemaResponse.schemaName) &&
        Objects.equals(this.fields, dataSourceSchemaResponse.fields) &&
        Objects.equals(this.schemaVersion, dataSourceSchemaResponse.schemaVersion) &&
        Objects.equals(this.autoInferred, dataSourceSchemaResponse.autoInferred);
  }

  @Override
  public int hashCode() {
    return Objects.hash(schemaName, fields, schemaVersion, autoInferred);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DataSourceSchemaResponse {\n");
    sb.append("    schemaName: ").append(toIndentedString(schemaName)).append("\n");
    sb.append("    fields: ").append(toIndentedString(fields)).append("\n");
    sb.append("    schemaVersion: ").append(toIndentedString(schemaVersion)).append("\n");
    sb.append("    autoInferred: ").append(toIndentedString(autoInferred)).append("\n");
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

