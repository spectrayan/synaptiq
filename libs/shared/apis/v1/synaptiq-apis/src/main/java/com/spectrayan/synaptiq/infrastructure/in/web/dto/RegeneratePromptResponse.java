package com.spectrayan.synaptiq.infrastructure.in.web.dto;

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
 * Response from prompt regeneration
 */

@Schema(name = "RegeneratePromptResponse", description = "Response from prompt regeneration")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-18T21:07:32.055501800-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class RegeneratePromptResponse {

  private @Nullable String prompt;

  public RegeneratePromptResponse prompt(@Nullable String prompt) {
    this.prompt = prompt;
    return this;
  }

  /**
   * The regenerated prompt text
   * @return prompt
   */
  
  @Schema(name = "prompt", description = "The regenerated prompt text", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("prompt")
  public @Nullable String getPrompt() {
    return prompt;
  }

  @JsonProperty("prompt")
  public void setPrompt(@Nullable String prompt) {
    this.prompt = prompt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RegeneratePromptResponse regeneratePromptResponse = (RegeneratePromptResponse) o;
    return Objects.equals(this.prompt, regeneratePromptResponse.prompt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(prompt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RegeneratePromptResponse {\n");
    sb.append("    prompt: ").append(toIndentedString(prompt)).append("\n");
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

