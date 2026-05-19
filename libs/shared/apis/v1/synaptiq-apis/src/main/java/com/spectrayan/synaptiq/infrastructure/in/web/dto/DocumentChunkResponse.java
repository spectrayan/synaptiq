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
 * DocumentChunkResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-18T21:07:32.055501800-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class DocumentChunkResponse {

  private String documentId;

  private String content;

  private Double similarityScore;

  public DocumentChunkResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public DocumentChunkResponse(String documentId, String content, Double similarityScore) {
    this.documentId = documentId;
    this.content = content;
    this.similarityScore = similarityScore;
  }

  public DocumentChunkResponse documentId(String documentId) {
    this.documentId = documentId;
    return this;
  }

  /**
   * Get documentId
   * @return documentId
   */
  @NotNull 
  @Schema(name = "documentId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("documentId")
  public String getDocumentId() {
    return documentId;
  }

  @JsonProperty("documentId")
  public void setDocumentId(String documentId) {
    this.documentId = documentId;
  }

  public DocumentChunkResponse content(String content) {
    this.content = content;
    return this;
  }

  /**
   * Get content
   * @return content
   */
  @NotNull 
  @Schema(name = "content", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("content")
  public String getContent() {
    return content;
  }

  @JsonProperty("content")
  public void setContent(String content) {
    this.content = content;
  }

  public DocumentChunkResponse similarityScore(Double similarityScore) {
    this.similarityScore = similarityScore;
    return this;
  }

  /**
   * Get similarityScore
   * @return similarityScore
   */
  @NotNull 
  @Schema(name = "similarityScore", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("similarityScore")
  public Double getSimilarityScore() {
    return similarityScore;
  }

  @JsonProperty("similarityScore")
  public void setSimilarityScore(Double similarityScore) {
    this.similarityScore = similarityScore;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DocumentChunkResponse documentChunkResponse = (DocumentChunkResponse) o;
    return Objects.equals(this.documentId, documentChunkResponse.documentId) &&
        Objects.equals(this.content, documentChunkResponse.content) &&
        Objects.equals(this.similarityScore, documentChunkResponse.similarityScore);
  }

  @Override
  public int hashCode() {
    return Objects.hash(documentId, content, similarityScore);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DocumentChunkResponse {\n");
    sb.append("    documentId: ").append(toIndentedString(documentId)).append("\n");
    sb.append("    content: ").append(toIndentedString(content)).append("\n");
    sb.append("    similarityScore: ").append(toIndentedString(similarityScore)).append("\n");
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

