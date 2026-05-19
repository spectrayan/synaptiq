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
 * ContrastCheckResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-18T21:07:32.055501800-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ContrastCheckResponse {

  private @Nullable String foreground;

  private @Nullable String background;

  private @Nullable Double ratio;

  private @Nullable Boolean aaNormal;

  private @Nullable Boolean aaLarge;

  private @Nullable Boolean aaaNormal;

  public ContrastCheckResponse foreground(@Nullable String foreground) {
    this.foreground = foreground;
    return this;
  }

  /**
   * Get foreground
   * @return foreground
   */
  
  @Schema(name = "foreground", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("foreground")
  public @Nullable String getForeground() {
    return foreground;
  }

  @JsonProperty("foreground")
  public void setForeground(@Nullable String foreground) {
    this.foreground = foreground;
  }

  public ContrastCheckResponse background(@Nullable String background) {
    this.background = background;
    return this;
  }

  /**
   * Get background
   * @return background
   */
  
  @Schema(name = "background", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("background")
  public @Nullable String getBackground() {
    return background;
  }

  @JsonProperty("background")
  public void setBackground(@Nullable String background) {
    this.background = background;
  }

  public ContrastCheckResponse ratio(@Nullable Double ratio) {
    this.ratio = ratio;
    return this;
  }

  /**
   * Get ratio
   * @return ratio
   */
  
  @Schema(name = "ratio", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("ratio")
  public @Nullable Double getRatio() {
    return ratio;
  }

  @JsonProperty("ratio")
  public void setRatio(@Nullable Double ratio) {
    this.ratio = ratio;
  }

  public ContrastCheckResponse aaNormal(@Nullable Boolean aaNormal) {
    this.aaNormal = aaNormal;
    return this;
  }

  /**
   * Get aaNormal
   * @return aaNormal
   */
  
  @Schema(name = "aaNormal", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("aaNormal")
  public @Nullable Boolean getAaNormal() {
    return aaNormal;
  }

  @JsonProperty("aaNormal")
  public void setAaNormal(@Nullable Boolean aaNormal) {
    this.aaNormal = aaNormal;
  }

  public ContrastCheckResponse aaLarge(@Nullable Boolean aaLarge) {
    this.aaLarge = aaLarge;
    return this;
  }

  /**
   * Get aaLarge
   * @return aaLarge
   */
  
  @Schema(name = "aaLarge", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("aaLarge")
  public @Nullable Boolean getAaLarge() {
    return aaLarge;
  }

  @JsonProperty("aaLarge")
  public void setAaLarge(@Nullable Boolean aaLarge) {
    this.aaLarge = aaLarge;
  }

  public ContrastCheckResponse aaaNormal(@Nullable Boolean aaaNormal) {
    this.aaaNormal = aaaNormal;
    return this;
  }

  /**
   * Get aaaNormal
   * @return aaaNormal
   */
  
  @Schema(name = "aaaNormal", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("aaaNormal")
  public @Nullable Boolean getAaaNormal() {
    return aaaNormal;
  }

  @JsonProperty("aaaNormal")
  public void setAaaNormal(@Nullable Boolean aaaNormal) {
    this.aaaNormal = aaaNormal;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ContrastCheckResponse contrastCheckResponse = (ContrastCheckResponse) o;
    return Objects.equals(this.foreground, contrastCheckResponse.foreground) &&
        Objects.equals(this.background, contrastCheckResponse.background) &&
        Objects.equals(this.ratio, contrastCheckResponse.ratio) &&
        Objects.equals(this.aaNormal, contrastCheckResponse.aaNormal) &&
        Objects.equals(this.aaLarge, contrastCheckResponse.aaLarge) &&
        Objects.equals(this.aaaNormal, contrastCheckResponse.aaaNormal);
  }

  @Override
  public int hashCode() {
    return Objects.hash(foreground, background, ratio, aaNormal, aaLarge, aaaNormal);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ContrastCheckResponse {\n");
    sb.append("    foreground: ").append(toIndentedString(foreground)).append("\n");
    sb.append("    background: ").append(toIndentedString(background)).append("\n");
    sb.append("    ratio: ").append(toIndentedString(ratio)).append("\n");
    sb.append("    aaNormal: ").append(toIndentedString(aaNormal)).append("\n");
    sb.append("    aaLarge: ").append(toIndentedString(aaLarge)).append("\n");
    sb.append("    aaaNormal: ").append(toIndentedString(aaaNormal)).append("\n");
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

