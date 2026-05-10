package com.spectrayan.synaptiq.integration.infrastructure.spi;

import com.spectrayan.synaptiq.integration.infrastructure.persistence.mongo.IntegrationDocument;
import com.spectrayan.synaptiq.integration.infrastructure.persistence.mongo.IntegrationRepository;
import com.spectrayan.synaptiq.integration.model.RouteConfig;
import com.spectrayan.synaptiq.integration.model.RouteStatus;
import com.spectrayan.synaptiq.integration.spi.RouteConfigProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * MongoDB-backed implementation of {@link RouteConfigProvider}.
 */
@Component
@RequiredArgsConstructor
public class MongoRouteConfigProvider implements RouteConfigProvider {

    private final IntegrationRepository repository;

    @Override
    public Mono<RouteConfig> findByRouteConfigId(String routeConfigId) {
        return repository.findById(routeConfigId).map(this::toModel);
    }

    @Override
    public Flux<RouteConfig> findActiveByTenantId(String tenantId) {
        return repository.findByTenantIdAndStatus(tenantId, RouteStatus.ACTIVE)
                .map(this::toModel);
    }

    @Override
    public Flux<RouteConfig> findAllByTenantId(String tenantId) {
        return repository.findByTenantId(tenantId).map(this::toModel);
    }

    @Override
    public Flux<RouteConfig> findAllActive() {
        return repository.findByStatus(RouteStatus.ACTIVE).map(this::toModel);
    }

    @Override
    public Mono<RouteConfig> save(RouteConfig config) {
        IntegrationDocument doc = toDocument(config);
        doc.setUpdatedAt(Instant.now());
        if (doc.getCreatedAt() == null) {
            doc.setCreatedAt(Instant.now());
        }
        return repository.save(doc).map(this::toModel);
    }

    @Override
    public Mono<Void> deleteByRouteConfigId(String routeConfigId) {
        return repository.deleteById(routeConfigId);
    }

    @Override
    public Mono<RouteConfig> updateStatus(String routeConfigId, RouteStatus status, String errorMessage) {
        return repository.findById(routeConfigId)
                .flatMap(doc -> {
                    doc.setStatus(status);
                    doc.setLastError(errorMessage);
                    doc.setUpdatedAt(Instant.now());
                    return repository.save(doc);
                })
                .map(this::toModel);
    }

    // ── Mapping ─────────────────────────────────────────────────

    private RouteConfig toModel(IntegrationDocument doc) {
        return RouteConfig.builder()
                .routeConfigId(doc.getId())
                .tenantId(doc.getTenantId())
                .name(doc.getName())
                .description(doc.getDescription())
                .connectorType(doc.getConnectorType())
                .templateId(doc.getTemplateId())
                .parameters(doc.getParameters())
                .routeYaml(doc.getRouteYaml())
                .credentialRef(doc.getCredentialRef())
                .status(doc.getStatus())
                .camelRouteId(doc.getCamelRouteId())
                .lastTestedAt(doc.getLastTestedAt())
                .lastExecutedAt(doc.getLastExecutedAt())
                .lastError(doc.getLastError())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }

    private IntegrationDocument toDocument(RouteConfig config) {
        return IntegrationDocument.builder()
                .id(config.getRouteConfigId())
                .tenantId(config.getTenantId())
                .name(config.getName())
                .description(config.getDescription())
                .connectorType(config.getConnectorType())
                .templateId(config.getTemplateId())
                .parameters(config.getParameters())
                .routeYaml(config.getRouteYaml())
                .credentialRef(config.getCredentialRef())
                .status(config.getStatus())
                .camelRouteId(config.getCamelRouteId())
                .lastTestedAt(config.getLastTestedAt())
                .lastExecutedAt(config.getLastExecutedAt())
                .lastError(config.getLastError())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
