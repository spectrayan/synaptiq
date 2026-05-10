package com.spectrayan.synaptiq.auth.application.service;

import com.spectrayan.synaptiq.auth.application.port.in.LoginUseCase;
import com.spectrayan.synaptiq.auth.application.port.in.RefreshTokenUseCase;
import com.spectrayan.synaptiq.auth.application.port.out.UserPersistencePort;
import com.spectrayan.synaptiq.shared.config.SynaptiqProperties;
import com.spectrayan.synaptiq.shared.exception.ErrorCode;
import com.spectrayan.synaptiq.shared.exception.SynaptiqException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Refresh token service — issues a new JWT + refresh token pair.
 * <p>
 * Currently uses a simple approach: validates the user still exists and is active,
 * then issues a fresh token pair. A production implementation should store and
 * validate refresh tokens in Redis with rotation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenUseCase {
    private final UserPersistencePort userPersistence;
    private final SynaptiqProperties properties;

    @Override
    public Mono<LoginUseCase.AuthToken> refresh(String refreshToken, String userId) {
        if (refreshToken == null || refreshToken.isBlank() || userId == null || userId.isBlank()) {
            return Mono.error(new SynaptiqException(ErrorCode.AUTHENTICATION_FAILED, "Invalid refresh request"));
        }

        return userPersistence.findById(userId)
            .switchIfEmpty(Mono.error(new SynaptiqException(ErrorCode.AUTHENTICATION_FAILED, "User not found")))
            .flatMap(user -> {
                if (user.isDisabled()) {
                    return Mono.error(new SynaptiqException(ErrorCode.AUTHENTICATION_FAILED, "Account is disabled"));
                }
                var key = Keys.hmacShaKeyFor(properties.getAuth().getJwtSecret().getBytes(StandardCharsets.UTF_8));
                int exp = properties.getAuth().getJwtExpiryHours() * 3600;
                String token = Jwts.builder().subject(user.getId())
                    .claim("email", user.getEmail()).claim("role", user.getRole())
                    .claim("tenantId", user.getTenantId())
                    .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + exp * 1000L))
                    .signWith(key).compact();
                String newRefreshToken = UUID.randomUUID().toString();
                log.info("Token refreshed for user={}", userId);
                return Mono.just(new LoginUseCase.AuthToken(token, newRefreshToken, exp));
            });
    }
}
