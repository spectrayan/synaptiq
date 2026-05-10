package com.spectrayan.synaptiq.auth.infrastructure.persistence.mongo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface PathScopeMappingMongoRepository extends ReactiveMongoRepository<PathScopeMappingDocument, String> {
    Mono<Long> countByHttpMethodIsNotNull();
}
