package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.synaptiq.infrastructure.in.web.dto.ActionsConfigResponse;
import com.synaptiq.infrastructure.in.web.dto.AiPersonaResponse;
import com.synaptiq.infrastructure.in.web.dto.ApplicationStatus;
import com.synaptiq.infrastructure.in.web.dto.BrandingResponse;
import com.synaptiq.infrastructure.in.web.dto.ComponentsToggleResponse;
import com.synaptiq.infrastructure.in.web.dto.GuardrailsResponse;
import com.synaptiq.infrastructure.in.web.dto.LlmProviderResponse;
import com.synaptiq.infrastructure.in.web.dto.PersonalizationResponse;
import com.synaptiq.infrastructure.in.web.dto.ThemePresetResponse;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Full application configuration response
 */

@Schema(name = "ApplicationResponse", description = "Full application configuration response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-08T22:14:16.718368-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ApplicationResponse {

  private @Nullable String appId;

  private @Nullable String slug;

  private @Nullable String name;

  private @Nullable String description;

  private @Nullable String icon;

  private @Nullable Boolean isDefault;

  private @Nullable ApplicationStatus status;

  private @Nullable AiPersonaResponse aiPersona;

  private @Nullable GuardrailsResponse guardrails;

  private @Nullable BrandingResponse branding;

  private @Nullable ComponentsToggleResponse components;

  private @Nullable ActionsConfigResponse actions;

  private @Nullable PersonalizationResponse personalization;

  @Valid
  private List<@Valid ThemePresetResponse> themes = new ArrayList<>();

  @Valid
  private List<String> dataSourceIds = new ArrayList<>();

  private @Nullable LlmProviderResponse llmOverride;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime createdAt;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime updatedAt;

  public ApplicationResponse appId(@Nullable String appId) {
    this.appId = appId;
    return this;
  }

  /**
   * Get appId
   * @return appId
   */
  
  @Schema(name = "appId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("appId")
  public @Nullable String getAppId() {
    return appId;
  }

  @JsonProperty("appId")
  public void setAppId(@Nullable String appId) {
    this.appId = appId;
  }

  public ApplicationResponse slug(@Nullable String slug) {
    this.slug = slug;
    return this;
  }

  /**
   * Get slug
   * @return slug
   */
  
  @Schema(name = "slug", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("slug")
  public @Nullable String getSlug() {
    return slug;
  }

  @JsonProperty("slug")
  public void setSlug(@Nullable String slug) {
    this.slug = slug;
  }

  public ApplicationResponse name(@Nullable String name) {
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

  public ApplicationResponse description(@Nullable String description) {
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

  public ApplicationResponse icon(@Nullable String icon) {
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

  public ApplicationResponse isDefault(@Nullable Boolean isDefault) {
    this.isDefault = isDefault;
    return this;
  }

  /**
   * Get isDefault
   * @return isDefault
   */
  
  @Schema(name = "isDefault", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("isDefault")
  public @Nullable Boolean getIsDefault() {
    return isDefault;
  }

  @JsonProperty("isDefault")
  public void setIsDefault(@Nullable Boolean isDefault) {
    this.isDefault = isDefault;
  }

  public ApplicationResponse status(@Nullable ApplicationStatus status) {
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
  public @Nullable ApplicationStatus getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(@Nullable ApplicationStatus status) {
    this.status = status;
  }

  public ApplicationResponse aiPersona(@Nullable AiPersonaResponse aiPersona) {
    this.aiPersona = aiPersona;
    return this;
  }

  /**
   * Get aiPersona
   * @return aiPersona
   */
  @Valid 
  @Schema(name = "aiPersona", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("aiPersona")
  public @Nullable AiPersonaResponse getAiPersona() {
    return aiPersona;
  }

  @JsonProperty("aiPersona")
  public void setAiPersona(@Nullable AiPersonaResponse aiPersona) {
    this.aiPersona = aiPersona;
  }

  public ApplicationResponse guardrails(@Nullable GuardrailsResponse guardrails) {
    this.guardrails = guardrails;
    return this;
  }

  /**
   * Get guardrails
   * @return guardrails
   */
  @Valid 
  @Schema(name = "guardrails", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("guardrails")
  public @Nullable GuardrailsResponse getGuardrails() {
    return guardrails;
  }

  @JsonProperty("guardrails")
  public void setGuardrails(@Nullable GuardrailsResponse guardrails) {
    this.guardrails = guardrails;
  }

  public ApplicationResponse branding(@Nullable BrandingResponse branding) {
    this.branding = branding;
    return this;
  }

  /**
   * Get branding
   * @return branding
   */
  @Valid 
  @Schema(name = "branding", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("branding")
  public @Nullable BrandingResponse getBranding() {
    return branding;
  }

  @JsonProperty("branding")
  public void setBranding(@Nullable BrandingResponse branding) {
    this.branding = branding;
  }

  public ApplicationResponse components(@Nullable ComponentsToggleResponse components) {
    this.components = components;
    return this;
  }

  /**
   * Get components
   * @return components
   */
  @Valid 
  @Schema(name = "components", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("components")
  public @Nullable ComponentsToggleResponse getComponents() {
    return components;
  }

  @JsonProperty("components")
  public void setComponents(@Nullable ComponentsToggleResponse components) {
    this.components = components;
  }

  public ApplicationResponse actions(@Nullable ActionsConfigResponse actions) {
    this.actions = actions;
    return this;
  }

  /**
   * Get actions
   * @return actions
   */
  @Valid 
  @Schema(name = "actions", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("actions")
  public @Nullable ActionsConfigResponse getActions() {
    return actions;
  }

  @JsonProperty("actions")
  public void setActions(@Nullable ActionsConfigResponse actions) {
    this.actions = actions;
  }

  public ApplicationResponse personalization(@Nullable PersonalizationResponse personalization) {
    this.personalization = personalization;
    return this;
  }

  /**
   * Get personalization
   * @return personalization
   */
  @Valid 
  @Schema(name = "personalization", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("personalization")
  public @Nullable PersonalizationResponse getPersonalization() {
    return personalization;
  }

  @JsonProperty("personalization")
  public void setPersonalization(@Nullable PersonalizationResponse personalization) {
    this.personalization = personalization;
  }

  public ApplicationResponse themes(List<@Valid ThemePresetResponse> themes) {
    this.themes = themes;
    return this;
  }

  public ApplicationResponse addThemesItem(ThemePresetResponse themesItem) {
    if (this.themes == null) {
      this.themes = new ArrayList<>();
    }
    this.themes.add(themesItem);
    return this;
  }

  /**
   * Get themes
   * @return themes
   */
  @Valid 
  @Schema(name = "themes", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("themes")
  public List<@Valid ThemePresetResponse> getThemes() {
    return themes;
  }

  @JsonProperty("themes")
  public void setThemes(List<@Valid ThemePresetResponse> themes) {
    this.themes = themes;
  }

  public ApplicationResponse dataSourceIds(List<String> dataSourceIds) {
    this.dataSourceIds = dataSourceIds;
    return this;
  }

  public ApplicationResponse addDataSourceIdsItem(String dataSourceIdsItem) {
    if (this.dataSourceIds == null) {
      this.dataSourceIds = new ArrayList<>();
    }
    this.dataSourceIds.add(dataSourceIdsItem);
    return this;
  }

  /**
   * Get dataSourceIds
   * @return dataSourceIds
   */
  
  @Schema(name = "dataSourceIds", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("dataSourceIds")
  public List<String> getDataSourceIds() {
    return dataSourceIds;
  }

  @JsonProperty("dataSourceIds")
  public void setDataSourceIds(List<String> dataSourceIds) {
    this.dataSourceIds = dataSourceIds;
  }

  public ApplicationResponse llmOverride(@Nullable LlmProviderResponse llmOverride) {
    this.llmOverride = llmOverride;
    return this;
  }

  /**
   * Get llmOverride
   * @return llmOverride
   */
  @Valid 
  @Schema(name = "llmOverride", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("llmOverride")
  public @Nullable LlmProviderResponse getLlmOverride() {
    return llmOverride;
  }

  @JsonProperty("llmOverride")
  public void setLlmOverride(@Nullable LlmProviderResponse llmOverride) {
    this.llmOverride = llmOverride;
  }

  public ApplicationResponse createdAt(@Nullable OffsetDateTime createdAt) {
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

  public ApplicationResponse updatedAt(@Nullable OffsetDateTime updatedAt) {
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
    ApplicationResponse applicationResponse = (ApplicationResponse) o;
    return Objects.equals(this.appId, applicationResponse.appId) &&
        Objects.equals(this.slug, applicationResponse.slug) &&
        Objects.equals(this.name, applicationResponse.name) &&
        Objects.equals(this.description, applicationResponse.description) &&
        Objects.equals(this.icon, applicationResponse.icon) &&
        Objects.equals(this.isDefault, applicationResponse.isDefault) &&
        Objects.equals(this.status, applicationResponse.status) &&
        Objects.equals(this.aiPersona, applicationResponse.aiPersona) &&
        Objects.equals(this.guardrails, applicationResponse.guardrails) &&
        Objects.equals(this.branding, applicationResponse.branding) &&
        Objects.equals(this.components, applicationResponse.components) &&
        Objects.equals(this.actions, applicationResponse.actions) &&
        Objects.equals(this.personalization, applicationResponse.personalization) &&
        Objects.equals(this.themes, applicationResponse.themes) &&
        Objects.equals(this.dataSourceIds, applicationResponse.dataSourceIds) &&
        Objects.equals(this.llmOverride, applicationResponse.llmOverride) &&
        Objects.equals(this.createdAt, applicationResponse.createdAt) &&
        Objects.equals(this.updatedAt, applicationResponse.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(appId, slug, name, description, icon, isDefault, status, aiPersona, guardrails, branding, components, actions, personalization, themes, dataSourceIds, llmOverride, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationResponse {\n");
    sb.append("    appId: ").append(toIndentedString(appId)).append("\n");
    sb.append("    slug: ").append(toIndentedString(slug)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
    sb.append("    isDefault: ").append(toIndentedString(isDefault)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    aiPersona: ").append(toIndentedString(aiPersona)).append("\n");
    sb.append("    guardrails: ").append(toIndentedString(guardrails)).append("\n");
    sb.append("    branding: ").append(toIndentedString(branding)).append("\n");
    sb.append("    components: ").append(toIndentedString(components)).append("\n");
    sb.append("    actions: ").append(toIndentedString(actions)).append("\n");
    sb.append("    personalization: ").append(toIndentedString(personalization)).append("\n");
    sb.append("    themes: ").append(toIndentedString(themes)).append("\n");
    sb.append("    dataSourceIds: ").append(toIndentedString(dataSourceIds)).append("\n");
    sb.append("    llmOverride: ").append(toIndentedString(llmOverride)).append("\n");
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

