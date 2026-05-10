package com.spectrayan.synaptiq.auth.application.port.in;

import reactor.core.publisher.Mono;

/**
 * Inbound port for refreshing JWT tokens using a refresh token.
 */
public interface RefreshTokenUseCase {
    Mono<LoginUseCase.AuthToken> refresh(String refreshToken, String userId);
}
