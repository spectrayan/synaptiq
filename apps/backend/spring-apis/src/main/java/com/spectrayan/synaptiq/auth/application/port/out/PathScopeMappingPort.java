package com.spectrayan.synaptiq.auth.application.port.out;

import com.spectrayan.synaptiq.auth.domain.model.PathScopeMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Outbound port for PathScopeMapping persistence.
 */
public interface PathScopeMappingPort {

    Flux<PathScopeMapping> findAll();

    Mono<PathScopeMapping> save(PathScopeMapping mapping);

    Mono<Long> count();
}
