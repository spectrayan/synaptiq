package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.SessionSummaryResponse;
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
 * SessionListResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-18T21:07:32.055501800-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class SessionListResponse {

  @Valid
  private List<@Valid SessionSummaryResponse> sessions = new ArrayList<>();

  private @Nullable Long total;

  public SessionListResponse sessions(List<@Valid SessionSummaryResponse> sessions) {
    this.sessions = sessions;
    return this;
  }

  public SessionListResponse addSessionsItem(SessionSummaryResponse sessionsItem) {
    if (this.sessions == null) {
      this.sessions = new ArrayList<>();
    }
    this.sessions.add(sessionsItem);
    return this;
  }

  /**
   * Get sessions
   * @return sessions
   */
  @Valid 
  @Schema(name = "sessions", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("sessions")
  public List<@Valid SessionSummaryResponse> getSessions() {
    return sessions;
  }

  @JsonProperty("sessions")
  public void setSessions(List<@Valid SessionSummaryResponse> sessions) {
    this.sessions = sessions;
  }

  public SessionListResponse total(@Nullable Long total) {
    this.total = total;
    return this;
  }

  /**
   * Get total
   * @return total
   */
  
  @Schema(name = "total", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("total")
  public @Nullable Long getTotal() {
    return total;
  }

  @JsonProperty("total")
  public void setTotal(@Nullable Long total) {
    this.total = total;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SessionListResponse sessionListResponse = (SessionListResponse) o;
    return Objects.equals(this.sessions, sessionListResponse.sessions) &&
        Objects.equals(this.total, sessionListResponse.total);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sessions, total);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SessionListResponse {\n");
    sb.append("    sessions: ").append(toIndentedString(sessions)).append("\n");
    sb.append("    total: ").append(toIndentedString(total)).append("\n");
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

