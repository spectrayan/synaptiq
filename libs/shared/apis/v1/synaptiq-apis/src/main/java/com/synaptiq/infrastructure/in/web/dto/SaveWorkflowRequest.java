package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.synaptiq.infrastructure.in.web.dto.FlowSettingsSpec;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * SaveWorkflowRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-05T21:15:25.464614100-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class SaveWorkflowRequest {

  private FlowSettingsSpec spec;

  public SaveWorkflowRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public SaveWorkflowRequest(FlowSettingsSpec spec) {
    this.spec = spec;
  }

  public SaveWorkflowRequest spec(FlowSettingsSpec spec) {
    this.spec = spec;
    return this;
  }

  /**
   * Get spec
   * @return spec
   */
  @NotNull @Valid 
  @Schema(name = "spec", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("spec")
  public FlowSettingsSpec getSpec() {
    return spec;
  }

  @JsonProperty("spec")
  public void setSpec(FlowSettingsSpec spec) {
    this.spec = spec;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SaveWorkflowRequest saveWorkflowRequest = (SaveWorkflowRequest) o;
    return Objects.equals(this.spec, saveWorkflowRequest.spec);
  }

  @Override
  public int hashCode() {
    return Objects.hash(spec);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SaveWorkflowRequest {\n");
    sb.append("    spec: ").append(toIndentedString(spec)).append("\n");
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

