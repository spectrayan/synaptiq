package com.synaptiq.auth.application.service;

import com.synaptiq.auth.application.port.in.UserProfileUseCase;
import com.synaptiq.auth.application.port.out.UserPersistencePort;
import com.synaptiq.auth.domain.model.User;
import com.synaptiq.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserProfileService implements UserProfileUseCase {
    private final UserPersistencePort userPersistence;

    @Override
    public Mono<User> getCurrentUser(String uid) {
        return userPersistence.findById(uid)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")));
    }
}
