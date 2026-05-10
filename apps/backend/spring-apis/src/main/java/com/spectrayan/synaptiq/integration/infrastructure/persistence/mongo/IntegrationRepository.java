package com.spectrayan.synaptiq.integration.infrastructure.persistence.mongo;

import com.spectrayan.synaptiq.integration.model.RouteStatus;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/**
 * Reactive MongoDB repository for integration documents.
 */
public interface IntegrationRepository extends ReactiveMongoRepository<IntegrationDocument, String> {

    Flux<IntegrationDocument> findByTenantId(String tenantId);

    Flux<IntegrationDocument> findByTenantIdAndStatus(String tenantId, RouteStatus status);

    Flux<IntegrationDocument> findByStatus(RouteStatus status);
}
