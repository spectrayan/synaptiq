package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
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
 * RoleResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-18T21:07:32.055501800-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class RoleResponse {

  private String id;

  private String slug;

  private @Nullable String tenantId;

  private String displayName;

  private @Nullable String description;

  private Boolean systemRole;

  /**
   * Role type, either global or tenant
   */
  public enum RoleTypeEnum {
    GLOBAL("global"),
    
    TENANT("tenant");

    private final String value;

    RoleTypeEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static RoleTypeEnum fromValue(String value) {
      for (RoleTypeEnum b : RoleTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private RoleTypeEnum roleType;

  @Valid
  private List<String> scopeSlugs = new ArrayList<>();

  public RoleResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public RoleResponse(String id, String slug, String displayName, Boolean systemRole, RoleTypeEnum roleType, List<String> scopeSlugs) {
    this.id = id;
    this.slug = slug;
    this.displayName = displayName;
    this.systemRole = systemRole;
    this.roleType = roleType;
    this.scopeSlugs = scopeSlugs;
  }

  public RoleResponse id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Unique role ID
   * @return id
   */
  @NotNull 
  @Schema(name = "id", description = "Unique role ID", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  public RoleResponse slug(String slug) {
    this.slug = slug;
    return this;
  }

  /**
   * Stable role identifier
   * @return slug
   */
  @NotNull 
  @Schema(name = "slug", description = "Stable role identifier", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("slug")
  public String getSlug() {
    return slug;
  }

  @JsonProperty("slug")
  public void setSlug(String slug) {
    this.slug = slug;
  }

  public RoleResponse tenantId(@Nullable String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  /**
   * Owning tenant ID, null for global roles
   * @return tenantId
   */
  
  @Schema(name = "tenantId", description = "Owning tenant ID, null for global roles", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("tenantId")
  public @Nullable String getTenantId() {
    return tenantId;
  }

  @JsonProperty("tenantId")
  public void setTenantId(@Nullable String tenantId) {
    this.tenantId = tenantId;
  }

  public RoleResponse displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  /**
   * Human-readable role name
   * @return displayName
   */
  @NotNull 
  @Schema(name = "displayName", description = "Human-readable role name", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }

  @JsonProperty("displayName")
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public RoleResponse description(@Nullable String description) {
    this.description = description;
    return this;
  }

  /**
   * Role description
   * @return description
   */
  
  @Schema(name = "description", description = "Role description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("description")
  public @Nullable String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(@Nullable String description) {
    this.description = description;
  }

  public RoleResponse systemRole(Boolean systemRole) {
    this.systemRole = systemRole;
    return this;
  }

  /**
   * Whether this role is system-managed (immutable)
   * @return systemRole
   */
  @NotNull 
  @Schema(name = "systemRole", description = "Whether this role is system-managed (immutable)", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("systemRole")
  public Boolean getSystemRole() {
    return systemRole;
  }

  @JsonProperty("systemRole")
  public void setSystemRole(Boolean systemRole) {
    this.systemRole = systemRole;
  }

  public RoleResponse roleType(RoleTypeEnum roleType) {
    this.roleType = roleType;
    return this;
  }

  /**
   * Role type, either global or tenant
   * @return roleType
   */
  @NotNull 
  @Schema(name = "roleType", description = "Role type, either global or tenant", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("roleType")
  public RoleTypeEnum getRoleType() {
    return roleType;
  }

  @JsonProperty("roleType")
  public void setRoleType(RoleTypeEnum roleType) {
    this.roleType = roleType;
  }

  public RoleResponse scopeSlugs(List<String> scopeSlugs) {
    this.scopeSlugs = scopeSlugs;
    return this;
  }

  public RoleResponse addScopeSlugsItem(String scopeSlugsItem) {
    if (this.scopeSlugs == null) {
      this.scopeSlugs = new ArrayList<>();
    }
    this.scopeSlugs.add(scopeSlugsItem);
    return this;
  }

  /**
   * Scope slugs granted by this role
   * @return scopeSlugs
   */
  @NotNull 
  @Schema(name = "scopeSlugs", description = "Scope slugs granted by this role", requiredMode = Schema.RequiredMode.REQUIRED)
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
    RoleResponse roleResponse = (RoleResponse) o;
    return Objects.equals(this.id, roleResponse.id) &&
        Objects.equals(this.slug, roleResponse.slug) &&
        Objects.equals(this.tenantId, roleResponse.tenantId) &&
        Objects.equals(this.displayName, roleResponse.displayName) &&
        Objects.equals(this.description, roleResponse.description) &&
        Objects.equals(this.systemRole, roleResponse.systemRole) &&
        Objects.equals(this.roleType, roleResponse.roleType) &&
        Objects.equals(this.scopeSlugs, roleResponse.scopeSlugs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, slug, tenantId, displayName, description, systemRole, roleType, scopeSlugs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RoleResponse {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    slug: ").append(toIndentedString(slug)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    systemRole: ").append(toIndentedString(systemRole)).append("\n");
    sb.append("    roleType: ").append(toIndentedString(roleType)).append("\n");
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

