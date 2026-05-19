package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.CredentialType;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Credential reference for data source authentication
 */

@Schema(name = "CredentialRefResponse", description = "Credential reference for data source authentication")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-18T21:07:32.055501800-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class CredentialRefResponse {

  private @Nullable CredentialType type;

  private @Nullable String secretRef;

  private @Nullable String oauthProvider;

  public CredentialRefResponse type(@Nullable CredentialType type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   * @return type
   */
  @Valid 
  @Schema(name = "type", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("type")
  public @Nullable CredentialType getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(@Nullable CredentialType type) {
    this.type = type;
  }

  public CredentialRefResponse secretRef(@Nullable String secretRef) {
    this.secretRef = secretRef;
    return this;
  }

  /**
   * Secret identifier (GCP secret name, Vault path, or MongoDB doc ID)
   * @return secretRef
   */
  
  @Schema(name = "secretRef", description = "Secret identifier (GCP secret name, Vault path, or MongoDB doc ID)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("secretRef")
  public @Nullable String getSecretRef() {
    return secretRef;
  }

  @JsonProperty("secretRef")
  public void setSecretRef(@Nullable String secretRef) {
    this.secretRef = secretRef;
  }

  public CredentialRefResponse oauthProvider(@Nullable String oauthProvider) {
    this.oauthProvider = oauthProvider;
    return this;
  }

  /**
   * OAuth2 provider name (only for OAUTH2 type)
   * @return oauthProvider
   */
  
  @Schema(name = "oauthProvider", description = "OAuth2 provider name (only for OAUTH2 type)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("oauthProvider")
  public @Nullable String getOauthProvider() {
    return oauthProvider;
  }

  @JsonProperty("oauthProvider")
  public void setOauthProvider(@Nullable String oauthProvider) {
    this.oauthProvider = oauthProvider;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CredentialRefResponse credentialRefResponse = (CredentialRefResponse) o;
    return Objects.equals(this.type, credentialRefResponse.type) &&
        Objects.equals(this.secretRef, credentialRefResponse.secretRef) &&
        Objects.equals(this.oauthProvider, credentialRefResponse.oauthProvider);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, secretRef, oauthProvider);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CredentialRefResponse {\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    secretRef: ").append(toIndentedString(secretRef)).append("\n");
    sb.append("    oauthProvider: ").append(toIndentedString(oauthProvider)).append("\n");
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

