package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ConnectionConfigResponseMcpServer
 */

@JsonTypeName("ConnectionConfigResponse_mcpServer")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-18T21:07:32.055501800-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ConnectionConfigResponseMcpServer {

  private @Nullable String serverUrl;

  /**
   * Gets or Sets transport
   */
  public enum TransportEnum {
    STDIO("STDIO"),
    
    HTTP("HTTP"),
    
    WS("WS");

    private final String value;

    TransportEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static TransportEnum fromValue(String value) {
      for (TransportEnum b : TransportEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private @Nullable TransportEnum transport;

  @Valid
  private Map<String, String> env = new HashMap<>();

  @Valid
  private List<String> enabledTools = new ArrayList<>();

  public ConnectionConfigResponseMcpServer serverUrl(@Nullable String serverUrl) {
    this.serverUrl = serverUrl;
    return this;
  }

  /**
   * Get serverUrl
   * @return serverUrl
   */
  
  @Schema(name = "serverUrl", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("serverUrl")
  public @Nullable String getServerUrl() {
    return serverUrl;
  }

  @JsonProperty("serverUrl")
  public void setServerUrl(@Nullable String serverUrl) {
    this.serverUrl = serverUrl;
  }

  public ConnectionConfigResponseMcpServer transport(@Nullable TransportEnum transport) {
    this.transport = transport;
    return this;
  }

  /**
   * Get transport
   * @return transport
   */
  
  @Schema(name = "transport", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("transport")
  public @Nullable TransportEnum getTransport() {
    return transport;
  }

  @JsonProperty("transport")
  public void setTransport(@Nullable TransportEnum transport) {
    this.transport = transport;
  }

  public ConnectionConfigResponseMcpServer env(Map<String, String> env) {
    this.env = env;
    return this;
  }

  public ConnectionConfigResponseMcpServer putEnvItem(String key, String envItem) {
    if (this.env == null) {
      this.env = new HashMap<>();
    }
    this.env.put(key, envItem);
    return this;
  }

  /**
   * Get env
   * @return env
   */
  
  @Schema(name = "env", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("env")
  public Map<String, String> getEnv() {
    return env;
  }

  @JsonProperty("env")
  public void setEnv(Map<String, String> env) {
    this.env = env;
  }

  public ConnectionConfigResponseMcpServer enabledTools(List<String> enabledTools) {
    this.enabledTools = enabledTools;
    return this;
  }

  public ConnectionConfigResponseMcpServer addEnabledToolsItem(String enabledToolsItem) {
    if (this.enabledTools == null) {
      this.enabledTools = new ArrayList<>();
    }
    this.enabledTools.add(enabledToolsItem);
    return this;
  }

  /**
   * Get enabledTools
   * @return enabledTools
   */
  
  @Schema(name = "enabledTools", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("enabledTools")
  public List<String> getEnabledTools() {
    return enabledTools;
  }

  @JsonProperty("enabledTools")
  public void setEnabledTools(List<String> enabledTools) {
    this.enabledTools = enabledTools;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConnectionConfigResponseMcpServer connectionConfigResponseMcpServer = (ConnectionConfigResponseMcpServer) o;
    return Objects.equals(this.serverUrl, connectionConfigResponseMcpServer.serverUrl) &&
        Objects.equals(this.transport, connectionConfigResponseMcpServer.transport) &&
        Objects.equals(this.env, connectionConfigResponseMcpServer.env) &&
        Objects.equals(this.enabledTools, connectionConfigResponseMcpServer.enabledTools);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serverUrl, transport, env, enabledTools);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConnectionConfigResponseMcpServer {\n");
    sb.append("    serverUrl: ").append(toIndentedString(serverUrl)).append("\n");
    sb.append("    transport: ").append(toIndentedString(transport)).append("\n");
    sb.append("    env: ").append(toIndentedString(env)).append("\n");
    sb.append("    enabledTools: ").append(toIndentedString(enabledTools)).append("\n");
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

