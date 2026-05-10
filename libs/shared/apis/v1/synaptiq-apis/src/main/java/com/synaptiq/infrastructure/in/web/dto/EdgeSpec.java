package com.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.lang.Nullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * Edge connecting two agent nodes
 */

@Schema(name = "EdgeSpec", description = "Edge connecting two agent nodes")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-08T22:14:16.718368-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public class EdgeSpec {

  private String source;

  private String target;

  /**
   * Gets or Sets condition
   */
  public enum ConditionEnum {
    ALWAYS("ALWAYS"),
    
    ON_TOOL_CALLS("ON_TOOL_CALLS"),
    
    NEVER("NEVER");

    private final String value;

    ConditionEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static ConditionEnum fromValue(String value) {
      for (ConditionEnum b : ConditionEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private ConditionEnum condition = ConditionEnum.ALWAYS;

  private @Nullable String label;

  public EdgeSpec() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public EdgeSpec(String source, String target) {
    this.source = source;
    this.target = target;
  }

  public EdgeSpec source(String source) {
    this.source = source;
    return this;
  }

  /**
   * Get source
   * @return source
   */
  @NotNull 
  @Schema(name = "source", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("source")
  public String getSource() {
    return source;
  }

  @JsonProperty("source")
  public void setSource(String source) {
    this.source = source;
  }

  public EdgeSpec target(String target) {
    this.target = target;
    return this;
  }

  /**
   * Get target
   * @return target
   */
  @NotNull 
  @Schema(name = "target", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("target")
  public String getTarget() {
    return target;
  }

  @JsonProperty("target")
  public void setTarget(String target) {
    this.target = target;
  }

  public EdgeSpec condition(ConditionEnum condition) {
    this.condition = condition;
    return this;
  }

  /**
   * Get condition
   * @return condition
   */
  
  @Schema(name = "condition", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("condition")
  public ConditionEnum getCondition() {
    return condition;
  }

  @JsonProperty("condition")
  public void setCondition(ConditionEnum condition) {
    this.condition = condition;
  }

  public EdgeSpec label(@Nullable String label) {
    this.label = label;
    return this;
  }

  /**
   * Get label
   * @return label
   */
  
  @Schema(name = "label", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("label")
  public @Nullable String getLabel() {
    return label;
  }

  @JsonProperty("label")
  public void setLabel(@Nullable String label) {
    this.label = label;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EdgeSpec edgeSpec = (EdgeSpec) o;
    return Objects.equals(this.source, edgeSpec.source) &&
        Objects.equals(this.target, edgeSpec.target) &&
        Objects.equals(this.condition, edgeSpec.condition) &&
        Objects.equals(this.label, edgeSpec.label);
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, target, condition, label);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EdgeSpec {\n");
    sb.append("    source: ").append(toIndentedString(source)).append("\n");
    sb.append("    target: ").append(toIndentedString(target)).append("\n");
    sb.append("    condition: ").append(toIndentedString(condition)).append("\n");
    sb.append("    label: ").append(toIndentedString(label)).append("\n");
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

