package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.synaptiq.infrastructure.in.web.dto.ConversationTurnResponse;
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
 * SessionHistoryResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-05T21:15:25.464614100-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class SessionHistoryResponse {

  private @Nullable String sessionId;

  @Valid
  private List<@Valid ConversationTurnResponse> turns = new ArrayList<>();

  private @Nullable Integer total;

  public SessionHistoryResponse sessionId(@Nullable String sessionId) {
    this.sessionId = sessionId;
    return this;
  }

  /**
   * Get sessionId
   * @return sessionId
   */
  
  @Schema(name = "sessionId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("sessionId")
  public @Nullable String getSessionId() {
    return sessionId;
  }

  @JsonProperty("sessionId")
  public void setSessionId(@Nullable String sessionId) {
    this.sessionId = sessionId;
  }

  public SessionHistoryResponse turns(List<@Valid ConversationTurnResponse> turns) {
    this.turns = turns;
    return this;
  }

  public SessionHistoryResponse addTurnsItem(ConversationTurnResponse turnsItem) {
    if (this.turns == null) {
      this.turns = new ArrayList<>();
    }
    this.turns.add(turnsItem);
    return this;
  }

  /**
   * Get turns
   * @return turns
   */
  @Valid 
  @Schema(name = "turns", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("turns")
  public List<@Valid ConversationTurnResponse> getTurns() {
    return turns;
  }

  @JsonProperty("turns")
  public void setTurns(List<@Valid ConversationTurnResponse> turns) {
    this.turns = turns;
  }

  public SessionHistoryResponse total(@Nullable Integer total) {
    this.total = total;
    return this;
  }

  /**
   * Get total
   * @return total
   */
  
  @Schema(name = "total", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("total")
  public @Nullable Integer getTotal() {
    return total;
  }

  @JsonProperty("total")
  public void setTotal(@Nullable Integer total) {
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
    SessionHistoryResponse sessionHistoryResponse = (SessionHistoryResponse) o;
    return Objects.equals(this.sessionId, sessionHistoryResponse.sessionId) &&
        Objects.equals(this.turns, sessionHistoryResponse.turns) &&
        Objects.equals(this.total, sessionHistoryResponse.total);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sessionId, turns, total);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SessionHistoryResponse {\n");
    sb.append("    sessionId: ").append(toIndentedString(sessionId)).append("\n");
    sb.append("    turns: ").append(toIndentedString(turns)).append("\n");
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

