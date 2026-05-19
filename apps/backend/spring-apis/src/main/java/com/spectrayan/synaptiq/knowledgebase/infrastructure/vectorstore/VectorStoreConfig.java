package com.spectrayan.synaptiq.knowledgebase.infrastructure.vectorstore;

import com.mongodb.client.MongoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Configures the blocking MongoTemplate used by MongoDBAtlasVectorStore.
 * 
 * <p>In a reactive Spring Boot app, only ReactiveMongoTemplate is auto-configured.
 * The MongoDBAtlasVectorStore auto-config creates a blocking MongoTemplate but defaults
 * to the 'test' database unless explicitly configured. This bean ensures it targets
 * the same database as the reactive layer.</p>
 */
@Configuration
public class VectorStoreConfig {

    @Bean
    @Primary
    public MongoTemplate mongoTemplate(MongoClient mongoClient,
                                       @Value("${spring.data.mongodb.database:synaptiq}") String databaseName) {
        return new MongoTemplate(mongoClient, databaseName);
    }
}
