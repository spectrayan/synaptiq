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
 * End-user personalization options
 */

@Schema(name = "PersonalizationResponse-2", description = "End-user personalization options")
@JsonTypeName("PersonalizationResponse-2")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-05T13:34:15.888298700-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class PersonalizationResponse2 {

  private @Nullable Boolean allowThemeSwitch;

  private @Nullable Boolean allowFontSwitch;

  private @Nullable Boolean allowBubbleStyle;

  public PersonalizationResponse2 allowThemeSwitch(@Nullable Boolean allowThemeSwitch) {
    this.allowThemeSwitch = allowThemeSwitch;
    return this;
  }

  /**
   * Get allowThemeSwitch
   * @return allowThemeSwitch
   */
  
  @Schema(name = "allowThemeSwitch", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("allowThemeSwitch")
  public @Nullable Boolean getAllowThemeSwitch() {
    return allowThemeSwitch;
  }

  @JsonProperty("allowThemeSwitch")
  public void setAllowThemeSwitch(@Nullable Boolean allowThemeSwitch) {
    this.allowThemeSwitch = allowThemeSwitch;
  }

  public PersonalizationResponse2 allowFontSwitch(@Nullable Boolean allowFontSwitch) {
    this.allowFontSwitch = allowFontSwitch;
    return this;
  }

  /**
   * Get allowFontSwitch
   * @return allowFontSwitch
   */
  
  @Schema(name = "allowFontSwitch", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("allowFontSwitch")
  public @Nullable Boolean getAllowFontSwitch() {
    return allowFontSwitch;
  }

  @JsonProperty("allowFontSwitch")
  public void setAllowFontSwitch(@Nullable Boolean allowFontSwitch) {
    this.allowFontSwitch = allowFontSwitch;
  }

  public PersonalizationResponse2 allowBubbleStyle(@Nullable Boolean allowBubbleStyle) {
    this.allowBubbleStyle = allowBubbleStyle;
    return this;
  }

  /**
   * Get allowBubbleStyle
   * @return allowBubbleStyle
   */
  
  @Schema(name = "allowBubbleStyle", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("allowBubbleStyle")
  public @Nullable Boolean getAllowBubbleStyle() {
    return allowBubbleStyle;
  }

  @JsonProperty("allowBubbleStyle")
  public void setAllowBubbleStyle(@Nullable Boolean allowBubbleStyle) {
    this.allowBubbleStyle = allowBubbleStyle;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PersonalizationResponse2 personalizationResponse2 = (PersonalizationResponse2) o;
    return Objects.equals(this.allowThemeSwitch, personalizationResponse2.allowThemeSwitch) &&
        Objects.equals(this.allowFontSwitch, personalizationResponse2.allowFontSwitch) &&
        Objects.equals(this.allowBubbleStyle, personalizationResponse2.allowBubbleStyle);
  }

  @Override
  public int hashCode() {
    return Objects.hash(allowThemeSwitch, allowFontSwitch, allowBubbleStyle);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PersonalizationResponse2 {\n");
    sb.append("    allowThemeSwitch: ").append(toIndentedString(allowThemeSwitch)).append("\n");
    sb.append("    allowFontSwitch: ").append(toIndentedString(allowFontSwitch)).append("\n");
    sb.append("    allowBubbleStyle: ").append(toIndentedString(allowBubbleStyle)).append("\n");
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

