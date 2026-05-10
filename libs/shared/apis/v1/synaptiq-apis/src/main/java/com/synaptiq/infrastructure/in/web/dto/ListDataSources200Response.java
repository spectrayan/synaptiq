package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.synaptiq.infrastructure.in.web.dto.DataSourceResponse;
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
 * ListDataSources200Response
 */

@JsonTypeName("listDataSources_200_response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-08T22:14:16.718368-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ListDataSources200Response {

  @Valid
  private List<@Valid DataSourceResponse> dataSources = new ArrayList<>();

  public ListDataSources200Response dataSources(List<@Valid DataSourceResponse> dataSources) {
    this.dataSources = dataSources;
    return this;
  }

  public ListDataSources200Response addDataSourcesItem(DataSourceResponse dataSourcesItem) {
    if (this.dataSources == null) {
      this.dataSources = new ArrayList<>();
    }
    this.dataSources.add(dataSourcesItem);
    return this;
  }

  /**
   * Get dataSources
   * @return dataSources
   */
  @Valid 
  @Schema(name = "dataSources", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("dataSources")
  public List<@Valid DataSourceResponse> getDataSources() {
    return dataSources;
  }

  @JsonProperty("dataSources")
  public void setDataSources(List<@Valid DataSourceResponse> dataSources) {
    this.dataSources = dataSources;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ListDataSources200Response listDataSources200Response = (ListDataSources200Response) o;
    return Objects.equals(this.dataSources, listDataSources200Response.dataSources);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dataSources);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ListDataSources200Response {\n");
    sb.append("    dataSources: ").append(toIndentedString(dataSources)).append("\n");
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

