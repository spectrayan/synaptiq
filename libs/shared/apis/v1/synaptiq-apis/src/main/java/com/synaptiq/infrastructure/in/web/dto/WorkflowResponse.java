package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.synaptiq.infrastructure.in.web.dto.FlowSettingsSpec;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * WorkflowResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-05T21:15:25.464614100-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class WorkflowResponse {

  private @Nullable String id;

  private @Nullable String tenantId;

  private @Nullable FlowSettingsSpec spec;

  private @Nullable Boolean isPublic;

  private @Nullable String shareToken;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime createdAt;

  public WorkflowResponse id(@Nullable String id) {
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

  public WorkflowResponse tenantId(@Nullable String tenantId) {
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

  public WorkflowResponse spec(@Nullable FlowSettingsSpec spec) {
    this.spec = spec;
    return this;
  }

  /**
   * Get spec
   * @return spec
   */
  @Valid 
  @Schema(name = "spec", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("spec")
  public @Nullable FlowSettingsSpec getSpec() {
    return spec;
  }

  @JsonProperty("spec")
  public void setSpec(@Nullable FlowSettingsSpec spec) {
    this.spec = spec;
  }

  public WorkflowResponse isPublic(@Nullable Boolean isPublic) {
    this.isPublic = isPublic;
    return this;
  }

  /**
   * Get isPublic
   * @return isPublic
   */
  
  @Schema(name = "isPublic", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("isPublic")
  public @Nullable Boolean getIsPublic() {
    return isPublic;
  }

  @JsonProperty("isPublic")
  public void setIsPublic(@Nullable Boolean isPublic) {
    this.isPublic = isPublic;
  }

  public WorkflowResponse shareToken(@Nullable String shareToken) {
    this.shareToken = shareToken;
    return this;
  }

  /**
   * Get shareToken
   * @return shareToken
   */
  
  @Schema(name = "shareToken", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("shareToken")
  public @Nullable String getShareToken() {
    return shareToken;
  }

  @JsonProperty("shareToken")
  public void setShareToken(@Nullable String shareToken) {
    this.shareToken = shareToken;
  }

  public WorkflowResponse createdAt(@Nullable OffsetDateTime createdAt) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WorkflowResponse workflowResponse = (WorkflowResponse) o;
    return Objects.equals(this.id, workflowResponse.id) &&
        Objects.equals(this.tenantId, workflowResponse.tenantId) &&
        Objects.equals(this.spec, workflowResponse.spec) &&
        Objects.equals(this.isPublic, workflowResponse.isPublic) &&
        Objects.equals(this.shareToken, workflowResponse.shareToken) &&
        Objects.equals(this.createdAt, workflowResponse.createdAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, tenantId, spec, isPublic, shareToken, createdAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WorkflowResponse {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    spec: ").append(toIndentedString(spec)).append("\n");
    sb.append("    isPublic: ").append(toIndentedString(isPublic)).append("\n");
    sb.append("    shareToken: ").append(toIndentedString(shareToken)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
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

