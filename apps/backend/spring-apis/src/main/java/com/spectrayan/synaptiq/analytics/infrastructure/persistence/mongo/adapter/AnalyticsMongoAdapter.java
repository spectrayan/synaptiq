package com.spectrayan.synaptiq.analytics.infrastructure.persistence.mongo.adapter;

import com.spectrayan.synaptiq.analytics.application.port.out.AnalyticsPersistencePort;
import com.spectrayan.synaptiq.analytics.domain.model.AnalyticsResult;
import com.spectrayan.synaptiq.analytics.domain.model.TokenUsageRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Component
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class AnalyticsMongoAdapter implements AnalyticsPersistencePort {

    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public Mono<TokenUsageRecord> saveTokenUsage(TokenUsageRecord record) {
        return mongoTemplate.save(record);
    }

    @Override
    public Mono<AnalyticsResult.Summary> getSummary(String tenantId, Instant from, Instant to) {
        Criteria criteria = Criteria.where("tenantId").is(tenantId).and("timestamp").gte(from).lte(to);
        Aggregation tokenAgg = newAggregation(
            match(criteria),
            group().sum("inputTokens").as("totalInput").sum("outputTokens").as("totalOutput").count().as("sessions")
        );

        return mongoTemplate.aggregate(tokenAgg, "token_usages", Map.class)
            .next()
            .map(tokens -> new AnalyticsResult.Summary(
                toLong(tokens, "sessions"),
                toLong(tokens, "totalInput") + toLong(tokens, "totalOutput"),
                (toLong(tokens, "totalInput") * 0.0001) + (toLong(tokens, "totalOutput") * 0.0002),
                0L
            ))
            .defaultIfEmpty(new AnalyticsResult.Summary(0, 0, 0.0, 0));
    }

    @Override
    public Mono<AnalyticsResult.TokenUsage> getTokenUsage(String tenantId, Instant from, Instant to) {
        Criteria criteria = Criteria.where("tenantId").is(tenantId).and("timestamp").gte(from).lte(to);
        Aggregation agg = newAggregation(
            match(criteria),
            group().sum("inputTokens").as("in").sum("outputTokens").as("out")
        );

        return mongoTemplate.aggregate(agg, "token_usages", Map.class)
            .next()
            .map(res -> new AnalyticsResult.TokenUsage(
                toLong(res, "in"),
                toLong(res, "out"),
                toLong(res, "in") + toLong(res, "out"),
                Map.of()
            ))
            .defaultIfEmpty(new AnalyticsResult.TokenUsage(0, 0, 0, Map.of()));
    }

    @Override
    public Mono<AnalyticsResult.Billing> getBilling(String tenantId, Instant from, Instant to) {
        Criteria criteria = Criteria.where("tenantId").is(tenantId).and("timestamp").gte(from).lte(to);
        Aggregation agg = newAggregation(
            match(criteria),
            group().sum("inputTokens").as("in").sum("outputTokens").as("out")
        );

        return mongoTemplate.aggregate(agg, "token_usages", Map.class)
            .next()
            .map(res -> {
                long total = toLong(res, "in") + toLong(res, "out");
                double cost = total * 0.00015;
                return new AnalyticsResult.Billing(cost, total, 0.00015, Map.of("vertex-gemini", cost));
            })
            .defaultIfEmpty(new AnalyticsResult.Billing(0.0, 0, 0.00015, Map.of()));
    }

    @Override
    public Mono<AnalyticsResult.PlatformRollup> getPlatformRollup(Instant from, Instant to) {
        Criteria criteria = Criteria.where("timestamp").gte(from).lte(to);
        Aggregation agg = newAggregation(
            match(criteria),
            group().sum("inputTokens").as("in").sum("outputTokens").as("out")
        );

        return mongoTemplate.aggregate(agg, "token_usages", Map.class)
            .next()
            .map(res -> {
                long total = toLong(res, "in") + toLong(res, "out");
                return new AnalyticsResult.PlatformRollup(1, 0, total, total * 0.00015, Map.of());
            })
            .defaultIfEmpty(new AnalyticsResult.PlatformRollup(0, 0, 0, 0.0, Map.of()));
    }

    private long toLong(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v instanceof Number n ? n.longValue() : 0L;
    }
}
