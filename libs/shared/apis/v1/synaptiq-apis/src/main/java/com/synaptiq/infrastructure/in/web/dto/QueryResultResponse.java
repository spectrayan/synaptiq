package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * QueryResultResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-05T21:15:25.464614100-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class QueryResultResponse {

  @Valid
  private List<Map<String, Object>> documents = new ArrayList<>();

  private @Nullable Integer total;

  public QueryResultResponse documents(List<Map<String, Object>> documents) {
    this.documents = documents;
    return this;
  }

  public QueryResultResponse addDocumentsItem(Map<String, Object> documentsItem) {
    if (this.documents == null) {
      this.documents = new ArrayList<>();
    }
    this.documents.add(documentsItem);
    return this;
  }

  /**
   * Get documents
   * @return documents
   */
  @Valid 
  @Schema(name = "documents", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("documents")
  public List<Map<String, Object>> getDocuments() {
    return documents;
  }

  @JsonProperty("documents")
  public void setDocuments(List<Map<String, Object>> documents) {
    this.documents = documents;
  }

  public QueryResultResponse total(@Nullable Integer total) {
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
    QueryResultResponse queryResultResponse = (QueryResultResponse) o;
    return Objects.equals(this.documents, queryResultResponse.documents) &&
        Objects.equals(this.total, queryResultResponse.total);
  }

  @Override
  public int hashCode() {
    return Objects.hash(documents, total);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class QueryResultResponse {\n");
    sb.append("    documents: ").append(toIndentedString(documents)).append("\n");
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

