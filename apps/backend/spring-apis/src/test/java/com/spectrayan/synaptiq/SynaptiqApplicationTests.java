package com.spectrayan.synaptiq;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Full integration test — requires Docker (MongoDB, Redis) and Vertex AI credentials.
 * Run locally with: mvn test -Dtest=SynaptiqApplicationTests -Dspring.profiles.active=test
 */
@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=" +
        "org.springframework.ai.model.vertexai.autoconfigure.gemini.VertexAiGeminiChatAutoConfiguration," +
        "org.springframework.ai.model.vertexai.autoconfigure.embedding.VertexAiEmbeddingAutoConfiguration"
})
@Testcontainers
@ActiveProfiles("test")
@Import(TestAiConfiguration.class)
@Disabled("Requires Docker + cloud credentials — run locally, not in CI")
class SynaptiqApplicationTests {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:6.0"));

    @Container
    @ServiceConnection(name = "redis")
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.0-alpine"))
            .withExposedPorts(6379);

    @Test
    void contextLoads() {
        // Verify the application context starts successfully
    }
}
