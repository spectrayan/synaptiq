package com.spectrayan.synaptiq.application.application.port.out;

import com.spectrayan.synaptiq.application.domain.model.Application;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Outbound port for Application persistence.
 * Returns domain models — never MongoDB documents.
 */
public interface ApplicationPersistencePort {

    Mono<Application> save(Application application);

    Mono<Application> findByAppId(String appId);

    Mono<Application> findDefaultByTenantId(String tenantId);

    Flux<Application> findAllByTenantId(String tenantId);

    Mono<Long> countByTenantId(String tenantId);

    Mono<Void> deleteByAppId(String appId);
}
