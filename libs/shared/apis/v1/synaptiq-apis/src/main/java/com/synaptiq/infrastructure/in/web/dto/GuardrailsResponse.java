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
 * GuardrailsResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-05T21:15:25.464614100-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class GuardrailsResponse {

  private @Nullable String outOfScopeMessage;

  private @Nullable Boolean recommendationMode;

  private @Nullable String language;

  public GuardrailsResponse outOfScopeMessage(@Nullable String outOfScopeMessage) {
    this.outOfScopeMessage = outOfScopeMessage;
    return this;
  }

  /**
   * Get outOfScopeMessage
   * @return outOfScopeMessage
   */
  
  @Schema(name = "outOfScopeMessage", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("outOfScopeMessage")
  public @Nullable String getOutOfScopeMessage() {
    return outOfScopeMessage;
  }

  @JsonProperty("outOfScopeMessage")
  public void setOutOfScopeMessage(@Nullable String outOfScopeMessage) {
    this.outOfScopeMessage = outOfScopeMessage;
  }

  public GuardrailsResponse recommendationMode(@Nullable Boolean recommendationMode) {
    this.recommendationMode = recommendationMode;
    return this;
  }

  /**
   * Get recommendationMode
   * @return recommendationMode
   */
  
  @Schema(name = "recommendationMode", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("recommendationMode")
  public @Nullable Boolean getRecommendationMode() {
    return recommendationMode;
  }

  @JsonProperty("recommendationMode")
  public void setRecommendationMode(@Nullable Boolean recommendationMode) {
    this.recommendationMode = recommendationMode;
  }

  public GuardrailsResponse language(@Nullable String language) {
    this.language = language;
    return this;
  }

  /**
   * Get language
   * @return language
   */
  
  @Schema(name = "language", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("language")
  public @Nullable String getLanguage() {
    return language;
  }

  @JsonProperty("language")
  public void setLanguage(@Nullable String language) {
    this.language = language;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GuardrailsResponse guardrailsResponse = (GuardrailsResponse) o;
    return Objects.equals(this.outOfScopeMessage, guardrailsResponse.outOfScopeMessage) &&
        Objects.equals(this.recommendationMode, guardrailsResponse.recommendationMode) &&
        Objects.equals(this.language, guardrailsResponse.language);
  }

  @Override
  public int hashCode() {
    return Objects.hash(outOfScopeMessage, recommendationMode, language);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GuardrailsResponse {\n");
    sb.append("    outOfScopeMessage: ").append(toIndentedString(outOfScopeMessage)).append("\n");
    sb.append("    recommendationMode: ").append(toIndentedString(recommendationMode)).append("\n");
    sb.append("    language: ").append(toIndentedString(language)).append("\n");
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

