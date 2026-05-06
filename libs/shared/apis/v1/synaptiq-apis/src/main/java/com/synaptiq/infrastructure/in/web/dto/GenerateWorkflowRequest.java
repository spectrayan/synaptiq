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
 * Request body for AI-powered workflow generation
 */

@Schema(name = "GenerateWorkflowRequest", description = "Request body for AI-powered workflow generation")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-05T21:15:25.464614100-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class GenerateWorkflowRequest {

  private String prompt;

  public GenerateWorkflowRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public GenerateWorkflowRequest(String prompt) {
    this.prompt = prompt;
  }

  public GenerateWorkflowRequest prompt(String prompt) {
    this.prompt = prompt;
    return this;
  }

  /**
   * Natural language description of the desired workflow
   * @return prompt
   */
  @NotNull 
  @Schema(name = "prompt", description = "Natural language description of the desired workflow", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("prompt")
  public String getPrompt() {
    return prompt;
  }

  @JsonProperty("prompt")
  public void setPrompt(String prompt) {
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
    GenerateWorkflowRequest generateWorkflowRequest = (GenerateWorkflowRequest) o;
    return Objects.equals(this.prompt, generateWorkflowRequest.prompt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(prompt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GenerateWorkflowRequest {\n");
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

