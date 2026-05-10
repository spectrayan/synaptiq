package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.AdminRole;
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
 * TenantAdminResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-10T17:15:52.297398600-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class TenantAdminResponse {

  private @Nullable String uid;

  private @Nullable String email;

  private @Nullable AdminRole role;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime invitedAt;

  private @Nullable Boolean accepted;

  public TenantAdminResponse uid(@Nullable String uid) {
    this.uid = uid;
    return this;
  }

  /**
   * Get uid
   * @return uid
   */
  
  @Schema(name = "uid", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("uid")
  public @Nullable String getUid() {
    return uid;
  }

  @JsonProperty("uid")
  public void setUid(@Nullable String uid) {
    this.uid = uid;
  }

  public TenantAdminResponse email(@Nullable String email) {
    this.email = email;
    return this;
  }

  /**
   * Get email
   * @return email
   */
  @jakarta.validation.constraints.Email 
  @Schema(name = "email", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("email")
  public @Nullable String getEmail() {
    return email;
  }

  @JsonProperty("email")
  public void setEmail(@Nullable String email) {
    this.email = email;
  }

  public TenantAdminResponse role(@Nullable AdminRole role) {
    this.role = role;
    return this;
  }

  /**
   * Get role
   * @return role
   */
  @Valid 
  @Schema(name = "role", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("role")
  public @Nullable AdminRole getRole() {
    return role;
  }

  @JsonProperty("role")
  public void setRole(@Nullable AdminRole role) {
    this.role = role;
  }

  public TenantAdminResponse invitedAt(@Nullable OffsetDateTime invitedAt) {
    this.invitedAt = invitedAt;
    return this;
  }

  /**
   * Get invitedAt
   * @return invitedAt
   */
  @Valid 
  @Schema(name = "invitedAt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("invitedAt")
  public @Nullable OffsetDateTime getInvitedAt() {
    return invitedAt;
  }

  @JsonProperty("invitedAt")
  public void setInvitedAt(@Nullable OffsetDateTime invitedAt) {
    this.invitedAt = invitedAt;
  }

  public TenantAdminResponse accepted(@Nullable Boolean accepted) {
    this.accepted = accepted;
    return this;
  }

  /**
   * Get accepted
   * @return accepted
   */
  
  @Schema(name = "accepted", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("accepted")
  public @Nullable Boolean getAccepted() {
    return accepted;
  }

  @JsonProperty("accepted")
  public void setAccepted(@Nullable Boolean accepted) {
    this.accepted = accepted;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TenantAdminResponse tenantAdminResponse = (TenantAdminResponse) o;
    return Objects.equals(this.uid, tenantAdminResponse.uid) &&
        Objects.equals(this.email, tenantAdminResponse.email) &&
        Objects.equals(this.role, tenantAdminResponse.role) &&
        Objects.equals(this.invitedAt, tenantAdminResponse.invitedAt) &&
        Objects.equals(this.accepted, tenantAdminResponse.accepted);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uid, email, role, invitedAt, accepted);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TenantAdminResponse {\n");
    sb.append("    uid: ").append(toIndentedString(uid)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    role: ").append(toIndentedString(role)).append("\n");
    sb.append("    invitedAt: ").append(toIndentedString(invitedAt)).append("\n");
    sb.append("    accepted: ").append(toIndentedString(accepted)).append("\n");
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

