package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.RoleResponse;
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
 * ListRoles200Response
 */

@JsonTypeName("listRoles_200_response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-10T17:15:52.297398600-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ListRoles200Response {

  @Valid
  private List<@Valid RoleResponse> roles = new ArrayList<>();

  public ListRoles200Response roles(List<@Valid RoleResponse> roles) {
    this.roles = roles;
    return this;
  }

  public ListRoles200Response addRolesItem(RoleResponse rolesItem) {
    if (this.roles == null) {
      this.roles = new ArrayList<>();
    }
    this.roles.add(rolesItem);
    return this;
  }

  /**
   * Get roles
   * @return roles
   */
  @Valid 
  @Schema(name = "roles", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("roles")
  public List<@Valid RoleResponse> getRoles() {
    return roles;
  }

  @JsonProperty("roles")
  public void setRoles(List<@Valid RoleResponse> roles) {
    this.roles = roles;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ListRoles200Response listRoles200Response = (ListRoles200Response) o;
    return Objects.equals(this.roles, listRoles200Response.roles);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roles);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ListRoles200Response {\n");
    sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
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

