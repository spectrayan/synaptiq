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
 * SchemaFieldResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-08T22:14:16.718368-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class SchemaFieldResponse {

  private @Nullable String fieldId;

  private @Nullable String label;

  private @Nullable String type;

  private @Nullable Boolean required;

  private @Nullable Boolean searchable;

  private @Nullable Boolean displayable;

  private @Nullable Boolean filterable;

  private @Nullable Integer displayOrder;

  @Valid
  private List<String> enumValues = new ArrayList<>();

  public SchemaFieldResponse fieldId(@Nullable String fieldId) {
    this.fieldId = fieldId;
    return this;
  }

  /**
   * Get fieldId
   * @return fieldId
   */
  
  @Schema(name = "fieldId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("fieldId")
  public @Nullable String getFieldId() {
    return fieldId;
  }

  @JsonProperty("fieldId")
  public void setFieldId(@Nullable String fieldId) {
    this.fieldId = fieldId;
  }

  public SchemaFieldResponse label(@Nullable String label) {
    this.label = label;
    return this;
  }

  /**
   * Get label
   * @return label
   */
  
  @Schema(name = "label", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("label")
  public @Nullable String getLabel() {
    return label;
  }

  @JsonProperty("label")
  public void setLabel(@Nullable String label) {
    this.label = label;
  }

  public SchemaFieldResponse type(@Nullable String type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   * @return type
   */
  
  @Schema(name = "type", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("type")
  public @Nullable String getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(@Nullable String type) {
    this.type = type;
  }

  public SchemaFieldResponse required(@Nullable Boolean required) {
    this.required = required;
    return this;
  }

  /**
   * Get required
   * @return required
   */
  
  @Schema(name = "required", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("required")
  public @Nullable Boolean getRequired() {
    return required;
  }

  @JsonProperty("required")
  public void setRequired(@Nullable Boolean required) {
    this.required = required;
  }

  public SchemaFieldResponse searchable(@Nullable Boolean searchable) {
    this.searchable = searchable;
    return this;
  }

  /**
   * Get searchable
   * @return searchable
   */
  
  @Schema(name = "searchable", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("searchable")
  public @Nullable Boolean getSearchable() {
    return searchable;
  }

  @JsonProperty("searchable")
  public void setSearchable(@Nullable Boolean searchable) {
    this.searchable = searchable;
  }

  public SchemaFieldResponse displayable(@Nullable Boolean displayable) {
    this.displayable = displayable;
    return this;
  }

  /**
   * Get displayable
   * @return displayable
   */
  
  @Schema(name = "displayable", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("displayable")
  public @Nullable Boolean getDisplayable() {
    return displayable;
  }

  @JsonProperty("displayable")
  public void setDisplayable(@Nullable Boolean displayable) {
    this.displayable = displayable;
  }

  public SchemaFieldResponse filterable(@Nullable Boolean filterable) {
    this.filterable = filterable;
    return this;
  }

  /**
   * Get filterable
   * @return filterable
   */
  
  @Schema(name = "filterable", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("filterable")
  public @Nullable Boolean getFilterable() {
    return filterable;
  }

  @JsonProperty("filterable")
  public void setFilterable(@Nullable Boolean filterable) {
    this.filterable = filterable;
  }

  public SchemaFieldResponse displayOrder(@Nullable Integer displayOrder) {
    this.displayOrder = displayOrder;
    return this;
  }

  /**
   * Get displayOrder
   * @return displayOrder
   */
  
  @Schema(name = "displayOrder", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("displayOrder")
  public @Nullable Integer getDisplayOrder() {
    return displayOrder;
  }

  @JsonProperty("displayOrder")
  public void setDisplayOrder(@Nullable Integer displayOrder) {
    this.displayOrder = displayOrder;
  }

  public SchemaFieldResponse enumValues(List<String> enumValues) {
    this.enumValues = enumValues;
    return this;
  }

  public SchemaFieldResponse addEnumValuesItem(String enumValuesItem) {
    if (this.enumValues == null) {
      this.enumValues = new ArrayList<>();
    }
    this.enumValues.add(enumValuesItem);
    return this;
  }

  /**
   * Get enumValues
   * @return enumValues
   */
  
  @Schema(name = "enumValues", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("enumValues")
  public List<String> getEnumValues() {
    return enumValues;
  }

  @JsonProperty("enumValues")
  public void setEnumValues(List<String> enumValues) {
    this.enumValues = enumValues;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SchemaFieldResponse schemaFieldResponse = (SchemaFieldResponse) o;
    return Objects.equals(this.fieldId, schemaFieldResponse.fieldId) &&
        Objects.equals(this.label, schemaFieldResponse.label) &&
        Objects.equals(this.type, schemaFieldResponse.type) &&
        Objects.equals(this.required, schemaFieldResponse.required) &&
        Objects.equals(this.searchable, schemaFieldResponse.searchable) &&
        Objects.equals(this.displayable, schemaFieldResponse.displayable) &&
        Objects.equals(this.filterable, schemaFieldResponse.filterable) &&
        Objects.equals(this.displayOrder, schemaFieldResponse.displayOrder) &&
        Objects.equals(this.enumValues, schemaFieldResponse.enumValues);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fieldId, label, type, required, searchable, displayable, filterable, displayOrder, enumValues);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SchemaFieldResponse {\n");
    sb.append("    fieldId: ").append(toIndentedString(fieldId)).append("\n");
    sb.append("    label: ").append(toIndentedString(label)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    required: ").append(toIndentedString(required)).append("\n");
    sb.append("    searchable: ").append(toIndentedString(searchable)).append("\n");
    sb.append("    displayable: ").append(toIndentedString(displayable)).append("\n");
    sb.append("    filterable: ").append(toIndentedString(filterable)).append("\n");
    sb.append("    displayOrder: ").append(toIndentedString(displayOrder)).append("\n");
    sb.append("    enumValues: ").append(toIndentedString(enumValues)).append("\n");
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

