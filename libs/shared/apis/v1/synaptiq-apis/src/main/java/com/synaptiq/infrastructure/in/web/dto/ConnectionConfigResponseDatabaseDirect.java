package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ConnectionConfigResponseDatabaseDirect
 */

@JsonTypeName("ConnectionConfigResponse_databaseDirect")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-05T21:15:25.464614100-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ConnectionConfigResponseDatabaseDirect {

  private @Nullable String connectionUri;

  private @Nullable String databaseName;

  private @Nullable String collectionOrTable;

  public ConnectionConfigResponseDatabaseDirect connectionUri(@Nullable String connectionUri) {
    this.connectionUri = connectionUri;
    return this;
  }

  /**
   * Get connectionUri
   * @return connectionUri
   */
  
  @Schema(name = "connectionUri", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("connectionUri")
  public @Nullable String getConnectionUri() {
    return connectionUri;
  }

  @JsonProperty("connectionUri")
  public void setConnectionUri(@Nullable String connectionUri) {
    this.connectionUri = connectionUri;
  }

  public ConnectionConfigResponseDatabaseDirect databaseName(@Nullable String databaseName) {
    this.databaseName = databaseName;
    return this;
  }

  /**
   * Get databaseName
   * @return databaseName
   */
  
  @Schema(name = "databaseName", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("databaseName")
  public @Nullable String getDatabaseName() {
    return databaseName;
  }

  @JsonProperty("databaseName")
  public void setDatabaseName(@Nullable String databaseName) {
    this.databaseName = databaseName;
  }

  public ConnectionConfigResponseDatabaseDirect collectionOrTable(@Nullable String collectionOrTable) {
    this.collectionOrTable = collectionOrTable;
    return this;
  }

  /**
   * Get collectionOrTable
   * @return collectionOrTable
   */
  
  @Schema(name = "collectionOrTable", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("collectionOrTable")
  public @Nullable String getCollectionOrTable() {
    return collectionOrTable;
  }

  @JsonProperty("collectionOrTable")
  public void setCollectionOrTable(@Nullable String collectionOrTable) {
    this.collectionOrTable = collectionOrTable;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConnectionConfigResponseDatabaseDirect connectionConfigResponseDatabaseDirect = (ConnectionConfigResponseDatabaseDirect) o;
    return Objects.equals(this.connectionUri, connectionConfigResponseDatabaseDirect.connectionUri) &&
        Objects.equals(this.databaseName, connectionConfigResponseDatabaseDirect.databaseName) &&
        Objects.equals(this.collectionOrTable, connectionConfigResponseDatabaseDirect.collectionOrTable);
  }

  @Override
  public int hashCode() {
    return Objects.hash(connectionUri, databaseName, collectionOrTable);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConnectionConfigResponseDatabaseDirect {\n");
    sb.append("    connectionUri: ").append(toIndentedString(connectionUri)).append("\n");
    sb.append("    databaseName: ").append(toIndentedString(databaseName)).append("\n");
    sb.append("    collectionOrTable: ").append(toIndentedString(collectionOrTable)).append("\n");
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

