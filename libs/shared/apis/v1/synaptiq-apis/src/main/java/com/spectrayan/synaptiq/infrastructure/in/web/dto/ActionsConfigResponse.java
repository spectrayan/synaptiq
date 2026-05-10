package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.ActionEntryResponse;
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
 * ActionsConfigResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-10T17:15:52.297398600-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class ActionsConfigResponse {

  @Valid
  private List<@Valid ActionEntryResponse> actions = new ArrayList<>();

  private @Nullable String enquiryWebhookUrl;

  private @Nullable String enquiryEmail;

  public ActionsConfigResponse actions(List<@Valid ActionEntryResponse> actions) {
    this.actions = actions;
    return this;
  }

  public ActionsConfigResponse addActionsItem(ActionEntryResponse actionsItem) {
    if (this.actions == null) {
      this.actions = new ArrayList<>();
    }
    this.actions.add(actionsItem);
    return this;
  }

  /**
   * Get actions
   * @return actions
   */
  @Valid 
  @Schema(name = "actions", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("actions")
  public List<@Valid ActionEntryResponse> getActions() {
    return actions;
  }

  @JsonProperty("actions")
  public void setActions(List<@Valid ActionEntryResponse> actions) {
    this.actions = actions;
  }

  public ActionsConfigResponse enquiryWebhookUrl(@Nullable String enquiryWebhookUrl) {
    this.enquiryWebhookUrl = enquiryWebhookUrl;
    return this;
  }

  /**
   * Get enquiryWebhookUrl
   * @return enquiryWebhookUrl
   */
  
  @Schema(name = "enquiryWebhookUrl", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("enquiryWebhookUrl")
  public @Nullable String getEnquiryWebhookUrl() {
    return enquiryWebhookUrl;
  }

  @JsonProperty("enquiryWebhookUrl")
  public void setEnquiryWebhookUrl(@Nullable String enquiryWebhookUrl) {
    this.enquiryWebhookUrl = enquiryWebhookUrl;
  }

  public ActionsConfigResponse enquiryEmail(@Nullable String enquiryEmail) {
    this.enquiryEmail = enquiryEmail;
    return this;
  }

  /**
   * Get enquiryEmail
   * @return enquiryEmail
   */
  
  @Schema(name = "enquiryEmail", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("enquiryEmail")
  public @Nullable String getEnquiryEmail() {
    return enquiryEmail;
  }

  @JsonProperty("enquiryEmail")
  public void setEnquiryEmail(@Nullable String enquiryEmail) {
    this.enquiryEmail = enquiryEmail;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ActionsConfigResponse actionsConfigResponse = (ActionsConfigResponse) o;
    return Objects.equals(this.actions, actionsConfigResponse.actions) &&
        Objects.equals(this.enquiryWebhookUrl, actionsConfigResponse.enquiryWebhookUrl) &&
        Objects.equals(this.enquiryEmail, actionsConfigResponse.enquiryEmail);
  }

  @Override
  public int hashCode() {
    return Objects.hash(actions, enquiryWebhookUrl, enquiryEmail);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ActionsConfigResponse {\n");
    sb.append("    actions: ").append(toIndentedString(actions)).append("\n");
    sb.append("    enquiryWebhookUrl: ").append(toIndentedString(enquiryWebhookUrl)).append("\n");
    sb.append("    enquiryEmail: ").append(toIndentedString(enquiryEmail)).append("\n");
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

