package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.synaptiq.infrastructure.in.web.dto.ApplicationResponse;
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
 * ListApplications200Response
 */

@JsonTypeName("listApplications_200_response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-08T22:14:16.718368-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ListApplications200Response {

  @Valid
  private List<@Valid ApplicationResponse> applications = new ArrayList<>();

  public ListApplications200Response applications(List<@Valid ApplicationResponse> applications) {
    this.applications = applications;
    return this;
  }

  public ListApplications200Response addApplicationsItem(ApplicationResponse applicationsItem) {
    if (this.applications == null) {
      this.applications = new ArrayList<>();
    }
    this.applications.add(applicationsItem);
    return this;
  }

  /**
   * Get applications
   * @return applications
   */
  @Valid 
  @Schema(name = "applications", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("applications")
  public List<@Valid ApplicationResponse> getApplications() {
    return applications;
  }

  @JsonProperty("applications")
  public void setApplications(List<@Valid ApplicationResponse> applications) {
    this.applications = applications;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ListApplications200Response listApplications200Response = (ListApplications200Response) o;
    return Objects.equals(this.applications, listApplications200Response.applications);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applications);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ListApplications200Response {\n");
    sb.append("    applications: ").append(toIndentedString(applications)).append("\n");
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

