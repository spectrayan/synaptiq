package com.synaptiq.shared.infrastructure.web;

import com.synaptiq.application.application.port.out.ApplicationPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Resolves the current Application ID from the request context.
 * <p>
 * Priority:
 * <ol>
 *   <li>{@code X-App-ID} header — explicit application targeting</li>
 *   <li>Tenant's default application — automatic fallback</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppIdResolver {

    public static final String APP_ID_HEADER = "X-App-ID";

    private final ApplicationPersistencePort applicationPersistence;

    /**
     * Resolves the appId from the exchange. If X-App-ID header is present,
     * uses it directly. Otherwise falls back to the tenant's default app.
     */
    public Mono<String> resolve(ServerWebExchange exchange, String tenantId) {
        String headerValue = exchange.getRequest().getHeaders().getFirst(APP_ID_HEADER);
        if (headerValue != null && !headerValue.isBlank()) {
            return Mono.just(headerValue);
        }
        return applicationPersistence.findDefaultByTenantId(tenantId)
            .map(app -> app.getAppId())
            .doOnNext(appId -> log.debug("Resolved default appId '{}' for tenant '{}'", appId, tenantId))
            .switchIfEmpty(Mono.defer(() -> {
                log.warn("No default application found for tenant '{}', using tenantId as fallback", tenantId);
                return Mono.just(tenantId);
            }));
    }
}
