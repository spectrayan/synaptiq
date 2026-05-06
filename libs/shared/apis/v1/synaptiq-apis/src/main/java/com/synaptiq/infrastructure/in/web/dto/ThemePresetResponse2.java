package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Theme preset configuration
 */

@Schema(name = "ThemePresetResponse-2", description = "Theme preset configuration")
@JsonTypeName("ThemePresetResponse-2")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-05T13:34:15.888298700-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ThemePresetResponse2 {

  private @Nullable String themeId;

  private @Nullable String name;

  private @Nullable String primaryColor;

  private @Nullable String secondaryColor;

  private @Nullable String backgroundStyle;

  private @Nullable Boolean isDefault;

  public ThemePresetResponse2 themeId(@Nullable String themeId) {
    this.themeId = themeId;
    return this;
  }

  /**
   * Get themeId
   * @return themeId
   */
  
  @Schema(name = "themeId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("themeId")
  public @Nullable String getThemeId() {
    return themeId;
  }

  @JsonProperty("themeId")
  public void setThemeId(@Nullable String themeId) {
    this.themeId = themeId;
  }

  public ThemePresetResponse2 name(@Nullable String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
   */
  
  @Schema(name = "name", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("name")
  public @Nullable String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(@Nullable String name) {
    this.name = name;
  }

  public ThemePresetResponse2 primaryColor(@Nullable String primaryColor) {
    this.primaryColor = primaryColor;
    return this;
  }

  /**
   * Get primaryColor
   * @return primaryColor
   */
  
  @Schema(name = "primaryColor", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("primaryColor")
  public @Nullable String getPrimaryColor() {
    return primaryColor;
  }

  @JsonProperty("primaryColor")
  public void setPrimaryColor(@Nullable String primaryColor) {
    this.primaryColor = primaryColor;
  }

  public ThemePresetResponse2 secondaryColor(@Nullable String secondaryColor) {
    this.secondaryColor = secondaryColor;
    return this;
  }

  /**
   * Get secondaryColor
   * @return secondaryColor
   */
  
  @Schema(name = "secondaryColor", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("secondaryColor")
  public @Nullable String getSecondaryColor() {
    return secondaryColor;
  }

  @JsonProperty("secondaryColor")
  public void setSecondaryColor(@Nullable String secondaryColor) {
    this.secondaryColor = secondaryColor;
  }

  public ThemePresetResponse2 backgroundStyle(@Nullable String backgroundStyle) {
    this.backgroundStyle = backgroundStyle;
    return this;
  }

  /**
   * Get backgroundStyle
   * @return backgroundStyle
   */
  
  @Schema(name = "backgroundStyle", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("backgroundStyle")
  public @Nullable String getBackgroundStyle() {
    return backgroundStyle;
  }

  @JsonProperty("backgroundStyle")
  public void setBackgroundStyle(@Nullable String backgroundStyle) {
    this.backgroundStyle = backgroundStyle;
  }

  public ThemePresetResponse2 isDefault(@Nullable Boolean isDefault) {
    this.isDefault = isDefault;
    return this;
  }

  /**
   * Get isDefault
   * @return isDefault
   */
  
  @Schema(name = "isDefault", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("isDefault")
  public @Nullable Boolean getIsDefault() {
    return isDefault;
  }

  @JsonProperty("isDefault")
  public void setIsDefault(@Nullable Boolean isDefault) {
    this.isDefault = isDefault;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ThemePresetResponse2 themePresetResponse2 = (ThemePresetResponse2) o;
    return Objects.equals(this.themeId, themePresetResponse2.themeId) &&
        Objects.equals(this.name, themePresetResponse2.name) &&
        Objects.equals(this.primaryColor, themePresetResponse2.primaryColor) &&
        Objects.equals(this.secondaryColor, themePresetResponse2.secondaryColor) &&
        Objects.equals(this.backgroundStyle, themePresetResponse2.backgroundStyle) &&
        Objects.equals(this.isDefault, themePresetResponse2.isDefault);
  }

  @Override
  public int hashCode() {
    return Objects.hash(themeId, name, primaryColor, secondaryColor, backgroundStyle, isDefault);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThemePresetResponse2 {\n");
    sb.append("    themeId: ").append(toIndentedString(themeId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    primaryColor: ").append(toIndentedString(primaryColor)).append("\n");
    sb.append("    secondaryColor: ").append(toIndentedString(secondaryColor)).append("\n");
    sb.append("    backgroundStyle: ").append(toIndentedString(backgroundStyle)).append("\n");
    sb.append("    isDefault: ").append(toIndentedString(isDefault)).append("\n");
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

