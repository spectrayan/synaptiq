package com.synaptiq.shared.infrastructure.web;

import com.synaptiq.infrastructure.in.web.api.HealthApi;
import com.synaptiq.infrastructure.in.web.dto.HealthResponse;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

/**
 * Implements the generated HealthApi interface from OpenAPI spec.
 */
@RestController
public class HealthController implements HealthApi {

    private final ReactiveMongoTemplate mongoTemplate;

    public HealthController(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Mono<ResponseEntity<HealthResponse>> healthCheck(ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(new HealthResponse()
            .status("UP")
            .timestamp(OffsetDateTime.now())));
    }

    @Override
    public Mono<ResponseEntity<HealthResponse>> readinessCheck(ServerWebExchange exchange) {
        return mongoTemplate.executeCommand("{ping: 1}")
            .map(doc -> ResponseEntity.ok(new HealthResponse()
                .status("UP")
                .mongo("UP")
                .timestamp(OffsetDateTime.now())))
            .onErrorReturn(ResponseEntity.status(503).body(new HealthResponse()
                .status("DOWN")
                .mongo("DOWN")
                .timestamp(OffsetDateTime.now())));
    }
}
