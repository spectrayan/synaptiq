package com.spectrayan.synaptiq.shared.infrastructure.config;

import com.mongodb.reactivestreams.client.MongoClient;
import com.spectrayan.synaptiq.shared.config.SynaptiqProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;

/**
 * Explicit MongoDB database factory configuration.
 *
 * <p>Spring Boot 4's auto-configuration does not reliably extract the database
 * name from the {@code spring.data.mongodb.uri} path, defaulting to {@code "test"}.
 * This bean overrides the auto-configured factory to ensure the correct database
 * is used, as specified by {@code synaptiq.mongo.database}.
 */
@Configuration
public class MongoDbConfig {

    @Bean
    ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory(
            MongoClient mongoClient,
            SynaptiqProperties properties) {
        return new SimpleReactiveMongoDatabaseFactory(mongoClient, properties.getMongo().getDatabase());
    }
}
