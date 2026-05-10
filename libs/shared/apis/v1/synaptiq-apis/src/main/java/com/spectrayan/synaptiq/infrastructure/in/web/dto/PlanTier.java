package com.spectrayan.synaptiq.infrastructure.in.web.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Subscription plan tier
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-05-10T17:15:52.297398600-05:00[America/Chicago]", comments = "Generator version: 7.21.0")
public enum PlanTier {
  
  FREE("FREE"),
  
  GROWTH("GROWTH"),
  
  ENTERPRISE("ENTERPRISE");

  private final String value;

  PlanTier(String value) {
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
  public static PlanTier fromValue(String value) {
    for (PlanTier b : PlanTier.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

