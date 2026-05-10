package com.spectrayan.synaptiq.auth.infrastructure.persistence.mongo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ScopeMongoRepository extends ReactiveMongoRepository<ScopeDocument, String> {
    Flux<ScopeDocument> findByResource(String resource);
    Mono<ScopeDocument> findBySlug(String slug);
}
