package com.spectrayan.synaptiq.integration.infrastructure.persistence.mongo;

import com.spectrayan.synaptiq.integration.model.RouteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * MongoDB document for integration route configurations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "integration_routes")
@CompoundIndex(name = "tenant_status_idx", def = "{'tenantId': 1, 'status': 1}")
public class IntegrationDocument {

    @Id
    private String id;
    private String tenantId;
    private String name;
    private String description;
    private String connectorType;
    private String templateId;
    @Builder.Default
    private Map<String, String> parameters = new HashMap<>();
    private String routeYaml;
    private String credentialRef;
    @Builder.Default
    private RouteStatus status = RouteStatus.PENDING;
    private String camelRouteId;
    private Instant lastTestedAt;
    private Instant lastExecutedAt;
    private String lastError;
    private Instant createdAt;
    private Instant updatedAt;
}
