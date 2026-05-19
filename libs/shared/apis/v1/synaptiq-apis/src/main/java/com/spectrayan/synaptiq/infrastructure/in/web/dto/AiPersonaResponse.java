package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
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
 * AiPersonaResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-18T21:07:32.055501800-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class AiPersonaResponse {

  private @Nullable String displayName;

  private @Nullable String tone;

  private @Nullable String customInstruction;

  private @Nullable String welcomeMessage;

  @Valid
  private List<String> starterPrompts = new ArrayList<>();

  public AiPersonaResponse displayName(@Nullable String displayName) {
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

  public AiPersonaResponse tone(@Nullable String tone) {
    this.tone = tone;
    return this;
  }

  /**
   * Get tone
   * @return tone
   */
  
  @Schema(name = "tone", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("tone")
  public @Nullable String getTone() {
    return tone;
  }

  @JsonProperty("tone")
  public void setTone(@Nullable String tone) {
    this.tone = tone;
  }

  public AiPersonaResponse customInstruction(@Nullable String customInstruction) {
    this.customInstruction = customInstruction;
    return this;
  }

  /**
   * Get customInstruction
   * @return customInstruction
   */
  
  @Schema(name = "customInstruction", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("customInstruction")
  public @Nullable String getCustomInstruction() {
    return customInstruction;
  }

  @JsonProperty("customInstruction")
  public void setCustomInstruction(@Nullable String customInstruction) {
    this.customInstruction = customInstruction;
  }

  public AiPersonaResponse welcomeMessage(@Nullable String welcomeMessage) {
    this.welcomeMessage = welcomeMessage;
    return this;
  }

  /**
   * Get welcomeMessage
   * @return welcomeMessage
   */
  
  @Schema(name = "welcomeMessage", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("welcomeMessage")
  public @Nullable String getWelcomeMessage() {
    return welcomeMessage;
  }

  @JsonProperty("welcomeMessage")
  public void setWelcomeMessage(@Nullable String welcomeMessage) {
    this.welcomeMessage = welcomeMessage;
  }

  public AiPersonaResponse starterPrompts(List<String> starterPrompts) {
    this.starterPrompts = starterPrompts;
    return this;
  }

  public AiPersonaResponse addStarterPromptsItem(String starterPromptsItem) {
    if (this.starterPrompts == null) {
      this.starterPrompts = new ArrayList<>();
    }
    this.starterPrompts.add(starterPromptsItem);
    return this;
  }

  /**
   * Get starterPrompts
   * @return starterPrompts
   */
  
  @Schema(name = "starterPrompts", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("starterPrompts")
  public List<String> getStarterPrompts() {
    return starterPrompts;
  }

  @JsonProperty("starterPrompts")
  public void setStarterPrompts(List<String> starterPrompts) {
    this.starterPrompts = starterPrompts;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AiPersonaResponse aiPersonaResponse = (AiPersonaResponse) o;
    return Objects.equals(this.displayName, aiPersonaResponse.displayName) &&
        Objects.equals(this.tone, aiPersonaResponse.tone) &&
        Objects.equals(this.customInstruction, aiPersonaResponse.customInstruction) &&
        Objects.equals(this.welcomeMessage, aiPersonaResponse.welcomeMessage) &&
        Objects.equals(this.starterPrompts, aiPersonaResponse.starterPrompts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(displayName, tone, customInstruction, welcomeMessage, starterPrompts);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AiPersonaResponse {\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    tone: ").append(toIndentedString(tone)).append("\n");
    sb.append("    customInstruction: ").append(toIndentedString(customInstruction)).append("\n");
    sb.append("    welcomeMessage: ").append(toIndentedString(welcomeMessage)).append("\n");
    sb.append("    starterPrompts: ").append(toIndentedString(starterPrompts)).append("\n");
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

