package com.spectrayan.synaptiq.application.application.service;

import com.spectrayan.synaptiq.application.application.port.in.ApplicationQueryUseCase;
import com.spectrayan.synaptiq.application.application.port.out.ApplicationPersistencePort;
import com.spectrayan.synaptiq.application.domain.model.Application;
import com.spectrayan.synaptiq.shared.config.CacheNames;
import com.spectrayan.synaptiq.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Handles Application read operations (get, list).
 * Results are cached to reduce DB load.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationQueryService implements ApplicationQueryUseCase {

    private final ApplicationPersistencePort persistence;

    @Override
    @Cacheable(value = CacheNames.APPLICATIONS, key = "#appId")
    public Mono<Application> getByAppId(String appId) {
        return persistence.findByAppId(appId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                "Application '" + appId + "' not found")));
    }

    @Override
    @Cacheable(value = CacheNames.APPLICATIONS_BY_TENANT, key = "#tenantId")
    public Flux<Application> listByTenantId(String tenantId) {
        return persistence.findAllByTenantId(tenantId);
    }
}
