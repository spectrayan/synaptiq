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
 * ScopeResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-10T17:15:52.297398600-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ScopeResponse {

  private String slug;

  private String displayName;

  private @Nullable String description;

  private String resource;

  private String action;

  public ScopeResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ScopeResponse(String slug, String displayName, String resource, String action) {
    this.slug = slug;
    this.displayName = displayName;
    this.resource = resource;
    this.action = action;
  }

  public ScopeResponse slug(String slug) {
    this.slug = slug;
    return this;
  }

  /**
   * Scope identifier, e.g. workflow:create
   * @return slug
   */
  @NotNull 
  @Schema(name = "slug", description = "Scope identifier, e.g. workflow:create", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("slug")
  public String getSlug() {
    return slug;
  }

  @JsonProperty("slug")
  public void setSlug(String slug) {
    this.slug = slug;
  }

  public ScopeResponse displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  /**
   * Human-readable scope name
   * @return displayName
   */
  @NotNull 
  @Schema(name = "displayName", description = "Human-readable scope name", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }

  @JsonProperty("displayName")
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public ScopeResponse description(@Nullable String description) {
    this.description = description;
    return this;
  }

  /**
   * Scope description
   * @return description
   */
  
  @Schema(name = "description", description = "Scope description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("description")
  public @Nullable String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(@Nullable String description) {
    this.description = description;
  }

  public ScopeResponse resource(String resource) {
    this.resource = resource;
    return this;
  }

  /**
   * Resource category, e.g. workflow
   * @return resource
   */
  @NotNull 
  @Schema(name = "resource", description = "Resource category, e.g. workflow", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("resource")
  public String getResource() {
    return resource;
  }

  @JsonProperty("resource")
  public void setResource(String resource) {
    this.resource = resource;
  }

  public ScopeResponse action(String action) {
    this.action = action;
    return this;
  }

  /**
   * Action within the resource, e.g. create
   * @return action
   */
  @NotNull 
  @Schema(name = "action", description = "Action within the resource, e.g. create", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("action")
  public String getAction() {
    return action;
  }

  @JsonProperty("action")
  public void setAction(String action) {
    this.action = action;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ScopeResponse scopeResponse = (ScopeResponse) o;
    return Objects.equals(this.slug, scopeResponse.slug) &&
        Objects.equals(this.displayName, scopeResponse.displayName) &&
        Objects.equals(this.description, scopeResponse.description) &&
        Objects.equals(this.resource, scopeResponse.resource) &&
        Objects.equals(this.action, scopeResponse.action);
  }

  @Override
  public int hashCode() {
    return Objects.hash(slug, displayName, description, resource, action);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScopeResponse {\n");
    sb.append("    slug: ").append(toIndentedString(slug)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    resource: ").append(toIndentedString(resource)).append("\n");
    sb.append("    action: ").append(toIndentedString(action)).append("\n");
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

