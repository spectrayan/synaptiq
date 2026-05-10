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
 * BrandingResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-10T17:15:52.297398600-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class BrandingResponse {

  private @Nullable String logoUrl;

  private @Nullable String primaryColor;

  private @Nullable String secondaryColor;

  private @Nullable String backgroundStyle;

  private @Nullable String headingFont;

  private @Nullable String bodyFont;

  private @Nullable String faviconUrl;

  private @Nullable String pageTitle;

  private @Nullable Boolean showPlatformBranding;

  public BrandingResponse logoUrl(@Nullable String logoUrl) {
    this.logoUrl = logoUrl;
    return this;
  }

  /**
   * Get logoUrl
   * @return logoUrl
   */
  
  @Schema(name = "logoUrl", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("logoUrl")
  public @Nullable String getLogoUrl() {
    return logoUrl;
  }

  @JsonProperty("logoUrl")
  public void setLogoUrl(@Nullable String logoUrl) {
    this.logoUrl = logoUrl;
  }

  public BrandingResponse primaryColor(@Nullable String primaryColor) {
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

  public BrandingResponse secondaryColor(@Nullable String secondaryColor) {
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

  public BrandingResponse backgroundStyle(@Nullable String backgroundStyle) {
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

  public BrandingResponse headingFont(@Nullable String headingFont) {
    this.headingFont = headingFont;
    return this;
  }

  /**
   * Get headingFont
   * @return headingFont
   */
  
  @Schema(name = "headingFont", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("headingFont")
  public @Nullable String getHeadingFont() {
    return headingFont;
  }

  @JsonProperty("headingFont")
  public void setHeadingFont(@Nullable String headingFont) {
    this.headingFont = headingFont;
  }

  public BrandingResponse bodyFont(@Nullable String bodyFont) {
    this.bodyFont = bodyFont;
    return this;
  }

  /**
   * Get bodyFont
   * @return bodyFont
   */
  
  @Schema(name = "bodyFont", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("bodyFont")
  public @Nullable String getBodyFont() {
    return bodyFont;
  }

  @JsonProperty("bodyFont")
  public void setBodyFont(@Nullable String bodyFont) {
    this.bodyFont = bodyFont;
  }

  public BrandingResponse faviconUrl(@Nullable String faviconUrl) {
    this.faviconUrl = faviconUrl;
    return this;
  }

  /**
   * Get faviconUrl
   * @return faviconUrl
   */
  
  @Schema(name = "faviconUrl", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("faviconUrl")
  public @Nullable String getFaviconUrl() {
    return faviconUrl;
  }

  @JsonProperty("faviconUrl")
  public void setFaviconUrl(@Nullable String faviconUrl) {
    this.faviconUrl = faviconUrl;
  }

  public BrandingResponse pageTitle(@Nullable String pageTitle) {
    this.pageTitle = pageTitle;
    return this;
  }

  /**
   * Get pageTitle
   * @return pageTitle
   */
  
  @Schema(name = "pageTitle", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("pageTitle")
  public @Nullable String getPageTitle() {
    return pageTitle;
  }

  @JsonProperty("pageTitle")
  public void setPageTitle(@Nullable String pageTitle) {
    this.pageTitle = pageTitle;
  }

  public BrandingResponse showPlatformBranding(@Nullable Boolean showPlatformBranding) {
    this.showPlatformBranding = showPlatformBranding;
    return this;
  }

  /**
   * Get showPlatformBranding
   * @return showPlatformBranding
   */
  
  @Schema(name = "showPlatformBranding", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("showPlatformBranding")
  public @Nullable Boolean getShowPlatformBranding() {
    return showPlatformBranding;
  }

  @JsonProperty("showPlatformBranding")
  public void setShowPlatformBranding(@Nullable Boolean showPlatformBranding) {
    this.showPlatformBranding = showPlatformBranding;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BrandingResponse brandingResponse = (BrandingResponse) o;
    return Objects.equals(this.logoUrl, brandingResponse.logoUrl) &&
        Objects.equals(this.primaryColor, brandingResponse.primaryColor) &&
        Objects.equals(this.secondaryColor, brandingResponse.secondaryColor) &&
        Objects.equals(this.backgroundStyle, brandingResponse.backgroundStyle) &&
        Objects.equals(this.headingFont, brandingResponse.headingFont) &&
        Objects.equals(this.bodyFont, brandingResponse.bodyFont) &&
        Objects.equals(this.faviconUrl, brandingResponse.faviconUrl) &&
        Objects.equals(this.pageTitle, brandingResponse.pageTitle) &&
        Objects.equals(this.showPlatformBranding, brandingResponse.showPlatformBranding);
  }

  @Override
  public int hashCode() {
    return Objects.hash(logoUrl, primaryColor, secondaryColor, backgroundStyle, headingFont, bodyFont, faviconUrl, pageTitle, showPlatformBranding);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BrandingResponse {\n");
    sb.append("    logoUrl: ").append(toIndentedString(logoUrl)).append("\n");
    sb.append("    primaryColor: ").append(toIndentedString(primaryColor)).append("\n");
    sb.append("    secondaryColor: ").append(toIndentedString(secondaryColor)).append("\n");
    sb.append("    backgroundStyle: ").append(toIndentedString(backgroundStyle)).append("\n");
    sb.append("    headingFont: ").append(toIndentedString(headingFont)).append("\n");
    sb.append("    bodyFont: ").append(toIndentedString(bodyFont)).append("\n");
    sb.append("    faviconUrl: ").append(toIndentedString(faviconUrl)).append("\n");
    sb.append("    pageTitle: ").append(toIndentedString(pageTitle)).append("\n");
    sb.append("    showPlatformBranding: ").append(toIndentedString(showPlatformBranding)).append("\n");
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

