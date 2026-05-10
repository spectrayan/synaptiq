package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.ToolDefinitionResponse;
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
 * List of available tools grouped by category
 */

@Schema(name = "ToolCatalogResponse", description = "List of available tools grouped by category")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-10T17:15:52.297398600-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ToolCatalogResponse {

  @Valid
  private List<@Valid ToolDefinitionResponse> tools = new ArrayList<>();

  public ToolCatalogResponse tools(List<@Valid ToolDefinitionResponse> tools) {
    this.tools = tools;
    return this;
  }

  public ToolCatalogResponse addToolsItem(ToolDefinitionResponse toolsItem) {
    if (this.tools == null) {
      this.tools = new ArrayList<>();
    }
    this.tools.add(toolsItem);
    return this;
  }

  /**
   * Get tools
   * @return tools
   */
  @Valid 
  @Schema(name = "tools", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("tools")
  public List<@Valid ToolDefinitionResponse> getTools() {
    return tools;
  }

  @JsonProperty("tools")
  public void setTools(List<@Valid ToolDefinitionResponse> tools) {
    this.tools = tools;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ToolCatalogResponse toolCatalogResponse = (ToolCatalogResponse) o;
    return Objects.equals(this.tools, toolCatalogResponse.tools);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tools);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ToolCatalogResponse {\n");
    sb.append("    tools: ").append(toIndentedString(tools)).append("\n");
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

