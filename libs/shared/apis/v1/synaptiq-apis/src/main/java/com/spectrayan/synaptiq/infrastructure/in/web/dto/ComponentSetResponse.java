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
 * Component enable/disable toggles for the application
 */

@Schema(name = "ComponentSetResponse", description = "Component enable/disable toggles for the application")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-05T13:34:15.888298700-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ComponentSetResponse {

  private @Nullable Boolean itemCard;

  private @Nullable Boolean itemGrid;

  private @Nullable Boolean itemDetail;

  private @Nullable Boolean comparisonTable;

  private @Nullable Boolean filterSummary;

  private @Nullable Boolean resultCount;

  private @Nullable Boolean emptyState;

  private @Nullable Boolean actionConfirm;

  private @Nullable Boolean infoBanner;

  private @Nullable Boolean formInput;

  private @Nullable Boolean dataTable;

  private @Nullable Boolean kpiCard;

  private @Nullable Boolean chart;

  private @Nullable Boolean statGrid;

  private @Nullable Boolean kanban;

  private @Nullable Boolean timeline;

  private @Nullable Boolean metricTable;

  private @Nullable Boolean progressTracker;

  private @Nullable Boolean launchpad;

  public ComponentSetResponse itemCard(@Nullable Boolean itemCard) {
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

  public ComponentSetResponse itemGrid(@Nullable Boolean itemGrid) {
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

  public ComponentSetResponse itemDetail(@Nullable Boolean itemDetail) {
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

  public ComponentSetResponse comparisonTable(@Nullable Boolean comparisonTable) {
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

  public ComponentSetResponse filterSummary(@Nullable Boolean filterSummary) {
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

  public ComponentSetResponse resultCount(@Nullable Boolean resultCount) {
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

  public ComponentSetResponse emptyState(@Nullable Boolean emptyState) {
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

  public ComponentSetResponse actionConfirm(@Nullable Boolean actionConfirm) {
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

  public ComponentSetResponse infoBanner(@Nullable Boolean infoBanner) {
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

  public ComponentSetResponse formInput(@Nullable Boolean formInput) {
    this.formInput = formInput;
    return this;
  }

  /**
   * Get formInput
   * @return formInput
   */
  
  @Schema(name = "formInput", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("formInput")
  public @Nullable Boolean getFormInput() {
    return formInput;
  }

  @JsonProperty("formInput")
  public void setFormInput(@Nullable Boolean formInput) {
    this.formInput = formInput;
  }

  public ComponentSetResponse dataTable(@Nullable Boolean dataTable) {
    this.dataTable = dataTable;
    return this;
  }

  /**
   * Get dataTable
   * @return dataTable
   */
  
  @Schema(name = "dataTable", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("dataTable")
  public @Nullable Boolean getDataTable() {
    return dataTable;
  }

  @JsonProperty("dataTable")
  public void setDataTable(@Nullable Boolean dataTable) {
    this.dataTable = dataTable;
  }

  public ComponentSetResponse kpiCard(@Nullable Boolean kpiCard) {
    this.kpiCard = kpiCard;
    return this;
  }

  /**
   * Get kpiCard
   * @return kpiCard
   */
  
  @Schema(name = "kpiCard", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("kpiCard")
  public @Nullable Boolean getKpiCard() {
    return kpiCard;
  }

  @JsonProperty("kpiCard")
  public void setKpiCard(@Nullable Boolean kpiCard) {
    this.kpiCard = kpiCard;
  }

  public ComponentSetResponse chart(@Nullable Boolean chart) {
    this.chart = chart;
    return this;
  }

  /**
   * Get chart
   * @return chart
   */
  
  @Schema(name = "chart", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("chart")
  public @Nullable Boolean getChart() {
    return chart;
  }

  @JsonProperty("chart")
  public void setChart(@Nullable Boolean chart) {
    this.chart = chart;
  }

  public ComponentSetResponse statGrid(@Nullable Boolean statGrid) {
    this.statGrid = statGrid;
    return this;
  }

  /**
   * Get statGrid
   * @return statGrid
   */
  
  @Schema(name = "statGrid", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("statGrid")
  public @Nullable Boolean getStatGrid() {
    return statGrid;
  }

  @JsonProperty("statGrid")
  public void setStatGrid(@Nullable Boolean statGrid) {
    this.statGrid = statGrid;
  }

  public ComponentSetResponse kanban(@Nullable Boolean kanban) {
    this.kanban = kanban;
    return this;
  }

  /**
   * Get kanban
   * @return kanban
   */
  
  @Schema(name = "kanban", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("kanban")
  public @Nullable Boolean getKanban() {
    return kanban;
  }

  @JsonProperty("kanban")
  public void setKanban(@Nullable Boolean kanban) {
    this.kanban = kanban;
  }

  public ComponentSetResponse timeline(@Nullable Boolean timeline) {
    this.timeline = timeline;
    return this;
  }

  /**
   * Get timeline
   * @return timeline
   */
  
  @Schema(name = "timeline", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("timeline")
  public @Nullable Boolean getTimeline() {
    return timeline;
  }

  @JsonProperty("timeline")
  public void setTimeline(@Nullable Boolean timeline) {
    this.timeline = timeline;
  }

  public ComponentSetResponse metricTable(@Nullable Boolean metricTable) {
    this.metricTable = metricTable;
    return this;
  }

  /**
   * Get metricTable
   * @return metricTable
   */
  
  @Schema(name = "metricTable", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("metricTable")
  public @Nullable Boolean getMetricTable() {
    return metricTable;
  }

  @JsonProperty("metricTable")
  public void setMetricTable(@Nullable Boolean metricTable) {
    this.metricTable = metricTable;
  }

  public ComponentSetResponse progressTracker(@Nullable Boolean progressTracker) {
    this.progressTracker = progressTracker;
    return this;
  }

  /**
   * Get progressTracker
   * @return progressTracker
   */
  
  @Schema(name = "progressTracker", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("progressTracker")
  public @Nullable Boolean getProgressTracker() {
    return progressTracker;
  }

  @JsonProperty("progressTracker")
  public void setProgressTracker(@Nullable Boolean progressTracker) {
    this.progressTracker = progressTracker;
  }

  public ComponentSetResponse launchpad(@Nullable Boolean launchpad) {
    this.launchpad = launchpad;
    return this;
  }

  /**
   * Get launchpad
   * @return launchpad
   */
  
  @Schema(name = "launchpad", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("launchpad")
  public @Nullable Boolean getLaunchpad() {
    return launchpad;
  }

  @JsonProperty("launchpad")
  public void setLaunchpad(@Nullable Boolean launchpad) {
    this.launchpad = launchpad;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ComponentSetResponse componentSetResponse = (ComponentSetResponse) o;
    return Objects.equals(this.itemCard, componentSetResponse.itemCard) &&
        Objects.equals(this.itemGrid, componentSetResponse.itemGrid) &&
        Objects.equals(this.itemDetail, componentSetResponse.itemDetail) &&
        Objects.equals(this.comparisonTable, componentSetResponse.comparisonTable) &&
        Objects.equals(this.filterSummary, componentSetResponse.filterSummary) &&
        Objects.equals(this.resultCount, componentSetResponse.resultCount) &&
        Objects.equals(this.emptyState, componentSetResponse.emptyState) &&
        Objects.equals(this.actionConfirm, componentSetResponse.actionConfirm) &&
        Objects.equals(this.infoBanner, componentSetResponse.infoBanner) &&
        Objects.equals(this.formInput, componentSetResponse.formInput) &&
        Objects.equals(this.dataTable, componentSetResponse.dataTable) &&
        Objects.equals(this.kpiCard, componentSetResponse.kpiCard) &&
        Objects.equals(this.chart, componentSetResponse.chart) &&
        Objects.equals(this.statGrid, componentSetResponse.statGrid) &&
        Objects.equals(this.kanban, componentSetResponse.kanban) &&
        Objects.equals(this.timeline, componentSetResponse.timeline) &&
        Objects.equals(this.metricTable, componentSetResponse.metricTable) &&
        Objects.equals(this.progressTracker, componentSetResponse.progressTracker) &&
        Objects.equals(this.launchpad, componentSetResponse.launchpad);
  }

  @Override
  public int hashCode() {
    return Objects.hash(itemCard, itemGrid, itemDetail, comparisonTable, filterSummary, resultCount, emptyState, actionConfirm, infoBanner, formInput, dataTable, kpiCard, chart, statGrid, kanban, timeline, metricTable, progressTracker, launchpad);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ComponentSetResponse {\n");
    sb.append("    itemCard: ").append(toIndentedString(itemCard)).append("\n");
    sb.append("    itemGrid: ").append(toIndentedString(itemGrid)).append("\n");
    sb.append("    itemDetail: ").append(toIndentedString(itemDetail)).append("\n");
    sb.append("    comparisonTable: ").append(toIndentedString(comparisonTable)).append("\n");
    sb.append("    filterSummary: ").append(toIndentedString(filterSummary)).append("\n");
    sb.append("    resultCount: ").append(toIndentedString(resultCount)).append("\n");
    sb.append("    emptyState: ").append(toIndentedString(emptyState)).append("\n");
    sb.append("    actionConfirm: ").append(toIndentedString(actionConfirm)).append("\n");
    sb.append("    infoBanner: ").append(toIndentedString(infoBanner)).append("\n");
    sb.append("    formInput: ").append(toIndentedString(formInput)).append("\n");
    sb.append("    dataTable: ").append(toIndentedString(dataTable)).append("\n");
    sb.append("    kpiCard: ").append(toIndentedString(kpiCard)).append("\n");
    sb.append("    chart: ").append(toIndentedString(chart)).append("\n");
    sb.append("    statGrid: ").append(toIndentedString(statGrid)).append("\n");
    sb.append("    kanban: ").append(toIndentedString(kanban)).append("\n");
    sb.append("    timeline: ").append(toIndentedString(timeline)).append("\n");
    sb.append("    metricTable: ").append(toIndentedString(metricTable)).append("\n");
    sb.append("    progressTracker: ").append(toIndentedString(progressTracker)).append("\n");
    sb.append("    launchpad: ").append(toIndentedString(launchpad)).append("\n");
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

