package com.synaptiq.catalog.domain.model;

import com.synaptiq.shared.domain.AggregateRoot;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class CatalogItem extends AggregateRoot {
    private String tenantId;
    @Builder.Default private String status = "draft";
    private Map<String, Object> data;
    private List<Double> embedding;
    @Builder.Default private String embeddingModel = "";
    private Instant embeddedAt;
}
