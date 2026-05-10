package com.spectrayan.synaptiq.auth.application.port.in;

import reactor.core.publisher.Mono;

public interface LoginUseCase {
    Mono<AuthToken> login(LoginCommand command);

    record LoginCommand(String email, String password) {}
    record AuthToken(String idToken, String refreshToken, int expiresIn) {}
}
