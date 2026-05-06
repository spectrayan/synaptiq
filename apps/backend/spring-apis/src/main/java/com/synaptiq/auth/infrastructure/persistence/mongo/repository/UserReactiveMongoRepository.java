package com.synaptiq.auth.infrastructure.persistence.mongo.repository;

import com.synaptiq.auth.infrastructure.persistence.mongo.entity.UserDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UserReactiveMongoRepository extends ReactiveMongoRepository<UserDocument, String> {
    Mono<UserDocument> findByEmail(String email);
}
