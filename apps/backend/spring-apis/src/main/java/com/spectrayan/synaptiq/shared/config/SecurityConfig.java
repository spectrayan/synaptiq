package com.spectrayan.synaptiq.shared.config;

import com.spectrayan.synaptiq.auth.infrastructure.security.JwtToScopeConverter;
import com.spectrayan.synaptiq.auth.infrastructure.security.ScopeAuthorizationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * WebFlux security configuration.
 *
 * <p>Authentication: JWT bearer tokens validated by Spring Security OAuth2
 * Resource Server. The {@link JwtToScopeConverter} resolves the JWT
 * {@code role} claim to scope-based authorities from MongoDB (cached).
 *
 * <p>Authorization: The {@link ScopeAuthorizationManager} (implementing
 * Spring's native {@code ReactiveAuthorizationManager}) loads path→scope
 * mappings from MongoDB and checks each request against the user's
 * authorities. No {@code @PreAuthorize} annotations needed.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            JwtToScopeConverter jwtToScopeConverter,
            ScopeAuthorizationManager scopeAuthorizationManager) {
        return http
            .cors(Customizer.withDefaults())
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                // ── Public endpoints (no auth required) ──────────────
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/health", "/health/**").permitAll()
                .pathMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/webjars/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/v1/config/branding/public").permitAll()
                .pathMatchers("/api/v1/auth/signup", "/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/v1/workflow/shared/**").permitAll()

                // ── All API paths — DB-driven scope authorization ────
                .pathMatchers("/api/v1/**").access(scopeAuthorizationManager)

                // ── Fallback — require authentication ────────────────
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtToScopeConverter))
            )
            .build();
    }
}
