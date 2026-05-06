package com.synaptiq.shared.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Centralized, type-safe configuration properties for Synaptiq.
 * Maps to {@code synaptiq.*} in application.yml.
 */
@ConfigurationProperties(prefix = "synaptiq")
@Validated
@Getter
@Setter
public class SynaptiqProperties {

    /** Whether debug mode is active (enables Swagger UI, verbose logging). */
    private boolean debug = false;

    /** Deployment environment name. */
    private String environment = "development";

    /** CORS allowed origins. */
    private List<String> corsOrigins = List.of("http://localhost:4200");

    /** Base domain for tenant subdomains (e.g., spectrayan.com). */
    private String baseDomain = "spectrayan.com";

    /** MongoDB configuration. */
    private final Mongo mongo = new Mongo();

    /** Redis configuration. */
    private final Redis redis = new Redis();

    /** Auth configuration. */
    private final Auth auth = new Auth();

    /** LLM / AI configuration. */
    private final Llm llm = new Llm();

    @Getter
    @Setter
    public static class Mongo {
        @NotBlank
        private String uri = "mongodb://localhost:27017";
        @NotBlank
        private String database = "synaptiq";
    }

    @Getter
    @Setter
    public static class Redis {
        private String url = "redis://localhost:6379";
        private int chatSessionTtlSeconds = 7200;
    }

    @Getter
    @Setter
    public static class Auth {
        /** Auth provider: "builtin" (MongoDB + JWT) or "firebase". */
        private String provider = "builtin";
        /** HMAC secret for builtin JWT. */
        private String jwtSecret = "synaptiq-local-dev-secret-change-in-prod";
        /** JWT token lifetime in hours. */
        private int jwtExpiryHours = 24;

        // Firebase settings (used when provider=firebase)
        private String firebaseProjectId = "";
        private String firebaseServiceAccountJson = "";
        private String firebaseApiKey = "";
        private String firebaseAuthEmulatorHost = "";
    }

    @Getter
    @Setter
    public static class Llm {
        /** Default LLM provider: vertexai | openai | anthropic. */
        private String defaultProvider = "vertexai";
        private String vertexaiProject = "";
        private String vertexaiLocation = "us-central1";
        private String geminiApiKey = "";
        private String geminiModel = "gemini-2.5-flash";
        private String openaiApiKey = "";
        private String anthropicApiKey = "";
    }
}
