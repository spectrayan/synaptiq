package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.ConnectionConfigResponseDatabaseDirect;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.ConnectionConfigResponseMcpServer;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.ConnectionConfigResponseRestApi;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.ConnectionConfigResponseSynaptiqNative;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Connection configuration — polymorphic based on data source type. Only the relevant fields are populated based on the parent DataSource type. 
 */

@Schema(name = "ConnectionConfigResponse", description = "Connection configuration — polymorphic based on data source type. Only the relevant fields are populated based on the parent DataSource type. ")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-10T17:15:52.297398600-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ConnectionConfigResponse {

  private @Nullable ConnectionConfigResponseSynaptiqNative synaptiqNative;

  private @Nullable ConnectionConfigResponseMcpServer mcpServer;

  private @Nullable ConnectionConfigResponseRestApi restApi;

  private @Nullable ConnectionConfigResponseDatabaseDirect databaseDirect;

  public ConnectionConfigResponse synaptiqNative(@Nullable ConnectionConfigResponseSynaptiqNative synaptiqNative) {
    this.synaptiqNative = synaptiqNative;
    return this;
  }

  /**
   * Get synaptiqNative
   * @return synaptiqNative
   */
  @Valid 
  @Schema(name = "synaptiqNative", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("synaptiqNative")
  public @Nullable ConnectionConfigResponseSynaptiqNative getSynaptiqNative() {
    return synaptiqNative;
  }

  @JsonProperty("synaptiqNative")
  public void setSynaptiqNative(@Nullable ConnectionConfigResponseSynaptiqNative synaptiqNative) {
    this.synaptiqNative = synaptiqNative;
  }

  public ConnectionConfigResponse mcpServer(@Nullable ConnectionConfigResponseMcpServer mcpServer) {
    this.mcpServer = mcpServer;
    return this;
  }

  /**
   * Get mcpServer
   * @return mcpServer
   */
  @Valid 
  @Schema(name = "mcpServer", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("mcpServer")
  public @Nullable ConnectionConfigResponseMcpServer getMcpServer() {
    return mcpServer;
  }

  @JsonProperty("mcpServer")
  public void setMcpServer(@Nullable ConnectionConfigResponseMcpServer mcpServer) {
    this.mcpServer = mcpServer;
  }

  public ConnectionConfigResponse restApi(@Nullable ConnectionConfigResponseRestApi restApi) {
    this.restApi = restApi;
    return this;
  }

  /**
   * Get restApi
   * @return restApi
   */
  @Valid 
  @Schema(name = "restApi", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("restApi")
  public @Nullable ConnectionConfigResponseRestApi getRestApi() {
    return restApi;
  }

  @JsonProperty("restApi")
  public void setRestApi(@Nullable ConnectionConfigResponseRestApi restApi) {
    this.restApi = restApi;
  }

  public ConnectionConfigResponse databaseDirect(@Nullable ConnectionConfigResponseDatabaseDirect databaseDirect) {
    this.databaseDirect = databaseDirect;
    return this;
  }

  /**
   * Get databaseDirect
   * @return databaseDirect
   */
  @Valid 
  @Schema(name = "databaseDirect", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("databaseDirect")
  public @Nullable ConnectionConfigResponseDatabaseDirect getDatabaseDirect() {
    return databaseDirect;
  }

  @JsonProperty("databaseDirect")
  public void setDatabaseDirect(@Nullable ConnectionConfigResponseDatabaseDirect databaseDirect) {
    this.databaseDirect = databaseDirect;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConnectionConfigResponse connectionConfigResponse = (ConnectionConfigResponse) o;
    return Objects.equals(this.synaptiqNative, connectionConfigResponse.synaptiqNative) &&
        Objects.equals(this.mcpServer, connectionConfigResponse.mcpServer) &&
        Objects.equals(this.restApi, connectionConfigResponse.restApi) &&
        Objects.equals(this.databaseDirect, connectionConfigResponse.databaseDirect);
  }

  @Override
  public int hashCode() {
    return Objects.hash(synaptiqNative, mcpServer, restApi, databaseDirect);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConnectionConfigResponse {\n");
    sb.append("    synaptiqNative: ").append(toIndentedString(synaptiqNative)).append("\n");
    sb.append("    mcpServer: ").append(toIndentedString(mcpServer)).append("\n");
    sb.append("    restApi: ").append(toIndentedString(restApi)).append("\n");
    sb.append("    databaseDirect: ").append(toIndentedString(databaseDirect)).append("\n");
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

