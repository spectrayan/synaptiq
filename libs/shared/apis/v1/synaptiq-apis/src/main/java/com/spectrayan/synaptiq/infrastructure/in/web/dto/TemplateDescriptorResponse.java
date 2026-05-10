package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.ConnectorType;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.TemplateParameterResponse;
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
 * Describes a built-in integration template available for tenant use
 */

@Schema(name = "TemplateDescriptorResponse", description = "Describes a built-in integration template available for tenant use")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-10T17:15:52.297398600-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class TemplateDescriptorResponse {

  private @Nullable String templateId;

  private @Nullable String displayName;

  private @Nullable String description;

  private @Nullable String icon;

  private @Nullable String category;

  private @Nullable ConnectorType connectorType;

  private @Nullable Boolean requiresCredential;

  @Valid
  private List<@Valid TemplateParameterResponse> parameters = new ArrayList<>();

  public TemplateDescriptorResponse templateId(@Nullable String templateId) {
    this.templateId = templateId;
    return this;
  }

  /**
   * Get templateId
   * @return templateId
   */
  
  @Schema(name = "templateId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("templateId")
  public @Nullable String getTemplateId() {
    return templateId;
  }

  @JsonProperty("templateId")
  public void setTemplateId(@Nullable String templateId) {
    this.templateId = templateId;
  }

  public TemplateDescriptorResponse displayName(@Nullable String displayName) {
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

  public TemplateDescriptorResponse description(@Nullable String description) {
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

  public TemplateDescriptorResponse icon(@Nullable String icon) {
    this.icon = icon;
    return this;
  }

  /**
   * Get icon
   * @return icon
   */
  
  @Schema(name = "icon", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("icon")
  public @Nullable String getIcon() {
    return icon;
  }

  @JsonProperty("icon")
  public void setIcon(@Nullable String icon) {
    this.icon = icon;
  }

  public TemplateDescriptorResponse category(@Nullable String category) {
    this.category = category;
    return this;
  }

  /**
   * Get category
   * @return category
   */
  
  @Schema(name = "category", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("category")
  public @Nullable String getCategory() {
    return category;
  }

  @JsonProperty("category")
  public void setCategory(@Nullable String category) {
    this.category = category;
  }

  public TemplateDescriptorResponse connectorType(@Nullable ConnectorType connectorType) {
    this.connectorType = connectorType;
    return this;
  }

  /**
   * Get connectorType
   * @return connectorType
   */
  @Valid 
  @Schema(name = "connectorType", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("connectorType")
  public @Nullable ConnectorType getConnectorType() {
    return connectorType;
  }

  @JsonProperty("connectorType")
  public void setConnectorType(@Nullable ConnectorType connectorType) {
    this.connectorType = connectorType;
  }

  public TemplateDescriptorResponse requiresCredential(@Nullable Boolean requiresCredential) {
    this.requiresCredential = requiresCredential;
    return this;
  }

  /**
   * Get requiresCredential
   * @return requiresCredential
   */
  
  @Schema(name = "requiresCredential", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("requiresCredential")
  public @Nullable Boolean getRequiresCredential() {
    return requiresCredential;
  }

  @JsonProperty("requiresCredential")
  public void setRequiresCredential(@Nullable Boolean requiresCredential) {
    this.requiresCredential = requiresCredential;
  }

  public TemplateDescriptorResponse parameters(List<@Valid TemplateParameterResponse> parameters) {
    this.parameters = parameters;
    return this;
  }

  public TemplateDescriptorResponse addParametersItem(TemplateParameterResponse parametersItem) {
    if (this.parameters == null) {
      this.parameters = new ArrayList<>();
    }
    this.parameters.add(parametersItem);
    return this;
  }

  /**
   * Get parameters
   * @return parameters
   */
  @Valid 
  @Schema(name = "parameters", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("parameters")
  public List<@Valid TemplateParameterResponse> getParameters() {
    return parameters;
  }

  @JsonProperty("parameters")
  public void setParameters(List<@Valid TemplateParameterResponse> parameters) {
    this.parameters = parameters;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TemplateDescriptorResponse templateDescriptorResponse = (TemplateDescriptorResponse) o;
    return Objects.equals(this.templateId, templateDescriptorResponse.templateId) &&
        Objects.equals(this.displayName, templateDescriptorResponse.displayName) &&
        Objects.equals(this.description, templateDescriptorResponse.description) &&
        Objects.equals(this.icon, templateDescriptorResponse.icon) &&
        Objects.equals(this.category, templateDescriptorResponse.category) &&
        Objects.equals(this.connectorType, templateDescriptorResponse.connectorType) &&
        Objects.equals(this.requiresCredential, templateDescriptorResponse.requiresCredential) &&
        Objects.equals(this.parameters, templateDescriptorResponse.parameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(templateId, displayName, description, icon, category, connectorType, requiresCredential, parameters);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TemplateDescriptorResponse {\n");
    sb.append("    templateId: ").append(toIndentedString(templateId)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    connectorType: ").append(toIndentedString(connectorType)).append("\n");
    sb.append("    requiresCredential: ").append(toIndentedString(requiresCredential)).append("\n");
    sb.append("    parameters: ").append(toIndentedString(parameters)).append("\n");
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

