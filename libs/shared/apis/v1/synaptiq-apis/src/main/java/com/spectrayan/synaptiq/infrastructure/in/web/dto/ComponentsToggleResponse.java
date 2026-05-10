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
 * ComponentsToggleResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-10T17:15:52.297398600-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ComponentsToggleResponse {

  private @Nullable Boolean itemCard;

  private @Nullable Boolean itemGrid;

  private @Nullable Boolean itemDetail;

  private @Nullable Boolean comparisonTable;

  private @Nullable Boolean filterSummary;

  private @Nullable Boolean resultCount;

  private @Nullable Boolean emptyState;

  private @Nullable Boolean actionConfirm;

  private @Nullable Boolean infoBanner;

  public ComponentsToggleResponse itemCard(@Nullable Boolean itemCard) {
    this.itemCard = itemCard;
    return this;
  }

  /**
   * Get itemCard
   * @return itemCard
   */
  
  @Schema(name = "itemCard", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("itemCard")
  public @Nullable Boolean getItemCard() {
    return itemCard;
  }

  @JsonProperty("itemCard")
  public void setItemCard(@Nullable Boolean itemCard) {
    this.itemCard = itemCard;
  }

  public ComponentsToggleResponse itemGrid(@Nullable Boolean itemGrid) {
    this.itemGrid = itemGrid;
    return this;
  }

  /**
   * Get itemGrid
   * @return itemGrid
   */
  
  @Schema(name = "itemGrid", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("itemGrid")
  public @Nullable Boolean getItemGrid() {
    return itemGrid;
  }

  @JsonProperty("itemGrid")
  public void setItemGrid(@Nullable Boolean itemGrid) {
    this.itemGrid = itemGrid;
  }

  public ComponentsToggleResponse itemDetail(@Nullable Boolean itemDetail) {
    this.itemDetail = itemDetail;
    return this;
  }

  /**
   * Get itemDetail
   * @return itemDetail
   */
  
  @Schema(name = "itemDetail", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("itemDetail")
  public @Nullable Boolean getItemDetail() {
    return itemDetail;
  }

  @JsonProperty("itemDetail")
  public void setItemDetail(@Nullable Boolean itemDetail) {
    this.itemDetail = itemDetail;
  }

  public ComponentsToggleResponse comparisonTable(@Nullable Boolean comparisonTable) {
    this.comparisonTable = comparisonTable;
    return this;
  }

  /**
   * Get comparisonTable
   * @return comparisonTable
   */
  
  @Schema(name = "comparisonTable", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("comparisonTable")
  public @Nullable Boolean getComparisonTable() {
    return comparisonTable;
  }

  @JsonProperty("comparisonTable")
  public void setComparisonTable(@Nullable Boolean comparisonTable) {
    this.comparisonTable = comparisonTable;
  }

  public ComponentsToggleResponse filterSummary(@Nullable Boolean filterSummary) {
    this.filterSummary = filterSummary;
    return this;
  }

  /**
   * Get filterSummary
   * @return filterSummary
   */
  
  @Schema(name = "filterSummary", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("filterSummary")
  public @Nullable Boolean getFilterSummary() {
    return filterSummary;
  }

  @JsonProperty("filterSummary")
  public void setFilterSummary(@Nullable Boolean filterSummary) {
    this.filterSummary = filterSummary;
  }

  public ComponentsToggleResponse resultCount(@Nullable Boolean resultCount) {
    this.resultCount = resultCount;
    return this;
  }

  /**
   * Get resultCount
   * @return resultCount
   */
  
  @Schema(name = "resultCount", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("resultCount")
  public @Nullable Boolean getResultCount() {
    return resultCount;
  }

  @JsonProperty("resultCount")
  public void setResultCount(@Nullable Boolean resultCount) {
    this.resultCount = resultCount;
  }

  public ComponentsToggleResponse emptyState(@Nullable Boolean emptyState) {
    this.emptyState = emptyState;
    return this;
  }

  /**
   * Get emptyState
   * @return emptyState
   */
  
  @Schema(name = "emptyState", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("emptyState")
  public @Nullable Boolean getEmptyState() {
    return emptyState;
  }

  @JsonProperty("emptyState")
  public void setEmptyState(@Nullable Boolean emptyState) {
    this.emptyState = emptyState;
  }

  public ComponentsToggleResponse actionConfirm(@Nullable Boolean actionConfirm) {
    this.actionConfirm = actionConfirm;
    return this;
  }

  /**
   * Get actionConfirm
   * @return actionConfirm
   */
  
  @Schema(name = "actionConfirm", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("actionConfirm")
  public @Nullable Boolean getActionConfirm() {
    return actionConfirm;
  }

  @JsonProperty("actionConfirm")
  public void setActionConfirm(@Nullable Boolean actionConfirm) {
    this.actionConfirm = actionConfirm;
  }

  public ComponentsToggleResponse infoBanner(@Nullable Boolean infoBanner) {
    this.infoBanner = infoBanner;
    return this;
  }

  /**
   * Get infoBanner
   * @return infoBanner
   */
  
  @Schema(name = "infoBanner", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("infoBanner")
  public @Nullable Boolean getInfoBanner() {
    return infoBanner;
  }

  @JsonProperty("infoBanner")
  public void setInfoBanner(@Nullable Boolean infoBanner) {
    this.infoBanner = infoBanner;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ComponentsToggleResponse componentsToggleResponse = (ComponentsToggleResponse) o;
    return Objects.equals(this.itemCard, componentsToggleResponse.itemCard) &&
        Objects.equals(this.itemGrid, componentsToggleResponse.itemGrid) &&
        Objects.equals(this.itemDetail, componentsToggleResponse.itemDetail) &&
        Objects.equals(this.comparisonTable, componentsToggleResponse.comparisonTable) &&
        Objects.equals(this.filterSummary, componentsToggleResponse.filterSummary) &&
        Objects.equals(this.resultCount, componentsToggleResponse.resultCount) &&
        Objects.equals(this.emptyState, componentsToggleResponse.emptyState) &&
        Objects.equals(this.actionConfirm, componentsToggleResponse.actionConfirm) &&
        Objects.equals(this.infoBanner, componentsToggleResponse.infoBanner);
  }

  @Override
  public int hashCode() {
    return Objects.hash(itemCard, itemGrid, itemDetail, comparisonTable, filterSummary, resultCount, emptyState, actionConfirm, infoBanner);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ComponentsToggleResponse {\n");
    sb.append("    itemCard: ").append(toIndentedString(itemCard)).append("\n");
    sb.append("    itemGrid: ").append(toIndentedString(itemGrid)).append("\n");
    sb.append("    itemDetail: ").append(toIndentedString(itemDetail)).append("\n");
    sb.append("    comparisonTable: ").append(toIndentedString(comparisonTable)).append("\n");
    sb.append("    filterSummary: ").append(toIndentedString(filterSummary)).append("\n");
    sb.append("    resultCount: ").append(toIndentedString(resultCount)).append("\n");
    sb.append("    emptyState: ").append(toIndentedString(emptyState)).append("\n");
    sb.append("    actionConfirm: ").append(toIndentedString(actionConfirm)).append("\n");
    sb.append("    infoBanner: ").append(toIndentedString(infoBanner)).append("\n");
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

