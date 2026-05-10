package com.spectrayan.synaptiq.shared.config;

import io.jsonwebtoken.security.Keys;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Provides the ReactiveJwtDecoder for builtin auth mode.
 * Uses the same HMAC-SHA secret key that LoginService uses to sign tokens.
 */
@Configuration
@ConditionalOnProperty(name = "synaptiq.auth.provider", havingValue = "builtin", matchIfMissing = true)
public class JwtDecoderConfig {

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(SynaptiqProperties properties) {
        byte[] keyBytes = properties.getAuth().getJwtSecret().getBytes(StandardCharsets.UTF_8);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        return NimbusReactiveJwtDecoder
            .withSecretKey(key)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
    }
}
