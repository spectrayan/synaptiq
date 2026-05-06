package com.synaptiq.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Tenant resolution WebFilter — mirrors the Python TenantMiddleware.
 *
 * <p>Resolution order:
 * <ol>
 *   <li>{@code X-Tenant-ID} header</li>
 *   <li>{@code tenant_id} query parameter</li>
 *   <li>Subdomain extraction (e.g., {@code acme.spectrayan.com} → {@code acme})</li>
 * </ol>
 *
 * Skips: actuator, swagger, auth, and public endpoints.
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class TenantResolutionFilter implements WebFilter {

    private final SynaptiqProperties properties;

    private static final Set<String> SKIP_PREFIXES = Set.of(
        "/actuator", "/health", "/swagger-ui", "/v3/api-docs", "/webjars",
        "/api/v1/auth/signup", "/api/v1/auth/login", "/api/v1/auth/refresh"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Skip paths that don't need tenant context
        if (SKIP_PREFIXES.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }
        // Shared workflow endpoint is public
        if (path.startsWith("/api/v1/workflow/shared/")) {
            return chain.filter(exchange);
        }

        String tenantId = resolveTenantId(exchange.getRequest());
        if (tenantId == null || tenantId.isBlank()) {
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            return exchange.getResponse().setComplete();
        }

        // Add tenant ID as request header for downstream controllers
        ServerHttpRequest mutated = exchange.getRequest().mutate()
            .header("X-Tenant-ID", tenantId)
            .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private String resolveTenantId(ServerHttpRequest request) {
        // 1. X-Tenant-ID header
        String header = request.getHeaders().getFirst("X-Tenant-ID");
        if (header != null && !header.isBlank()) return header;

        // 2. Query parameter
        String queryParam = request.getQueryParams().getFirst("tenant_id");
        if (queryParam != null && !queryParam.isBlank()) return queryParam;

        // 3. Subdomain extraction
        String host = request.getHeaders().getFirst("Host");
        if (host != null) {
            String baseDomain = properties.getBaseDomain();
            if (host.endsWith("." + baseDomain)) {
                String subdomain = host.replace("." + baseDomain, "").split(":")[0];
                if (!subdomain.isBlank() && !"www".equals(subdomain)) return subdomain;
            }
        }

        // 4. Default for development
        if (properties.isDebug()) return "demo";

        return null;
    }
}
