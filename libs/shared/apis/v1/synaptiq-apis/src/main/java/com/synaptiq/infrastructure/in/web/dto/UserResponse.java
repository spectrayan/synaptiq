package com.synaptiq.infrastructure.in.web.dto;

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
 * UserResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-08T22:14:16.718368-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class UserResponse {

  private @Nullable String uid;

  private @Nullable String email;

  private @Nullable String displayName;

  private @Nullable String role;

  private @Nullable Boolean emailVerified;

  private @Nullable String tenantId;

  public UserResponse uid(@Nullable String uid) {
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

  public UserResponse email(@Nullable String email) {
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

  public UserResponse displayName(@Nullable String displayName) {
    this.displayName = displayName;
    return this;
  }

  /**
   * Get displayName
   * @return displayName
   */
  
  @Schema(name = "displayName", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("displayName")
  public @Nullable String getDisplayName() {
    return displayName;
  }

  @JsonProperty("displayName")
  public void setDisplayName(@Nullable String displayName) {
    this.displayName = displayName;
  }

  public UserResponse role(@Nullable String role) {
    this.role = role;
    return this;
  }

  /**
   * Get role
   * @return role
   */
  
  @Schema(name = "role", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("role")
  public @Nullable String getRole() {
    return role;
  }

  @JsonProperty("role")
  public void setRole(@Nullable String role) {
    this.role = role;
  }

  public UserResponse emailVerified(@Nullable Boolean emailVerified) {
    this.emailVerified = emailVerified;
    return this;
  }

  /**
   * Get emailVerified
   * @return emailVerified
   */
  
  @Schema(name = "emailVerified", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("emailVerified")
  public @Nullable Boolean getEmailVerified() {
    return emailVerified;
  }

  @JsonProperty("emailVerified")
  public void setEmailVerified(@Nullable Boolean emailVerified) {
    this.emailVerified = emailVerified;
  }

  public UserResponse tenantId(@Nullable String tenantId) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserResponse userResponse = (UserResponse) o;
    return Objects.equals(this.uid, userResponse.uid) &&
        Objects.equals(this.email, userResponse.email) &&
        Objects.equals(this.displayName, userResponse.displayName) &&
        Objects.equals(this.role, userResponse.role) &&
        Objects.equals(this.emailVerified, userResponse.emailVerified) &&
        Objects.equals(this.tenantId, userResponse.tenantId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uid, email, displayName, role, emailVerified, tenantId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UserResponse {\n");
    sb.append("    uid: ").append(toIndentedString(uid)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    role: ").append(toIndentedString(role)).append("\n");
    sb.append("    emailVerified: ").append(toIndentedString(emailVerified)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
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

