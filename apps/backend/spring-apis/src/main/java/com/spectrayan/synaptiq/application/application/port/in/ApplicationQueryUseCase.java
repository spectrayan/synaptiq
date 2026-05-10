package com.spectrayan.synaptiq.application.application.port.in;

import com.spectrayan.synaptiq.application.domain.model.Application;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Inbound port for Application read operations (get, list).
 */
public interface ApplicationQueryUseCase {

    Mono<Application> getByAppId(String appId);

    Flux<Application> listByTenantId(String tenantId);
}
