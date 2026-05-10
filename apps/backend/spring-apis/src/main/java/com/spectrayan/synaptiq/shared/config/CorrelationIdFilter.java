package com.spectrayan.synaptiq.shared.config;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

/**
 * WebFilter that propagates a correlation/request ID through the reactive chain.
 * <p>
 * For each request:
 * <ol>
 *   <li>Reads or generates a unique {@code X-Request-ID} header</li>
 *   <li>Stores it in Reactor Context (available to downstream operators)</li>
 *   <li>Copies it to MDC for structured logging via logback-spring.xml</li>
 *   <li>Adds it to the response headers for client-side correlation</li>
 * </ol>
 *
 * Ordering: runs before TenantResolutionFilter to ensure correlation ID
 * is available from the very first log line.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter implements WebFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String MDC_REQUEST_ID = "requestId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Use incoming header or generate a new UUID
        String requestId = request.getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        }

        // Add to response headers for client-side correlation
        exchange.getResponse().getHeaders().set(REQUEST_ID_HEADER, requestId);

        final String finalRequestId = requestId;

        return chain.filter(exchange)
            .contextWrite(Context.of(MDC_REQUEST_ID, finalRequestId))
            .doFirst(() -> MDC.put(MDC_REQUEST_ID, finalRequestId))
            .doFinally(signal -> MDC.remove(MDC_REQUEST_ID));
    }
}
