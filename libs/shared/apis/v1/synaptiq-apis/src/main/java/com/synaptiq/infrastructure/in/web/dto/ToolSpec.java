package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
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
 * Tool configuration for an agent
 */

@Schema(name = "ToolSpec", description = "Tool configuration for an agent")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-05T21:15:25.464614100-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ToolSpec {

  private @Nullable String id;

  /**
   * Gets or Sets type
   */
  public enum TypeEnum {
    PYTHON("PYTHON"),
    
    JAVA("JAVA"),
    
    MCP("MCP"),
    
    LANGCHAIN("LANGCHAIN"),
    
    SPRING_BEAN("SPRING_BEAN");

    private final String value;

    TypeEnum(String value) {
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
    public static TypeEnum fromValue(String value) {
      for (TypeEnum b : TypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private @Nullable TypeEnum type;

  private @Nullable String name;

  private @Nullable String importPath;

  private @Nullable String mcpServer;

  private @Nullable String mcpTool;

  @Valid
  private Map<String, Object> params = new HashMap<>();

  public ToolSpec id(@Nullable String id) {
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

  public ToolSpec type(@Nullable TypeEnum type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   * @return type
   */
  
  @Schema(name = "type", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("type")
  public @Nullable TypeEnum getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(@Nullable TypeEnum type) {
    this.type = type;
  }

  public ToolSpec name(@Nullable String name) {
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

  public ToolSpec importPath(@Nullable String importPath) {
    this.importPath = importPath;
    return this;
  }

  /**
   * Get importPath
   * @return importPath
   */
  
  @Schema(name = "importPath", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("importPath")
  public @Nullable String getImportPath() {
    return importPath;
  }

  @JsonProperty("importPath")
  public void setImportPath(@Nullable String importPath) {
    this.importPath = importPath;
  }

  public ToolSpec mcpServer(@Nullable String mcpServer) {
    this.mcpServer = mcpServer;
    return this;
  }

  /**
   * Get mcpServer
   * @return mcpServer
   */
  
  @Schema(name = "mcpServer", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("mcpServer")
  public @Nullable String getMcpServer() {
    return mcpServer;
  }

  @JsonProperty("mcpServer")
  public void setMcpServer(@Nullable String mcpServer) {
    this.mcpServer = mcpServer;
  }

  public ToolSpec mcpTool(@Nullable String mcpTool) {
    this.mcpTool = mcpTool;
    return this;
  }

  /**
   * Get mcpTool
   * @return mcpTool
   */
  
  @Schema(name = "mcpTool", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("mcpTool")
  public @Nullable String getMcpTool() {
    return mcpTool;
  }

  @JsonProperty("mcpTool")
  public void setMcpTool(@Nullable String mcpTool) {
    this.mcpTool = mcpTool;
  }

  public ToolSpec params(Map<String, Object> params) {
    this.params = params;
    return this;
  }

  public ToolSpec putParamsItem(String key, Object paramsItem) {
    if (this.params == null) {
      this.params = new HashMap<>();
    }
    this.params.put(key, paramsItem);
    return this;
  }

  /**
   * Get params
   * @return params
   */
  
  @Schema(name = "params", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("params")
  public Map<String, Object> getParams() {
    return params;
  }

  @JsonProperty("params")
  public void setParams(Map<String, Object> params) {
    this.params = params;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ToolSpec toolSpec = (ToolSpec) o;
    return Objects.equals(this.id, toolSpec.id) &&
        Objects.equals(this.type, toolSpec.type) &&
        Objects.equals(this.name, toolSpec.name) &&
        Objects.equals(this.importPath, toolSpec.importPath) &&
        Objects.equals(this.mcpServer, toolSpec.mcpServer) &&
        Objects.equals(this.mcpTool, toolSpec.mcpTool) &&
        Objects.equals(this.params, toolSpec.params);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, type, name, importPath, mcpServer, mcpTool, params);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ToolSpec {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    importPath: ").append(toIndentedString(importPath)).append("\n");
    sb.append("    mcpServer: ").append(toIndentedString(mcpServer)).append("\n");
    sb.append("    mcpTool: ").append(toIndentedString(mcpTool)).append("\n");
    sb.append("    params: ").append(toIndentedString(params)).append("\n");
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

