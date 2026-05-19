package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.IntegrationResponse;
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
 * ListIntegrations200Response
 */

@JsonTypeName("listIntegrations_200_response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-18T21:07:32.055501800-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ListIntegrations200Response {

  @Valid
  private List<@Valid IntegrationResponse> integrations = new ArrayList<>();

  public ListIntegrations200Response integrations(List<@Valid IntegrationResponse> integrations) {
    this.integrations = integrations;
    return this;
  }

  public ListIntegrations200Response addIntegrationsItem(IntegrationResponse integrationsItem) {
    if (this.integrations == null) {
      this.integrations = new ArrayList<>();
    }
    this.integrations.add(integrationsItem);
    return this;
  }

  /**
   * Get integrations
   * @return integrations
   */
  @Valid 
  @Schema(name = "integrations", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("integrations")
  public List<@Valid IntegrationResponse> getIntegrations() {
    return integrations;
  }

  @JsonProperty("integrations")
  public void setIntegrations(List<@Valid IntegrationResponse> integrations) {
    this.integrations = integrations;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ListIntegrations200Response listIntegrations200Response = (ListIntegrations200Response) o;
    return Objects.equals(this.integrations, listIntegrations200Response.integrations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(integrations);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ListIntegrations200Response {\n");
    sb.append("    integrations: ").append(toIndentedString(integrations)).append("\n");
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

