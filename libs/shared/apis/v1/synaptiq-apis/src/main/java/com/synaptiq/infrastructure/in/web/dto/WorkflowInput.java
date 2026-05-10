package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
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
 * Input field definition for parameterized workflow execution
 */

@Schema(name = "WorkflowInput", description = "Input field definition for parameterized workflow execution")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-08T22:14:16.718368-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class WorkflowInput {

  private String name;

  private @Nullable String label;

  private String type;

  private @Nullable String description;

  private Boolean required = false;

  private @Nullable Object defaultValue = null;

  @Valid
  private List<String> options = new ArrayList<>();

  public WorkflowInput() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public WorkflowInput(String name, String type) {
    this.name = name;
    this.type = type;
  }

  public WorkflowInput name(String name) {
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

  public WorkflowInput label(@Nullable String label) {
    this.label = label;
    return this;
  }

  /**
   * Get label
   * @return label
   */
  
  @Schema(name = "label", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("label")
  public @Nullable String getLabel() {
    return label;
  }

  @JsonProperty("label")
  public void setLabel(@Nullable String label) {
    this.label = label;
  }

  public WorkflowInput type(String type) {
    this.type = type;
    return this;
  }

  /**
   * Input type (text, number, select, boolean, etc.)
   * @return type
   */
  @NotNull 
  @Schema(name = "type", description = "Input type (text, number, select, boolean, etc.)", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("type")
  public String getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(String type) {
    this.type = type;
  }

  public WorkflowInput description(@Nullable String description) {
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

  public WorkflowInput required(Boolean required) {
    this.required = required;
    return this;
  }

  /**
   * Get required
   * @return required
   */
  
  @Schema(name = "required", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("required")
  public Boolean getRequired() {
    return required;
  }

  @JsonProperty("required")
  public void setRequired(Boolean required) {
    this.required = required;
  }

  public WorkflowInput defaultValue(@Nullable Object defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  /**
   * Get defaultValue
   * @return defaultValue
   */
  
  @Schema(name = "defaultValue", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("defaultValue")
  public @Nullable Object getDefaultValue() {
    return defaultValue;
  }

  @JsonProperty("defaultValue")
  public void setDefaultValue(@Nullable Object defaultValue) {
    this.defaultValue = defaultValue;
  }

  public WorkflowInput options(List<String> options) {
    this.options = options;
    return this;
  }

  public WorkflowInput addOptionsItem(String optionsItem) {
    if (this.options == null) {
      this.options = new ArrayList<>();
    }
    this.options.add(optionsItem);
    return this;
  }

  /**
   * Available options for select-type inputs
   * @return options
   */
  
  @Schema(name = "options", description = "Available options for select-type inputs", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("options")
  public List<String> getOptions() {
    return options;
  }

  @JsonProperty("options")
  public void setOptions(List<String> options) {
    this.options = options;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WorkflowInput workflowInput = (WorkflowInput) o;
    return Objects.equals(this.name, workflowInput.name) &&
        Objects.equals(this.label, workflowInput.label) &&
        Objects.equals(this.type, workflowInput.type) &&
        Objects.equals(this.description, workflowInput.description) &&
        Objects.equals(this.required, workflowInput.required) &&
        Objects.equals(this.defaultValue, workflowInput.defaultValue) &&
        Objects.equals(this.options, workflowInput.options);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, label, type, description, required, defaultValue, options);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WorkflowInput {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    label: ").append(toIndentedString(label)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    required: ").append(toIndentedString(required)).append("\n");
    sb.append("    defaultValue: ").append(toIndentedString(defaultValue)).append("\n");
    sb.append("    options: ").append(toIndentedString(options)).append("\n");
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

