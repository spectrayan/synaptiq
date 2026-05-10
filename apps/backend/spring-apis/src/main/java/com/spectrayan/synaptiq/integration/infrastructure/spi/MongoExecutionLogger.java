package com.spectrayan.synaptiq.integration.infrastructure.spi;

import com.spectrayan.synaptiq.integration.model.ExecutionResult;
import com.spectrayan.synaptiq.integration.spi.ExecutionLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * MongoDB-backed execution logger.
 * <p>
 * Persists every execution result to the {@code integration_executions} collection
 * for audit and analytics.
 */
@Slf4j
@Component
public class MongoExecutionLogger implements ExecutionLogger {

    private static final String COLLECTION = "integration_executions";
    private final ReactiveMongoTemplate mongoTemplate;

    public MongoExecutionLogger(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Mono<Void> log(ExecutionResult result) {
        return mongoTemplate.insert(result, COLLECTION)
                .doOnSuccess(r -> log.debug("Logged execution: {} status={} duration={}ms",
                        r.getRouteConfigId(), r.getStatus(), r.getDurationMs()))
                .then();
    }
}
