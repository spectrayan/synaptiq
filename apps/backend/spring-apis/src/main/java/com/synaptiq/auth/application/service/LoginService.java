package com.synaptiq.auth.application.service;

import com.synaptiq.auth.application.port.in.LoginUseCase;
import com.synaptiq.auth.application.port.out.UserPersistencePort;
import com.synaptiq.shared.config.SynaptiqProperties;
import com.synaptiq.shared.exception.ErrorCode;
import com.synaptiq.shared.exception.SynaptiqException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {
    private final UserPersistencePort userPersistence;
    private final SynaptiqProperties properties;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public Mono<AuthToken> login(LoginCommand cmd) {
        return userPersistence.findByEmail(cmd.email())
            .switchIfEmpty(Mono.error(new SynaptiqException(ErrorCode.AUTHENTICATION_FAILED, "Invalid credentials")))
            .flatMap(user -> {
                if (!encoder.matches(cmd.password(), user.getPasswordHash()))
                    return Mono.error(new SynaptiqException(ErrorCode.AUTHENTICATION_FAILED, "Invalid credentials"));
                var key = Keys.hmacShaKeyFor(properties.getAuth().getJwtSecret().getBytes(StandardCharsets.UTF_8));
                int exp = properties.getAuth().getJwtExpiryHours() * 3600;
                String token = Jwts.builder().subject(user.getId())
                    .claim("email", user.getEmail()).claim("role", user.getRole())
                    .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + exp * 1000L))
                    .signWith(key).compact();
                return Mono.just(new AuthToken(token, UUID.randomUUID().toString(), exp));
            });
    }
}
