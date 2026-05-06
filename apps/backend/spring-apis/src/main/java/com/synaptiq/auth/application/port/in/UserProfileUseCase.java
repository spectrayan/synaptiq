package com.synaptiq.auth.application.port.in;

import com.synaptiq.auth.domain.model.User;
import reactor.core.publisher.Mono;

public interface UserProfileUseCase {
    Mono<User> getCurrentUser(String uid);
}
