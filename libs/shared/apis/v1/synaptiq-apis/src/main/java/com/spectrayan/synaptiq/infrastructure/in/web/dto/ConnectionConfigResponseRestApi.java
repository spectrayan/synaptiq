package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.HashMap;
import java.util.Map;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ConnectionConfigResponseRestApi
 */

@JsonTypeName("ConnectionConfigResponse_restApi")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-10T17:15:52.297398600-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ConnectionConfigResponseRestApi {

  private @Nullable String baseUrl;

  @Valid
  private Map<String, String> headers = new HashMap<>();

  private @Nullable String authType;

  public ConnectionConfigResponseRestApi baseUrl(@Nullable String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  /**
   * Get baseUrl
   * @return baseUrl
   */
  
  @Schema(name = "baseUrl", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("baseUrl")
  public @Nullable String getBaseUrl() {
    return baseUrl;
  }

  @JsonProperty("baseUrl")
  public void setBaseUrl(@Nullable String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public ConnectionConfigResponseRestApi headers(Map<String, String> headers) {
    this.headers = headers;
    return this;
  }

  public ConnectionConfigResponseRestApi putHeadersItem(String key, String headersItem) {
    if (this.headers == null) {
      this.headers = new HashMap<>();
    }
    this.headers.put(key, headersItem);
    return this;
  }

  /**
   * Get headers
   * @return headers
   */
  
  @Schema(name = "headers", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("headers")
  public Map<String, String> getHeaders() {
    return headers;
  }

  @JsonProperty("headers")
  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public ConnectionConfigResponseRestApi authType(@Nullable String authType) {
    this.authType = authType;
    return this;
  }

  /**
   * Get authType
   * @return authType
   */
  
  @Schema(name = "authType", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("authType")
  public @Nullable String getAuthType() {
    return authType;
  }

  @JsonProperty("authType")
  public void setAuthType(@Nullable String authType) {
    this.authType = authType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConnectionConfigResponseRestApi connectionConfigResponseRestApi = (ConnectionConfigResponseRestApi) o;
    return Objects.equals(this.baseUrl, connectionConfigResponseRestApi.baseUrl) &&
        Objects.equals(this.headers, connectionConfigResponseRestApi.headers) &&
        Objects.equals(this.authType, connectionConfigResponseRestApi.authType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseUrl, headers, authType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConnectionConfigResponseRestApi {\n");
    sb.append("    baseUrl: ").append(toIndentedString(baseUrl)).append("\n");
    sb.append("    headers: ").append(toIndentedString(headers)).append("\n");
    sb.append("    authType: ").append(toIndentedString(authType)).append("\n");
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

