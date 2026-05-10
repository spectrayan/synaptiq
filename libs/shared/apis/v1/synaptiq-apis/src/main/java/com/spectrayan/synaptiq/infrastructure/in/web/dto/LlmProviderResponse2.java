package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * LLM provider configuration
 */

@Schema(name = "LlmProviderResponse-2", description = "LLM provider configuration")
@JsonTypeName("LlmProviderResponse-2")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-05T13:34:15.888298700-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class LlmProviderResponse2 {

  private @Nullable String provider;

  private @Nullable String modelId;

  private @Nullable Boolean isByok;

  public LlmProviderResponse2 provider(@Nullable String provider) {
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

  public LlmProviderResponse2 modelId(@Nullable String modelId) {
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

  public LlmProviderResponse2 isByok(@Nullable Boolean isByok) {
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
    LlmProviderResponse2 llmProviderResponse2 = (LlmProviderResponse2) o;
    return Objects.equals(this.provider, llmProviderResponse2.provider) &&
        Objects.equals(this.modelId, llmProviderResponse2.modelId) &&
        Objects.equals(this.isByok, llmProviderResponse2.isByok);
  }

  @Override
  public int hashCode() {
    return Objects.hash(provider, modelId, isByok);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LlmProviderResponse2 {\n");
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

