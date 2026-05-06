package com.synaptiq.auth.application.service;

import com.synaptiq.auth.application.port.in.SignupUseCase;
import com.synaptiq.auth.application.port.out.UserPersistencePort;
import com.synaptiq.auth.domain.model.User;
import com.synaptiq.shared.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SignupService implements SignupUseCase {
    private final UserPersistencePort userPersistence;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public Mono<User> signup(SignupCommand cmd) {
        return userPersistence.findByEmail(cmd.email())
            .flatMap(x -> Mono.<User>error(new DuplicateResourceException("Email already exists")))
            .switchIfEmpty(Mono.defer(() -> {
                var user = User.builder().email(cmd.email()).passwordHash(encoder.encode(cmd.password())).build();
                return userPersistence.save(user);
            }));
    }
}
