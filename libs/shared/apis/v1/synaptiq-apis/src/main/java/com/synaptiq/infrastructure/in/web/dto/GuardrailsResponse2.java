package com.synaptiq.infrastructure.in.web.dto;

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
 * AI guardrails configuration
 */

@Schema(name = "GuardrailsResponse-2", description = "AI guardrails configuration")
@JsonTypeName("GuardrailsResponse-2")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-05T13:34:15.888298700-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class GuardrailsResponse2 {

  private @Nullable String outOfScopeMessage;

  private @Nullable Boolean recommendationMode;

  private @Nullable String language;

  public GuardrailsResponse2 outOfScopeMessage(@Nullable String outOfScopeMessage) {
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

  public GuardrailsResponse2 recommendationMode(@Nullable Boolean recommendationMode) {
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

  public GuardrailsResponse2 language(@Nullable String language) {
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
    GuardrailsResponse2 guardrailsResponse2 = (GuardrailsResponse2) o;
    return Objects.equals(this.outOfScopeMessage, guardrailsResponse2.outOfScopeMessage) &&
        Objects.equals(this.recommendationMode, guardrailsResponse2.recommendationMode) &&
        Objects.equals(this.language, guardrailsResponse2.language);
  }

  @Override
  public int hashCode() {
    return Objects.hash(outOfScopeMessage, recommendationMode, language);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GuardrailsResponse2 {\n");
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

