package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Describes a single parameter for a template
 */

@Schema(name = "TemplateParameterResponse", description = "Describes a single parameter for a template")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-18T21:07:32.055501800-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class TemplateParameterResponse {

  private @Nullable String name;

  private @Nullable String displayName;

  private @Nullable String description;

  private @Nullable String type;

  private @Nullable Boolean required;

  private @Nullable String defaultValue;

  private @Nullable String placeholder;

  public TemplateParameterResponse name(@Nullable String name) {
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

  public TemplateParameterResponse displayName(@Nullable String displayName) {
    this.displayName = displayName;
    return this;
  }

  /**
   * Get displayName
   * @return displayName
   */
  
  @Schema(name = "displayName", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("displayName")
  public @Nullable String getDisplayName() {
    return displayName;
  }

  @JsonProperty("displayName")
  public void setDisplayName(@Nullable String displayName) {
    this.displayName = displayName;
  }

  public TemplateParameterResponse description(@Nullable String description) {
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

  public TemplateParameterResponse type(@Nullable String type) {
    this.type = type;
    return this;
  }

  /**
   * Parameter type: string, number, boolean, secret, cron
   * @return type
   */
  
  @Schema(name = "type", description = "Parameter type: string, number, boolean, secret, cron", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("type")
  public @Nullable String getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(@Nullable String type) {
    this.type = type;
  }

  public TemplateParameterResponse required(@Nullable Boolean required) {
    this.required = required;
    return this;
  }

  /**
   * Get required
   * @return required
   */
  
  @Schema(name = "required", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("required")
  public @Nullable Boolean getRequired() {
    return required;
  }

  @JsonProperty("required")
  public void setRequired(@Nullable Boolean required) {
    this.required = required;
  }

  public TemplateParameterResponse defaultValue(@Nullable String defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  /**
   * Get defaultValue
   * @return defaultValue
   */
  
  @Schema(name = "defaultValue", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("defaultValue")
  public @Nullable String getDefaultValue() {
    return defaultValue;
  }

  @JsonProperty("defaultValue")
  public void setDefaultValue(@Nullable String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public TemplateParameterResponse placeholder(@Nullable String placeholder) {
    this.placeholder = placeholder;
    return this;
  }

  /**
   * Get placeholder
   * @return placeholder
   */
  
  @Schema(name = "placeholder", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("placeholder")
  public @Nullable String getPlaceholder() {
    return placeholder;
  }

  @JsonProperty("placeholder")
  public void setPlaceholder(@Nullable String placeholder) {
    this.placeholder = placeholder;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TemplateParameterResponse templateParameterResponse = (TemplateParameterResponse) o;
    return Objects.equals(this.name, templateParameterResponse.name) &&
        Objects.equals(this.displayName, templateParameterResponse.displayName) &&
        Objects.equals(this.description, templateParameterResponse.description) &&
        Objects.equals(this.type, templateParameterResponse.type) &&
        Objects.equals(this.required, templateParameterResponse.required) &&
        Objects.equals(this.defaultValue, templateParameterResponse.defaultValue) &&
        Objects.equals(this.placeholder, templateParameterResponse.placeholder);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, displayName, description, type, required, defaultValue, placeholder);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TemplateParameterResponse {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    required: ").append(toIndentedString(required)).append("\n");
    sb.append("    defaultValue: ").append(toIndentedString(defaultValue)).append("\n");
    sb.append("    placeholder: ").append(toIndentedString(placeholder)).append("\n");
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

