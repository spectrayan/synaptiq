package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.ConnectionConfigResponse;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.CredentialRefResponse;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.DataSourceSchemaResponse;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.DataSourceStatus;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.DataSourceType;
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
 * Data source configuration response
 */

@Schema(name = "DataSourceResponse", description = "Data source configuration response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-10T17:15:52.297398600-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class DataSourceResponse {

  private @Nullable String dataSourceId;

  private @Nullable String name;

  private @Nullable String description;

  private @Nullable DataSourceType type;

  private @Nullable DataSourceStatus status;

  private @Nullable DataSourceSchemaResponse schema;

  private @Nullable ConnectionConfigResponse connection;

  private @Nullable CredentialRefResponse credentialRef;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime lastSyncedAt;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime createdAt;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime updatedAt;

  public DataSourceResponse dataSourceId(@Nullable String dataSourceId) {
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

  public DataSourceResponse name(@Nullable String name) {
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

  public DataSourceResponse description(@Nullable String description) {
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

  public DataSourceResponse type(@Nullable DataSourceType type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   * @return type
   */
  @Valid 
  @Schema(name = "type", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("type")
  public @Nullable DataSourceType getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(@Nullable DataSourceType type) {
    this.type = type;
  }

  public DataSourceResponse status(@Nullable DataSourceStatus status) {
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
  public @Nullable DataSourceStatus getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(@Nullable DataSourceStatus status) {
    this.status = status;
  }

  public DataSourceResponse schema(@Nullable DataSourceSchemaResponse schema) {
    this.schema = schema;
    return this;
  }

  /**
   * Get schema
   * @return schema
   */
  @Valid 
  @Schema(name = "schema", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("schema")
  public @Nullable DataSourceSchemaResponse getSchema() {
    return schema;
  }

  @JsonProperty("schema")
  public void setSchema(@Nullable DataSourceSchemaResponse schema) {
    this.schema = schema;
  }

  public DataSourceResponse connection(@Nullable ConnectionConfigResponse connection) {
    this.connection = connection;
    return this;
  }

  /**
   * Get connection
   * @return connection
   */
  @Valid 
  @Schema(name = "connection", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("connection")
  public @Nullable ConnectionConfigResponse getConnection() {
    return connection;
  }

  @JsonProperty("connection")
  public void setConnection(@Nullable ConnectionConfigResponse connection) {
    this.connection = connection;
  }

  public DataSourceResponse credentialRef(@Nullable CredentialRefResponse credentialRef) {
    this.credentialRef = credentialRef;
    return this;
  }

  /**
   * Get credentialRef
   * @return credentialRef
   */
  @Valid 
  @Schema(name = "credentialRef", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("credentialRef")
  public @Nullable CredentialRefResponse getCredentialRef() {
    return credentialRef;
  }

  @JsonProperty("credentialRef")
  public void setCredentialRef(@Nullable CredentialRefResponse credentialRef) {
    this.credentialRef = credentialRef;
  }

  public DataSourceResponse lastSyncedAt(@Nullable OffsetDateTime lastSyncedAt) {
    this.lastSyncedAt = lastSyncedAt;
    return this;
  }

  /**
   * Get lastSyncedAt
   * @return lastSyncedAt
   */
  @Valid 
  @Schema(name = "lastSyncedAt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("lastSyncedAt")
  public @Nullable OffsetDateTime getLastSyncedAt() {
    return lastSyncedAt;
  }

  @JsonProperty("lastSyncedAt")
  public void setLastSyncedAt(@Nullable OffsetDateTime lastSyncedAt) {
    this.lastSyncedAt = lastSyncedAt;
  }

  public DataSourceResponse createdAt(@Nullable OffsetDateTime createdAt) {
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

  public DataSourceResponse updatedAt(@Nullable OffsetDateTime updatedAt) {
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
    DataSourceResponse dataSourceResponse = (DataSourceResponse) o;
    return Objects.equals(this.dataSourceId, dataSourceResponse.dataSourceId) &&
        Objects.equals(this.name, dataSourceResponse.name) &&
        Objects.equals(this.description, dataSourceResponse.description) &&
        Objects.equals(this.type, dataSourceResponse.type) &&
        Objects.equals(this.status, dataSourceResponse.status) &&
        Objects.equals(this.schema, dataSourceResponse.schema) &&
        Objects.equals(this.connection, dataSourceResponse.connection) &&
        Objects.equals(this.credentialRef, dataSourceResponse.credentialRef) &&
        Objects.equals(this.lastSyncedAt, dataSourceResponse.lastSyncedAt) &&
        Objects.equals(this.createdAt, dataSourceResponse.createdAt) &&
        Objects.equals(this.updatedAt, dataSourceResponse.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dataSourceId, name, description, type, status, schema, connection, credentialRef, lastSyncedAt, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DataSourceResponse {\n");
    sb.append("    dataSourceId: ").append(toIndentedString(dataSourceId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    schema: ").append(toIndentedString(schema)).append("\n");
    sb.append("    connection: ").append(toIndentedString(connection)).append("\n");
    sb.append("    credentialRef: ").append(toIndentedString(credentialRef)).append("\n");
    sb.append("    lastSyncedAt: ").append(toIndentedString(lastSyncedAt)).append("\n");
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

