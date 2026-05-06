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
 * LlmProviderResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-05T21:15:25.464614100-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class LlmProviderResponse {

  private @Nullable String provider;

  private @Nullable String modelId;

  private @Nullable Boolean isByok;

  public LlmProviderResponse provider(@Nullable String provider) {
    this.provider = provider;
    return this;
  }

  /**
   * Get provider
   * @return provider
   */
  
  @Schema(name = "provider", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("provider")
  public @Nullable String getProvider() {
    return provider;
  }

  @JsonProperty("provider")
  public void setProvider(@Nullable String provider) {
    this.provider = provider;
  }

  public LlmProviderResponse modelId(@Nullable String modelId) {
    this.modelId = modelId;
    return this;
  }

  /**
   * Get modelId
   * @return modelId
   */
  
  @Schema(name = "modelId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("modelId")
  public @Nullable String getModelId() {
    return modelId;
  }

  @JsonProperty("modelId")
  public void setModelId(@Nullable String modelId) {
    this.modelId = modelId;
  }

  public LlmProviderResponse isByok(@Nullable Boolean isByok) {
    this.isByok = isByok;
    return this;
  }

  /**
   * Get isByok
   * @return isByok
   */
  
  @Schema(name = "isByok", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("isByok")
  public @Nullable Boolean getIsByok() {
    return isByok;
  }

  @JsonProperty("isByok")
  public void setIsByok(@Nullable Boolean isByok) {
    this.isByok = isByok;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LlmProviderResponse llmProviderResponse = (LlmProviderResponse) o;
    return Objects.equals(this.provider, llmProviderResponse.provider) &&
        Objects.equals(this.modelId, llmProviderResponse.modelId) &&
        Objects.equals(this.isByok, llmProviderResponse.isByok);
  }

  @Override
  public int hashCode() {
    return Objects.hash(provider, modelId, isByok);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LlmProviderResponse {\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    modelId: ").append(toIndentedString(modelId)).append("\n");
    sb.append("    isByok: ").append(toIndentedString(isByok)).append("\n");
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

