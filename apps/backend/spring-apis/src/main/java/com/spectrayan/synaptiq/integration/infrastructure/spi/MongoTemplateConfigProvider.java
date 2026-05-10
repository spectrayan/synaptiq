package com.spectrayan.synaptiq.integration.infrastructure.spi;

import com.spectrayan.synaptiq.integration.infrastructure.persistence.mongo.TemplateDocument;
import com.spectrayan.synaptiq.integration.infrastructure.persistence.mongo.TemplateRepository;
import com.spectrayan.synaptiq.integration.model.TemplateDescriptor;
import com.spectrayan.synaptiq.integration.spi.TemplateConfigProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * MongoDB-backed implementation of {@link TemplateConfigProvider}.
 * <p>
 * Enables runtime creation and management of custom integration templates
 * without code changes. Admins and tenants can define Camel YAML-based
 * templates via the API, which are persisted here and loaded dynamically.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MongoTemplateConfigProvider implements TemplateConfigProvider {

    private final TemplateRepository repository;

    @Override
    public Flux<TemplateDescriptor> findAllAccessibleByTenant(String tenantId) {
        return Flux.merge(
                repository.findByTenantIdIsNull(),
                repository.findByTenantId(tenantId)
        ).map(this::toModel);
    }

    @Override
    public Flux<TemplateDescriptor> findAll() {
        return repository.findAll().map(this::toModel);
    }

    @Override
    public Mono<TemplateDescriptor> findByTemplateId(String templateId) {
        return repository.findByTemplateId(templateId).map(this::toModel);
    }

    @Override
    public Mono<TemplateDescriptor> save(TemplateDescriptor template) {
        TemplateDocument doc = toDocument(template);
        doc.setUpdatedAt(Instant.now());
        if (doc.getCreatedAt() == null) {
            doc.setCreatedAt(Instant.now());
        }
        return repository.save(doc).map(this::toModel);
    }

    @Override
    public Mono<Void> deleteByTemplateId(String templateId) {
        return repository.deleteByTemplateId(templateId);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Mapping
    // ═══════════════════════════════════════════════════════════════

    private TemplateDescriptor toModel(TemplateDocument doc) {
        return TemplateDescriptor.builder()
                .templateId(doc.getTemplateId())
                .displayName(doc.getDisplayName())
                .description(doc.getDescription())
                .icon(doc.getIcon())
                .category(doc.getCategory())
                .connectorType(doc.getConnectorType())
                .parameters(doc.getParameters())
                .requiresCredential(doc.isRequiresCredential())
                .builtIn(doc.isBuiltIn())
                .tenantId(doc.getTenantId())
                .routeYaml(doc.getRouteYaml())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }

    private TemplateDocument toDocument(TemplateDescriptor model) {
        return TemplateDocument.builder()
                .templateId(model.getTemplateId())
                .displayName(model.getDisplayName())
                .description(model.getDescription())
                .icon(model.getIcon())
                .category(model.getCategory())
                .connectorType(model.getConnectorType())
                .parameters(model.getParameters())
                .requiresCredential(model.isRequiresCredential())
                .builtIn(model.isBuiltIn())
                .tenantId(model.getTenantId())
                .routeYaml(model.getRouteYaml())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .build();
    }
}
