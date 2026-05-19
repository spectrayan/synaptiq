package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.ConnectorType;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.RouteStatus;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Integration route configuration
 */

@Schema(name = "IntegrationResponse", description = "Integration route configuration")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-18T21:07:32.055501800-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class IntegrationResponse {

  private @Nullable String routeConfigId;

  private @Nullable String tenantId;

  private @Nullable String name;

  private @Nullable String description;

  private @Nullable ConnectorType connectorType;

  private @Nullable String templateId;

  @Valid
  private Map<String, String> parameters = new HashMap<>();

  private @Nullable RouteStatus status;

  private @Nullable String credentialRef;

  private @Nullable String camelRouteId;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime lastTestedAt;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime lastExecutedAt;

  private @Nullable String lastError;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime createdAt;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime updatedAt;

  public IntegrationResponse routeConfigId(@Nullable String routeConfigId) {
    this.routeConfigId = routeConfigId;
    return this;
  }

  /**
   * Get routeConfigId
   * @return routeConfigId
   */
  
  @Schema(name = "routeConfigId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("routeConfigId")
  public @Nullable String getRouteConfigId() {
    return routeConfigId;
  }

  @JsonProperty("routeConfigId")
  public void setRouteConfigId(@Nullable String routeConfigId) {
    this.routeConfigId = routeConfigId;
  }

  public IntegrationResponse tenantId(@Nullable String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  /**
   * Get tenantId
   * @return tenantId
   */
  
  @Schema(name = "tenantId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("tenantId")
  public @Nullable String getTenantId() {
    return tenantId;
  }

  @JsonProperty("tenantId")
  public void setTenantId(@Nullable String tenantId) {
    this.tenantId = tenantId;
  }

  public IntegrationResponse name(@Nullable String name) {
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

  public IntegrationResponse description(@Nullable String description) {
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

  public IntegrationResponse connectorType(@Nullable ConnectorType connectorType) {
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

  public IntegrationResponse templateId(@Nullable String templateId) {
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

  public IntegrationResponse parameters(Map<String, String> parameters) {
    this.parameters = parameters;
    return this;
  }

  public IntegrationResponse putParametersItem(String key, String parametersItem) {
    if (this.parameters == null) {
      this.parameters = new HashMap<>();
    }
    this.parameters.put(key, parametersItem);
    return this;
  }

  /**
   * Get parameters
   * @return parameters
   */
  
  @Schema(name = "parameters", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("parameters")
  public Map<String, String> getParameters() {
    return parameters;
  }

  @JsonProperty("parameters")
  public void setParameters(Map<String, String> parameters) {
    this.parameters = parameters;
  }

  public IntegrationResponse status(@Nullable RouteStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
   */
  @Valid 
  @Schema(name = "status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public @Nullable RouteStatus getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(@Nullable RouteStatus status) {
    this.status = status;
  }

  public IntegrationResponse credentialRef(@Nullable String credentialRef) {
    this.credentialRef = credentialRef;
    return this;
  }

  /**
   * Get credentialRef
   * @return credentialRef
   */
  
  @Schema(name = "credentialRef", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("credentialRef")
  public @Nullable String getCredentialRef() {
    return credentialRef;
  }

  @JsonProperty("credentialRef")
  public void setCredentialRef(@Nullable String credentialRef) {
    this.credentialRef = credentialRef;
  }

  public IntegrationResponse camelRouteId(@Nullable String camelRouteId) {
    this.camelRouteId = camelRouteId;
    return this;
  }

  /**
   * Get camelRouteId
   * @return camelRouteId
   */
  
  @Schema(name = "camelRouteId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("camelRouteId")
  public @Nullable String getCamelRouteId() {
    return camelRouteId;
  }

  @JsonProperty("camelRouteId")
  public void setCamelRouteId(@Nullable String camelRouteId) {
    this.camelRouteId = camelRouteId;
  }

  public IntegrationResponse lastTestedAt(@Nullable OffsetDateTime lastTestedAt) {
    this.lastTestedAt = lastTestedAt;
    return this;
  }

  /**
   * Get lastTestedAt
   * @return lastTestedAt
   */
  @Valid 
  @Schema(name = "lastTestedAt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("lastTestedAt")
  public @Nullable OffsetDateTime getLastTestedAt() {
    return lastTestedAt;
  }

  @JsonProperty("lastTestedAt")
  public void setLastTestedAt(@Nullable OffsetDateTime lastTestedAt) {
    this.lastTestedAt = lastTestedAt;
  }

  public IntegrationResponse lastExecutedAt(@Nullable OffsetDateTime lastExecutedAt) {
    this.lastExecutedAt = lastExecutedAt;
    return this;
  }

  /**
   * Get lastExecutedAt
   * @return lastExecutedAt
   */
  @Valid 
  @Schema(name = "lastExecutedAt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("lastExecutedAt")
  public @Nullable OffsetDateTime getLastExecutedAt() {
    return lastExecutedAt;
  }

  @JsonProperty("lastExecutedAt")
  public void setLastExecutedAt(@Nullable OffsetDateTime lastExecutedAt) {
    this.lastExecutedAt = lastExecutedAt;
  }

  public IntegrationResponse lastError(@Nullable String lastError) {
    this.lastError = lastError;
    return this;
  }

  /**
   * Get lastError
   * @return lastError
   */
  
  @Schema(name = "lastError", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("lastError")
  public @Nullable String getLastError() {
    return lastError;
  }

  @JsonProperty("lastError")
  public void setLastError(@Nullable String lastError) {
    this.lastError = lastError;
  }

  public IntegrationResponse createdAt(@Nullable OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * Get createdAt
   * @return createdAt
   */
  @Valid 
  @Schema(name = "createdAt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("createdAt")
  public @Nullable OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  @JsonProperty("createdAt")
  public void setCreatedAt(@Nullable OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public IntegrationResponse updatedAt(@Nullable OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  /**
   * Get updatedAt
   * @return updatedAt
   */
  @Valid 
  @Schema(name = "updatedAt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("updatedAt")
  public @Nullable OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  @JsonProperty("updatedAt")
  public void setUpdatedAt(@Nullable OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IntegrationResponse integrationResponse = (IntegrationResponse) o;
    return Objects.equals(this.routeConfigId, integrationResponse.routeConfigId) &&
        Objects.equals(this.tenantId, integrationResponse.tenantId) &&
        Objects.equals(this.name, integrationResponse.name) &&
        Objects.equals(this.description, integrationResponse.description) &&
        Objects.equals(this.connectorType, integrationResponse.connectorType) &&
        Objects.equals(this.templateId, integrationResponse.templateId) &&
        Objects.equals(this.parameters, integrationResponse.parameters) &&
        Objects.equals(this.status, integrationResponse.status) &&
        Objects.equals(this.credentialRef, integrationResponse.credentialRef) &&
        Objects.equals(this.camelRouteId, integrationResponse.camelRouteId) &&
        Objects.equals(this.lastTestedAt, integrationResponse.lastTestedAt) &&
        Objects.equals(this.lastExecutedAt, integrationResponse.lastExecutedAt) &&
        Objects.equals(this.lastError, integrationResponse.lastError) &&
        Objects.equals(this.createdAt, integrationResponse.createdAt) &&
        Objects.equals(this.updatedAt, integrationResponse.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(routeConfigId, tenantId, name, description, connectorType, templateId, parameters, status, credentialRef, camelRouteId, lastTestedAt, lastExecutedAt, lastError, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IntegrationResponse {\n");
    sb.append("    routeConfigId: ").append(toIndentedString(routeConfigId)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    connectorType: ").append(toIndentedString(connectorType)).append("\n");
    sb.append("    templateId: ").append(toIndentedString(templateId)).append("\n");
    sb.append("    parameters: ").append(toIndentedString(parameters)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    credentialRef: ").append(toIndentedString(credentialRef)).append("\n");
    sb.append("    camelRouteId: ").append(toIndentedString(camelRouteId)).append("\n");
    sb.append("    lastTestedAt: ").append(toIndentedString(lastTestedAt)).append("\n");
    sb.append("    lastExecutedAt: ").append(toIndentedString(lastExecutedAt)).append("\n");
    sb.append("    lastError: ").append(toIndentedString(lastError)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
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

