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
 * UpdateCustomRoleRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-18T21:07:32.055501800-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class UpdateCustomRoleRequest {

  private @Nullable String displayName;

  private @Nullable String description;

  @Valid
  private List<String> scopeSlugs = new ArrayList<>();

  public UpdateCustomRoleRequest displayName(@Nullable String displayName) {
    this.displayName = displayName;
    return this;
  }

  /**
   * Updated display name
   * @return displayName
   */
  @Size(min = 1, max = 100) 
  @Schema(name = "displayName", description = "Updated display name", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("displayName")
  public @Nullable String getDisplayName() {
    return displayName;
  }

  @JsonProperty("displayName")
  public void setDisplayName(@Nullable String displayName) {
    this.displayName = displayName;
  }

  public UpdateCustomRoleRequest description(@Nullable String description) {
    this.description = description;
    return this;
  }

  /**
   * Updated description
   * @return description
   */
  @Size(max = 500) 
  @Schema(name = "description", description = "Updated description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("description")
  public @Nullable String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(@Nullable String description) {
    this.description = description;
  }

  public UpdateCustomRoleRequest scopeSlugs(List<String> scopeSlugs) {
    this.scopeSlugs = scopeSlugs;
    return this;
  }

  public UpdateCustomRoleRequest addScopeSlugsItem(String scopeSlugsItem) {
    if (this.scopeSlugs == null) {
      this.scopeSlugs = new ArrayList<>();
    }
    this.scopeSlugs.add(scopeSlugsItem);
    return this;
  }

  /**
   * Updated scope slugs this role grants
   * @return scopeSlugs
   */
  @Size(min = 1) 
  @Schema(name = "scopeSlugs", description = "Updated scope slugs this role grants", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
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
    UpdateCustomRoleRequest updateCustomRoleRequest = (UpdateCustomRoleRequest) o;
    return Objects.equals(this.displayName, updateCustomRoleRequest.displayName) &&
        Objects.equals(this.description, updateCustomRoleRequest.description) &&
        Objects.equals(this.scopeSlugs, updateCustomRoleRequest.scopeSlugs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(displayName, description, scopeSlugs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UpdateCustomRoleRequest {\n");
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

