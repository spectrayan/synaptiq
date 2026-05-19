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
 * ThemePresetResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-18T21:07:32.055501800-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ThemePresetResponse {

  private @Nullable String themeId;

  private @Nullable String name;

  private @Nullable String primaryColor;

  private @Nullable String secondaryColor;

  private @Nullable String backgroundStyle;

  private @Nullable Boolean isDefault;

  public ThemePresetResponse themeId(@Nullable String themeId) {
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

  public ThemePresetResponse name(@Nullable String name) {
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

  public ThemePresetResponse primaryColor(@Nullable String primaryColor) {
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

  public ThemePresetResponse secondaryColor(@Nullable String secondaryColor) {
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

  public ThemePresetResponse backgroundStyle(@Nullable String backgroundStyle) {
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

  public ThemePresetResponse isDefault(@Nullable Boolean isDefault) {
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
    ThemePresetResponse themePresetResponse = (ThemePresetResponse) o;
    return Objects.equals(this.themeId, themePresetResponse.themeId) &&
        Objects.equals(this.name, themePresetResponse.name) &&
        Objects.equals(this.primaryColor, themePresetResponse.primaryColor) &&
        Objects.equals(this.secondaryColor, themePresetResponse.secondaryColor) &&
        Objects.equals(this.backgroundStyle, themePresetResponse.backgroundStyle) &&
        Objects.equals(this.isDefault, themePresetResponse.isDefault);
  }

  @Override
  public int hashCode() {
    return Objects.hash(themeId, name, primaryColor, secondaryColor, backgroundStyle, isDefault);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThemePresetResponse {\n");
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

