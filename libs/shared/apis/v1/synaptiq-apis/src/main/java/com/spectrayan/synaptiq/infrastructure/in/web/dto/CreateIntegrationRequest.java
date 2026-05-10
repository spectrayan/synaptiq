package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.ConnectorType;
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
 * Request to create a new integration
 */

@Schema(name = "CreateIntegrationRequest", description = "Request to create a new integration")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-10T17:15:52.297398600-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class CreateIntegrationRequest {

  private String name;

  private @Nullable String description;

  private ConnectorType connectorType;

  private @Nullable String templateId;

  @Valid
  private Map<String, String> parameters = new HashMap<>();

  private @Nullable String credentialRef;

  public CreateIntegrationRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CreateIntegrationRequest(String name, ConnectorType connectorType) {
    this.name = name;
    this.connectorType = connectorType;
  }

  public CreateIntegrationRequest name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
   */
  @NotNull @Size(min = 1, max = 200) 
  @Schema(name = "name", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  public CreateIntegrationRequest description(@Nullable String description) {
    this.description = description;
    return this;
  }

  /**
   * Get description
   * @return description
   */
  @Size(max = 1000) 
  @Schema(name = "description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("description")
  public @Nullable String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(@Nullable String description) {
    this.description = description;
  }

  public CreateIntegrationRequest connectorType(ConnectorType connectorType) {
    this.connectorType = connectorType;
    return this;
  }

  /**
   * Get connectorType
   * @return connectorType
   */
  @NotNull @Valid 
  @Schema(name = "connectorType", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("connectorType")
  public ConnectorType getConnectorType() {
    return connectorType;
  }

  @JsonProperty("connectorType")
  public void setConnectorType(ConnectorType connectorType) {
    this.connectorType = connectorType;
  }

  public CreateIntegrationRequest templateId(@Nullable String templateId) {
    this.templateId = templateId;
    return this;
  }

  /**
   * Built-in template ID to instantiate (e.g., \"rest-api-poll\", \"slack-notify\")
   * @return templateId
   */
  
  @Schema(name = "templateId", description = "Built-in template ID to instantiate (e.g., \"rest-api-poll\", \"slack-notify\")", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("templateId")
  public @Nullable String getTemplateId() {
    return templateId;
  }

  @JsonProperty("templateId")
  public void setTemplateId(@Nullable String templateId) {
    this.templateId = templateId;
  }

  public CreateIntegrationRequest parameters(Map<String, String> parameters) {
    this.parameters = parameters;
    return this;
  }

  public CreateIntegrationRequest putParametersItem(String key, String parametersItem) {
    if (this.parameters == null) {
      this.parameters = new HashMap<>();
    }
    this.parameters.put(key, parametersItem);
    return this;
  }

  /**
   * Key-value parameters for the template or adapter
   * @return parameters
   */
  
  @Schema(name = "parameters", description = "Key-value parameters for the template or adapter", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("parameters")
  public Map<String, String> getParameters() {
    return parameters;
  }

  @JsonProperty("parameters")
  public void setParameters(Map<String, String> parameters) {
    this.parameters = parameters;
  }

  public CreateIntegrationRequest credentialRef(@Nullable String credentialRef) {
    this.credentialRef = credentialRef;
    return this;
  }

  /**
   * Reference to credential/secret for authentication
   * @return credentialRef
   */
  
  @Schema(name = "credentialRef", description = "Reference to credential/secret for authentication", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("credentialRef")
  public @Nullable String getCredentialRef() {
    return credentialRef;
  }

  @JsonProperty("credentialRef")
  public void setCredentialRef(@Nullable String credentialRef) {
    this.credentialRef = credentialRef;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateIntegrationRequest createIntegrationRequest = (CreateIntegrationRequest) o;
    return Objects.equals(this.name, createIntegrationRequest.name) &&
        Objects.equals(this.description, createIntegrationRequest.description) &&
        Objects.equals(this.connectorType, createIntegrationRequest.connectorType) &&
        Objects.equals(this.templateId, createIntegrationRequest.templateId) &&
        Objects.equals(this.parameters, createIntegrationRequest.parameters) &&
        Objects.equals(this.credentialRef, createIntegrationRequest.credentialRef);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, connectorType, templateId, parameters, credentialRef);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateIntegrationRequest {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    connectorType: ").append(toIndentedString(connectorType)).append("\n");
    sb.append("    templateId: ").append(toIndentedString(templateId)).append("\n");
    sb.append("    parameters: ").append(toIndentedString(parameters)).append("\n");
    sb.append("    credentialRef: ").append(toIndentedString(credentialRef)).append("\n");
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

