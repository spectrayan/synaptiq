package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.synaptiq.infrastructure.in.web.dto.ConnectionConfigResponse;
import com.synaptiq.infrastructure.in.web.dto.CredentialRefResponse;
import com.synaptiq.infrastructure.in.web.dto.DataSourceType;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Create a new data source
 */

@Schema(name = "CreateDataSourceRequest", description = "Create a new data source")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-08T22:14:16.718368-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class CreateDataSourceRequest {

  private String name;

  private @Nullable String description;

  private DataSourceType type;

  private @Nullable ConnectionConfigResponse connection;

  private @Nullable CredentialRefResponse credentialRef;

  public CreateDataSourceRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CreateDataSourceRequest(String name, DataSourceType type) {
    this.name = name;
    this.type = type;
  }

  public CreateDataSourceRequest name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
   */
  @NotNull 
  @Schema(name = "name", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  public CreateDataSourceRequest description(@Nullable String description) {
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

  public CreateDataSourceRequest type(DataSourceType type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   * @return type
   */
  @NotNull @Valid 
  @Schema(name = "type", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("type")
  public DataSourceType getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(DataSourceType type) {
    this.type = type;
  }

  public CreateDataSourceRequest connection(@Nullable ConnectionConfigResponse connection) {
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

  public CreateDataSourceRequest credentialRef(@Nullable CredentialRefResponse credentialRef) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateDataSourceRequest createDataSourceRequest = (CreateDataSourceRequest) o;
    return Objects.equals(this.name, createDataSourceRequest.name) &&
        Objects.equals(this.description, createDataSourceRequest.description) &&
        Objects.equals(this.type, createDataSourceRequest.type) &&
        Objects.equals(this.connection, createDataSourceRequest.connection) &&
        Objects.equals(this.credentialRef, createDataSourceRequest.credentialRef);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, type, connection, credentialRef);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateDataSourceRequest {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    connection: ").append(toIndentedString(connection)).append("\n");
    sb.append("    credentialRef: ").append(toIndentedString(credentialRef)).append("\n");
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

