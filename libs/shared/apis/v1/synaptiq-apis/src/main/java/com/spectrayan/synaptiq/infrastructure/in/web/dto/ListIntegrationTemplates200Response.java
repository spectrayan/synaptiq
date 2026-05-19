package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.TemplateDescriptorResponse;
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
 * ListIntegrationTemplates200Response
 */

@JsonTypeName("listIntegrationTemplates_200_response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-18T21:07:32.055501800-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ListIntegrationTemplates200Response {

  @Valid
  private List<@Valid TemplateDescriptorResponse> templates = new ArrayList<>();

  public ListIntegrationTemplates200Response templates(List<@Valid TemplateDescriptorResponse> templates) {
    this.templates = templates;
    return this;
  }

  public ListIntegrationTemplates200Response addTemplatesItem(TemplateDescriptorResponse templatesItem) {
    if (this.templates == null) {
      this.templates = new ArrayList<>();
    }
    this.templates.add(templatesItem);
    return this;
  }

  /**
   * Get templates
   * @return templates
   */
  @Valid 
  @Schema(name = "templates", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("templates")
  public List<@Valid TemplateDescriptorResponse> getTemplates() {
    return templates;
  }

  @JsonProperty("templates")
  public void setTemplates(List<@Valid TemplateDescriptorResponse> templates) {
    this.templates = templates;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ListIntegrationTemplates200Response listIntegrationTemplates200Response = (ListIntegrationTemplates200Response) o;
    return Objects.equals(this.templates, listIntegrationTemplates200Response.templates);
  }

  @Override
  public int hashCode() {
    return Objects.hash(templates);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ListIntegrationTemplates200Response {\n");
    sb.append("    templates: ").append(toIndentedString(templates)).append("\n");
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

