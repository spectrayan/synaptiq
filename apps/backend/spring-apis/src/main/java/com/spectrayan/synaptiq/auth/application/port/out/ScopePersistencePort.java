package com.spectrayan.synaptiq.auth.application.port.out;

import com.spectrayan.synaptiq.auth.domain.model.Scope;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Outbound port for Scope persistence.
 */
public interface ScopePersistencePort {

    Flux<Scope> findAll();

    Flux<Scope> findByResource(String resource);

    Mono<Scope> findBySlug(String slug);

    Mono<Scope> save(Scope scope);

    Mono<Long> count();
}
