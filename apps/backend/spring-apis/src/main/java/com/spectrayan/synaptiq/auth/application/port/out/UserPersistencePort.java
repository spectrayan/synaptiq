package com.spectrayan.synaptiq.auth.application.port.out;

import com.spectrayan.synaptiq.auth.domain.model.User;
import reactor.core.publisher.Mono;

public interface UserPersistencePort {
    Mono<User> save(User user);
    Mono<User> findById(String id);
    Mono<User> findByEmail(String email);
}
