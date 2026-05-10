package com.spectrayan.synaptiq.auth.application.port.in;

import com.spectrayan.synaptiq.auth.domain.model.User;
import reactor.core.publisher.Mono;

public interface SignupUseCase {
    Mono<User> signup(SignupCommand command);
    record SignupCommand(String email, String password) {}
}
