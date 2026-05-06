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
 * AuthTokenResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-05T21:15:25.464614100-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class AuthTokenResponse {

  private @Nullable String idToken;

  private @Nullable String refreshToken;

  private @Nullable Integer expiresIn;

  public AuthTokenResponse idToken(@Nullable String idToken) {
    this.idToken = idToken;
    return this;
  }

  /**
   * JWT identity token
   * @return idToken
   */
  
  @Schema(name = "idToken", description = "JWT identity token", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("idToken")
  public @Nullable String getIdToken() {
    return idToken;
  }

  @JsonProperty("idToken")
  public void setIdToken(@Nullable String idToken) {
    this.idToken = idToken;
  }

  public AuthTokenResponse refreshToken(@Nullable String refreshToken) {
    this.refreshToken = refreshToken;
    return this;
  }

  /**
   * Opaque refresh token
   * @return refreshToken
   */
  
  @Schema(name = "refreshToken", description = "Opaque refresh token", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("refreshToken")
  public @Nullable String getRefreshToken() {
    return refreshToken;
  }

  @JsonProperty("refreshToken")
  public void setRefreshToken(@Nullable String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public AuthTokenResponse expiresIn(@Nullable Integer expiresIn) {
    this.expiresIn = expiresIn;
    return this;
  }

  /**
   * Token lifetime in seconds
   * @return expiresIn
   */
  
  @Schema(name = "expiresIn", description = "Token lifetime in seconds", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("expiresIn")
  public @Nullable Integer getExpiresIn() {
    return expiresIn;
  }

  @JsonProperty("expiresIn")
  public void setExpiresIn(@Nullable Integer expiresIn) {
    this.expiresIn = expiresIn;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AuthTokenResponse authTokenResponse = (AuthTokenResponse) o;
    return Objects.equals(this.idToken, authTokenResponse.idToken) &&
        Objects.equals(this.refreshToken, authTokenResponse.refreshToken) &&
        Objects.equals(this.expiresIn, authTokenResponse.expiresIn);
  }

  @Override
  public int hashCode() {
    return Objects.hash(idToken, refreshToken, expiresIn);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AuthTokenResponse {\n");
    sb.append("    idToken: ").append(toIndentedString(idToken)).append("\n");
    sb.append("    refreshToken: ").append(toIndentedString(refreshToken)).append("\n");
    sb.append("    expiresIn: ").append(toIndentedString(expiresIn)).append("\n");
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

