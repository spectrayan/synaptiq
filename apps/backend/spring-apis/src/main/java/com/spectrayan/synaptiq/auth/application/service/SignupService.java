package com.spectrayan.synaptiq.auth.application.service;

import com.spectrayan.synaptiq.auth.application.port.in.SignupUseCase;
import com.spectrayan.synaptiq.auth.application.port.out.UserPersistencePort;
import com.spectrayan.synaptiq.auth.domain.model.User;
import com.spectrayan.synaptiq.shared.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupService implements SignupUseCase {
    private final UserPersistencePort userPersistence;
    private final BCryptPasswordEncoder encoder;

    @Override
    public Mono<User> signup(SignupCommand cmd) {
        log.debug("Signup attempt for email={}", cmd.email());
        return userPersistence.findByEmail(cmd.email())
            .flatMap(x -> Mono.<User>error(new DuplicateResourceException("Email already exists")))
            .switchIfEmpty(Mono.defer(() -> {
                var user = User.builder()
                    .email(cmd.email())
                    .passwordHash(encoder.encode(cmd.password()))
                    .build();
                log.info("User registered: email={}", cmd.email());
                return userPersistence.save(user);
            }));
    }
}
