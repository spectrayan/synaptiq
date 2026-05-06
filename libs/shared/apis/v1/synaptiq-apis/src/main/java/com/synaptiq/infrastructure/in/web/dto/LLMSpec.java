package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * LLM configuration for an agent node
 */

@Schema(name = "LLMSpec", description = "LLM configuration for an agent node")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-05T21:15:25.464614100-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class LLMSpec {

  /**
   * Gets or Sets provider
   */
  public enum ProviderEnum {
    VERTEXAI("VERTEXAI"),
    
    OPENAI("OPENAI"),
    
    ANTHROPIC("ANTHROPIC");

    private final String value;

    ProviderEnum(String value) {
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
    public static ProviderEnum fromValue(String value) {
      for (ProviderEnum b : ProviderEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private @Nullable ProviderEnum provider;

  private @Nullable String model;

  private Double temperature = 0d;

  private @Nullable Integer maxTokens;

  private Boolean streaming = true;

  @Valid
  private Map<String, Object> params = new HashMap<>();

  public LLMSpec provider(@Nullable ProviderEnum provider) {
    this.provider = provider;
    return this;
  }

  /**
   * Get provider
   * @return provider
   */
  
  @Schema(name = "provider", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("provider")
  public @Nullable ProviderEnum getProvider() {
    return provider;
  }

  @JsonProperty("provider")
  public void setProvider(@Nullable ProviderEnum provider) {
    this.provider = provider;
  }

  public LLMSpec model(@Nullable String model) {
    this.model = model;
    return this;
  }

  /**
   * Get model
   * @return model
   */
  
  @Schema(name = "model", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("model")
  public @Nullable String getModel() {
    return model;
  }

  @JsonProperty("model")
  public void setModel(@Nullable String model) {
    this.model = model;
  }

  public LLMSpec temperature(Double temperature) {
    this.temperature = temperature;
    return this;
  }

  /**
   * Get temperature
   * @return temperature
   */
  
  @Schema(name = "temperature", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("temperature")
  public Double getTemperature() {
    return temperature;
  }

  @JsonProperty("temperature")
  public void setTemperature(Double temperature) {
    this.temperature = temperature;
  }

  public LLMSpec maxTokens(@Nullable Integer maxTokens) {
    this.maxTokens = maxTokens;
    return this;
  }

  /**
   * Get maxTokens
   * @return maxTokens
   */
  
  @Schema(name = "maxTokens", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("maxTokens")
  public @Nullable Integer getMaxTokens() {
    return maxTokens;
  }

  @JsonProperty("maxTokens")
  public void setMaxTokens(@Nullable Integer maxTokens) {
    this.maxTokens = maxTokens;
  }

  public LLMSpec streaming(Boolean streaming) {
    this.streaming = streaming;
    return this;
  }

  /**
   * Get streaming
   * @return streaming
   */
  
  @Schema(name = "streaming", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("streaming")
  public Boolean getStreaming() {
    return streaming;
  }

  @JsonProperty("streaming")
  public void setStreaming(Boolean streaming) {
    this.streaming = streaming;
  }

  public LLMSpec params(Map<String, Object> params) {
    this.params = params;
    return this;
  }

  public LLMSpec putParamsItem(String key, Object paramsItem) {
    if (this.params == null) {
      this.params = new HashMap<>();
    }
    this.params.put(key, paramsItem);
    return this;
  }

  /**
   * Get params
   * @return params
   */
  
  @Schema(name = "params", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("params")
  public Map<String, Object> getParams() {
    return params;
  }

  @JsonProperty("params")
  public void setParams(Map<String, Object> params) {
    this.params = params;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LLMSpec llMSpec = (LLMSpec) o;
    return Objects.equals(this.provider, llMSpec.provider) &&
        Objects.equals(this.model, llMSpec.model) &&
        Objects.equals(this.temperature, llMSpec.temperature) &&
        Objects.equals(this.maxTokens, llMSpec.maxTokens) &&
        Objects.equals(this.streaming, llMSpec.streaming) &&
        Objects.equals(this.params, llMSpec.params);
  }

  @Override
  public int hashCode() {
    return Objects.hash(provider, model, temperature, maxTokens, streaming, params);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LLMSpec {\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    model: ").append(toIndentedString(model)).append("\n");
    sb.append("    temperature: ").append(toIndentedString(temperature)).append("\n");
    sb.append("    maxTokens: ").append(toIndentedString(maxTokens)).append("\n");
    sb.append("    streaming: ").append(toIndentedString(streaming)).append("\n");
    sb.append("    params: ").append(toIndentedString(params)).append("\n");
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

