package com.spectrayan.synaptiq.application.application.port.in;

import com.spectrayan.synaptiq.application.domain.model.Application;
import reactor.core.publisher.Mono;

/**
 * Inbound port for Application write operations (create, delete).
 */
public interface ApplicationCommandUseCase {

    Mono<Application> create(CreateApplicationCommand command);

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
