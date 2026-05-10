package com.spectrayan.synaptiq.infrastructure.in.web.dto;

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
 * CreateRoleRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-10T17:15:52.297398600-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class CreateRoleRequest {

  private String slug;

  private String displayName;

  private @Nullable String description;

  @Valid
  private List<String> scopeSlugs = new ArrayList<>();

  public CreateRoleRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CreateRoleRequest(String slug, String displayName, List<String> scopeSlugs) {
    this.slug = slug;
    this.displayName = displayName;
    this.scopeSlugs = scopeSlugs;
  }

  public CreateRoleRequest slug(String slug) {
    this.slug = slug;
    return this;
  }

  /**
   * Unique role identifier, e.g. \"custom:marketing-editor\"
   * @return slug
   */
  @NotNull @Size(min = 3, max = 100) 
  @Schema(name = "slug", description = "Unique role identifier, e.g. \"custom:marketing-editor\"", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("slug")
  public String getSlug() {
    return slug;
  }

  @JsonProperty("slug")
  public void setSlug(String slug) {
    this.slug = slug;
  }

  public CreateRoleRequest displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  /**
   * Human-readable name for admin UI
   * @return displayName
   */
  @NotNull @Size(min = 1, max = 100) 
  @Schema(name = "displayName", description = "Human-readable name for admin UI", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }

  @JsonProperty("displayName")
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public CreateRoleRequest description(@Nullable String description) {
    this.description = description;
    return this;
  }

  /**
   * Role description for admin UI
   * @return description
   */
  @Size(max = 500) 
  @Schema(name = "description", description = "Role description for admin UI", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("description")
  public @Nullable String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(@Nullable String description) {
    this.description = description;
  }

  public CreateRoleRequest scopeSlugs(List<String> scopeSlugs) {
    this.scopeSlugs = scopeSlugs;
    return this;
  }

  public CreateRoleRequest addScopeSlugsItem(String scopeSlugsItem) {
    if (this.scopeSlugs == null) {
      this.scopeSlugs = new ArrayList<>();
    }
    this.scopeSlugs.add(scopeSlugsItem);
    return this;
  }

  /**
   * List of scope slugs this role grants
   * @return scopeSlugs
   */
  @NotNull @Size(min = 1) 
  @Schema(name = "scopeSlugs", description = "List of scope slugs this role grants", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("scopeSlugs")
  public List<String> getScopeSlugs() {
    return scopeSlugs;
  }

  @JsonProperty("scopeSlugs")
  public void setScopeSlugs(List<String> scopeSlugs) {
    this.scopeSlugs = scopeSlugs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateRoleRequest createRoleRequest = (CreateRoleRequest) o;
    return Objects.equals(this.slug, createRoleRequest.slug) &&
        Objects.equals(this.displayName, createRoleRequest.displayName) &&
        Objects.equals(this.description, createRoleRequest.description) &&
        Objects.equals(this.scopeSlugs, createRoleRequest.scopeSlugs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(slug, displayName, description, scopeSlugs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateRoleRequest {\n");
    sb.append("    slug: ").append(toIndentedString(slug)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    scopeSlugs: ").append(toIndentedString(scopeSlugs)).append("\n");
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

