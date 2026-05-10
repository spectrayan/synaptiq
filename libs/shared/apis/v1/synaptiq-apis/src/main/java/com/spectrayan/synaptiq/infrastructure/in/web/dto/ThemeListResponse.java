package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.ThemePresetResponse;
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
 * ThemeListResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-10T17:15:52.297398600-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ThemeListResponse {

  @Valid
  private List<@Valid ThemePresetResponse> themes = new ArrayList<>();

  public ThemeListResponse themes(List<@Valid ThemePresetResponse> themes) {
    this.themes = themes;
    return this;
  }

  public ThemeListResponse addThemesItem(ThemePresetResponse themesItem) {
    if (this.themes == null) {
      this.themes = new ArrayList<>();
    }
    this.themes.add(themesItem);
    return this;
  }

  /**
   * Get themes
   * @return themes
   */
  @Valid 
  @Schema(name = "themes", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("themes")
  public List<@Valid ThemePresetResponse> getThemes() {
    return themes;
  }

  @JsonProperty("themes")
  public void setThemes(List<@Valid ThemePresetResponse> themes) {
    this.themes = themes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ThemeListResponse themeListResponse = (ThemeListResponse) o;
    return Objects.equals(this.themes, themeListResponse.themes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(themes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThemeListResponse {\n");
    sb.append("    themes: ").append(toIndentedString(themes)).append("\n");
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

