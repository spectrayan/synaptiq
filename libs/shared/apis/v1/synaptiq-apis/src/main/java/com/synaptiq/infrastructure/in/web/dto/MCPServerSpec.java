package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
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
 * MCP server connection configuration
 */

@Schema(name = "MCPServerSpec", description = "MCP server connection configuration")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-08T22:14:16.718368-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class MCPServerSpec {

  private @Nullable String id;

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

  private TransportEnum transport = TransportEnum.STDIO;

  private @Nullable String command;

  @Valid
  private List<String> args = new ArrayList<>();

  @Valid
  private Map<String, String> env = new HashMap<>();

  private @Nullable String url;

  public MCPServerSpec id(@Nullable String id) {
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

  public MCPServerSpec transport(TransportEnum transport) {
    this.transport = transport;
    return this;
  }

  /**
   * Get transport
   * @return transport
   */
  
  @Schema(name = "transport", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("transport")
  public TransportEnum getTransport() {
    return transport;
  }

  @JsonProperty("transport")
  public void setTransport(TransportEnum transport) {
    this.transport = transport;
  }

  public MCPServerSpec command(@Nullable String command) {
    this.command = command;
    return this;
  }

  /**
   * Get command
   * @return command
   */
  
  @Schema(name = "command", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("command")
  public @Nullable String getCommand() {
    return command;
  }

  @JsonProperty("command")
  public void setCommand(@Nullable String command) {
    this.command = command;
  }

  public MCPServerSpec args(List<String> args) {
    this.args = args;
    return this;
  }

  public MCPServerSpec addArgsItem(String argsItem) {
    if (this.args == null) {
      this.args = new ArrayList<>();
    }
    this.args.add(argsItem);
    return this;
  }

  /**
   * Get args
   * @return args
   */
  
  @Schema(name = "args", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("args")
  public List<String> getArgs() {
    return args;
  }

  @JsonProperty("args")
  public void setArgs(List<String> args) {
    this.args = args;
  }

  public MCPServerSpec env(Map<String, String> env) {
    this.env = env;
    return this;
  }

  public MCPServerSpec putEnvItem(String key, String envItem) {
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

  public MCPServerSpec url(@Nullable String url) {
    this.url = url;
    return this;
  }

  /**
   * Get url
   * @return url
   */
  
  @Schema(name = "url", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("url")
  public @Nullable String getUrl() {
    return url;
  }

  @JsonProperty("url")
  public void setUrl(@Nullable String url) {
    this.url = url;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MCPServerSpec mcPServerSpec = (MCPServerSpec) o;
    return Objects.equals(this.id, mcPServerSpec.id) &&
        Objects.equals(this.transport, mcPServerSpec.transport) &&
        Objects.equals(this.command, mcPServerSpec.command) &&
        Objects.equals(this.args, mcPServerSpec.args) &&
        Objects.equals(this.env, mcPServerSpec.env) &&
        Objects.equals(this.url, mcPServerSpec.url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, transport, command, args, env, url);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MCPServerSpec {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    transport: ").append(toIndentedString(transport)).append("\n");
    sb.append("    command: ").append(toIndentedString(command)).append("\n");
    sb.append("    args: ").append(toIndentedString(args)).append("\n");
    sb.append("    env: ").append(toIndentedString(env)).append("\n");
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
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

