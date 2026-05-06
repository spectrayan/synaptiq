package com.synaptiq.application.application.port.in;

import com.synaptiq.application.domain.model.Application;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Inbound port for Application CRUD operations.
 */
public interface ApplicationCrudUseCase {

    Mono<Application> create(CreateApplicationCommand command);

    Mono<Application> getByAppId(String appId);

    Flux<Application> listByTenantId(String tenantId);

    Mono<Void> delete(String appId);

    record CreateApplicationCommand(
        String tenantId,
        String name,
        String slug,
        String description,
        String icon,
        boolean isDefault
    ) {}
}
