package com.spectrayan.synaptiq.auth.application.service;

import com.spectrayan.synaptiq.auth.application.port.in.LoginUseCase;
import com.spectrayan.synaptiq.auth.application.port.out.UserPersistencePort;
import com.spectrayan.synaptiq.shared.config.SynaptiqProperties;
import com.spectrayan.synaptiq.shared.exception.ErrorCode;
import com.spectrayan.synaptiq.shared.exception.SynaptiqException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {
    private final UserPersistencePort userPersistence;
    private final SynaptiqProperties properties;
    private final BCryptPasswordEncoder encoder;

    @Override
    public Mono<AuthToken> login(LoginCommand cmd) {
        log.debug("Login attempt for email={}", cmd.email());
        return userPersistence.findByEmail(cmd.email())
            .switchIfEmpty(Mono.error(new SynaptiqException(ErrorCode.AUTHENTICATION_FAILED, "Invalid credentials")))
            .flatMap(user -> {
                if (!encoder.matches(cmd.password(), user.getPasswordHash())) {
                    log.warn("Failed login attempt for email={}", cmd.email());
                    return Mono.error(new SynaptiqException(ErrorCode.AUTHENTICATION_FAILED, "Invalid credentials"));
                }
                var key = Keys.hmacShaKeyFor(properties.getAuth().getJwtSecret().getBytes(StandardCharsets.UTF_8));
                int exp = properties.getAuth().getJwtExpiryHours() * 3600;
                String token = Jwts.builder().subject(user.getId())
                    .claim("email", user.getEmail()).claim("role", user.getRole())
                    .claim("tenantId", user.getTenantId())
                    .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + exp * 1000L))
                    .signWith(key).compact();
                String refreshToken = UUID.randomUUID().toString();
                log.info("Login successful for user={}, email={}", user.getId(), user.getEmail());
                return Mono.just(new AuthToken(token, refreshToken, exp));
            });
    }
}
