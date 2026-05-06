package com.synaptiq.auth.infrastructure.persistence.mongo.repository;

import com.synaptiq.auth.application.port.out.UserPersistencePort;
import com.synaptiq.auth.domain.model.User;
import com.synaptiq.auth.infrastructure.persistence.mongo.mapper.UserPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserMongoAdapter implements UserPersistencePort {
    private final UserReactiveMongoRepository mongoRepository;
    private final UserPersistenceMapper mapper;

    @Override
    public Mono<User> save(User user) {
        return mongoRepository.save(mapper.toDocument(user)).map(mapper::toDomain);
    }

    @Override
    public Mono<User> findById(String id) {
        return mongoRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return mongoRepository.findByEmail(email).map(mapper::toDomain);
    }
}
