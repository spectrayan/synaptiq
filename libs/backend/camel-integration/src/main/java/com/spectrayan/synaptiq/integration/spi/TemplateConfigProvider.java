package com.spectrayan.synaptiq.integration.spi;

import com.spectrayan.synaptiq.integration.model.TemplateDescriptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * SPI for persisting and retrieving integration templates.
 * <p>
 * The consuming application implements this backed by MongoDB (or any
 * other persistence layer) to enable runtime template management.
 * Templates stored here are loaded alongside the built-in Java DSL
 * templates to provide a unified catalog.
 */
public interface TemplateConfigProvider {

    /**
     * Find all global templates (tenantId is null) plus templates
     * scoped to the given tenant.
     */
    Flux<TemplateDescriptor> findAllAccessibleByTenant(String tenantId);

    /**
     * Find all templates regardless of tenant scope.
     */
    Flux<TemplateDescriptor> findAll();

    /**
     * Find a template by its unique ID.
     */
    Mono<TemplateDescriptor> findByTemplateId(String templateId);

    /**
     * Save a new or updated template.
     */
    Mono<TemplateDescriptor> save(TemplateDescriptor template);

    /**
     * Delete a template by ID. Built-in templates cannot be deleted.
     */
    Mono<Void> deleteByTemplateId(String templateId);
}
