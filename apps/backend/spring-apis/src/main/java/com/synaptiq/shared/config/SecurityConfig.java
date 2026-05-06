package com.synaptiq.shared.config;

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
 * <p>Permits public access to health/actuator/swagger endpoints and tenant
 * public-branding routes. All other endpoints require authentication via
 * JWT bearer token (either Firebase or builtin).
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .cors(Customizer.withDefaults())
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                // Health / Actuator
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/health", "/health/**").permitAll()
                // Swagger / OpenAPI
                .pathMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/webjars/**").permitAll()
                // Public branding endpoint
                .pathMatchers(HttpMethod.GET, "/api/v1/config/branding/public").permitAll()
                // Auth endpoints
                .pathMatchers("/api/v1/auth/signup", "/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                // Shared workflow endpoint
                .pathMatchers(HttpMethod.GET, "/api/v1/workflow/shared/**").permitAll()
                // Everything else requires auth
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {})
            )
            .build();
    }
}
