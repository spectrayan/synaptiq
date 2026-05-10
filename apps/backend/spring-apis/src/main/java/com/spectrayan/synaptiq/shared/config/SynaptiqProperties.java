package com.spectrayan.synaptiq.shared.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.validation.annotation.Validated;

import java.util.Arrays;
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

    private static final String DEFAULT_JWT_SECRET = "synaptiq-local-dev-secret-change-in-prod";

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

    /** Application-level rate limiting. */
    private final RateLimit rateLimit = new RateLimit();

    /** Cache configuration. */
    private final Cache cache = new Cache();

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
        private String jwtSecret = DEFAULT_JWT_SECRET;
        /** JWT token lifetime in hours. */
        private int jwtExpiryHours = 24;
        /** Refresh token lifetime in days. */
        private int refreshExpiryDays = 7;

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

    @Getter
    @Setter
    public static class RateLimit {
        /** Whether application-level rate limiting is enabled. */
        private boolean enabled = false;

        /** Global per-IP requests per second. */
        private int requestsPerSecond = 100;

        /** Stricter per-IP limit for login/signup endpoints (per minute). */
        private int loginRequestsPerMinute = 5;

        /** Wait time in ms for a rate-limit permit (0 = fail immediately). */
        private int timeout = 0;
    }

    @Getter
    @Setter
    public static class Cache {
        /** Cache type: "caffeine" (in-memory, default) or "redis". */
        private String type = "caffeine";
    }
}
