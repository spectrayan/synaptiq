package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.synaptiq.infrastructure.in.web.dto.ActionsConfigResponse;
import com.synaptiq.infrastructure.in.web.dto.AiPersonaResponse;
import com.synaptiq.infrastructure.in.web.dto.BrandingResponse;
import com.synaptiq.infrastructure.in.web.dto.ComponentsToggleResponse;
import com.synaptiq.infrastructure.in.web.dto.GuardrailsResponse;
import com.synaptiq.infrastructure.in.web.dto.LlmProviderResponse;
import com.synaptiq.infrastructure.in.web.dto.PersonalizationResponse;
import com.synaptiq.infrastructure.in.web.dto.ThemePresetResponse;
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
 * Create a new application
 */

@Schema(name = "CreateApplicationRequest", description = "Create a new application")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-05T21:15:25.464614100-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class CreateApplicationRequest {

  private String name;

  private String slug;

  private @Nullable String description;

  private @Nullable String icon;

  private Boolean isDefault = false;

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

  public CreateApplicationRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CreateApplicationRequest(String name, String slug) {
    this.name = name;
    this.slug = slug;
  }

  public CreateApplicationRequest name(String name) {
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

  public CreateApplicationRequest slug(String slug) {
    this.slug = slug;
    return this;
  }

  /**
   * Get slug
   * @return slug
   */
  @NotNull 
  @Schema(name = "slug", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("slug")
  public String getSlug() {
    return slug;
  }

  @JsonProperty("slug")
  public void setSlug(String slug) {
    this.slug = slug;
  }

  public CreateApplicationRequest description(@Nullable String description) {
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

  public CreateApplicationRequest icon(@Nullable String icon) {
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

  public CreateApplicationRequest isDefault(Boolean isDefault) {
    this.isDefault = isDefault;
    return this;
  }

  /**
   * Get isDefault
   * @return isDefault
   */
  
  @Schema(name = "isDefault", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("isDefault")
  public Boolean getIsDefault() {
    return isDefault;
  }

  @JsonProperty("isDefault")
  public void setIsDefault(Boolean isDefault) {
    this.isDefault = isDefault;
  }

  public CreateApplicationRequest aiPersona(@Nullable AiPersonaResponse aiPersona) {
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

  public CreateApplicationRequest guardrails(@Nullable GuardrailsResponse guardrails) {
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

  public CreateApplicationRequest branding(@Nullable BrandingResponse branding) {
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

  public CreateApplicationRequest components(@Nullable ComponentsToggleResponse components) {
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

  public CreateApplicationRequest actions(@Nullable ActionsConfigResponse actions) {
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

  public CreateApplicationRequest personalization(@Nullable PersonalizationResponse personalization) {
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

  public CreateApplicationRequest themes(List<@Valid ThemePresetResponse> themes) {
    this.themes = themes;
    return this;
  }

  public CreateApplicationRequest addThemesItem(ThemePresetResponse themesItem) {
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

  public CreateApplicationRequest dataSourceIds(List<String> dataSourceIds) {
    this.dataSourceIds = dataSourceIds;
    return this;
  }

  public CreateApplicationRequest addDataSourceIdsItem(String dataSourceIdsItem) {
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

  public CreateApplicationRequest llmOverride(@Nullable LlmProviderResponse llmOverride) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateApplicationRequest createApplicationRequest = (CreateApplicationRequest) o;
    return Objects.equals(this.name, createApplicationRequest.name) &&
        Objects.equals(this.slug, createApplicationRequest.slug) &&
        Objects.equals(this.description, createApplicationRequest.description) &&
        Objects.equals(this.icon, createApplicationRequest.icon) &&
        Objects.equals(this.isDefault, createApplicationRequest.isDefault) &&
        Objects.equals(this.aiPersona, createApplicationRequest.aiPersona) &&
        Objects.equals(this.guardrails, createApplicationRequest.guardrails) &&
        Objects.equals(this.branding, createApplicationRequest.branding) &&
        Objects.equals(this.components, createApplicationRequest.components) &&
        Objects.equals(this.actions, createApplicationRequest.actions) &&
        Objects.equals(this.personalization, createApplicationRequest.personalization) &&
        Objects.equals(this.themes, createApplicationRequest.themes) &&
        Objects.equals(this.dataSourceIds, createApplicationRequest.dataSourceIds) &&
        Objects.equals(this.llmOverride, createApplicationRequest.llmOverride);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, slug, description, icon, isDefault, aiPersona, guardrails, branding, components, actions, personalization, themes, dataSourceIds, llmOverride);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateApplicationRequest {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    slug: ").append(toIndentedString(slug)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
    sb.append("    isDefault: ").append(toIndentedString(isDefault)).append("\n");
    sb.append("    aiPersona: ").append(toIndentedString(aiPersona)).append("\n");
    sb.append("    guardrails: ").append(toIndentedString(guardrails)).append("\n");
    sb.append("    branding: ").append(toIndentedString(branding)).append("\n");
    sb.append("    components: ").append(toIndentedString(components)).append("\n");
    sb.append("    actions: ").append(toIndentedString(actions)).append("\n");
    sb.append("    personalization: ").append(toIndentedString(personalization)).append("\n");
    sb.append("    themes: ").append(toIndentedString(themes)).append("\n");
    sb.append("    dataSourceIds: ").append(toIndentedString(dataSourceIds)).append("\n");
    sb.append("    llmOverride: ").append(toIndentedString(llmOverride)).append("\n");
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

