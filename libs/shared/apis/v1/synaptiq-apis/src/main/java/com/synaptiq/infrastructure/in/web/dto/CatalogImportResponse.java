package com.synaptiq.infrastructure.in.web.dto;

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
 * CatalogImportResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-08T22:14:16.718368-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class CatalogImportResponse {

  private @Nullable Integer imported;

  private @Nullable Integer skipped;

  @Valid
  private List<String> errors = new ArrayList<>();

  public CatalogImportResponse imported(@Nullable Integer imported) {
    this.imported = imported;
    return this;
  }

  /**
   * Get imported
   * @return imported
   */
  
  @Schema(name = "imported", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("imported")
  public @Nullable Integer getImported() {
    return imported;
  }

  @JsonProperty("imported")
  public void setImported(@Nullable Integer imported) {
    this.imported = imported;
  }

  public CatalogImportResponse skipped(@Nullable Integer skipped) {
    this.skipped = skipped;
    return this;
  }

  /**
   * Get skipped
   * @return skipped
   */
  
  @Schema(name = "skipped", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("skipped")
  public @Nullable Integer getSkipped() {
    return skipped;
  }

  @JsonProperty("skipped")
  public void setSkipped(@Nullable Integer skipped) {
    this.skipped = skipped;
  }

  public CatalogImportResponse errors(List<String> errors) {
    this.errors = errors;
    return this;
  }

  public CatalogImportResponse addErrorsItem(String errorsItem) {
    if (this.errors == null) {
      this.errors = new ArrayList<>();
    }
    this.errors.add(errorsItem);
    return this;
  }

  /**
   * Get errors
   * @return errors
   */
  
  @Schema(name = "errors", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("errors")
  public List<String> getErrors() {
    return errors;
  }

  @JsonProperty("errors")
  public void setErrors(List<String> errors) {
    this.errors = errors;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CatalogImportResponse catalogImportResponse = (CatalogImportResponse) o;
    return Objects.equals(this.imported, catalogImportResponse.imported) &&
        Objects.equals(this.skipped, catalogImportResponse.skipped) &&
        Objects.equals(this.errors, catalogImportResponse.errors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(imported, skipped, errors);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CatalogImportResponse {\n");
    sb.append("    imported: ").append(toIndentedString(imported)).append("\n");
    sb.append("    skipped: ").append(toIndentedString(skipped)).append("\n");
    sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
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

