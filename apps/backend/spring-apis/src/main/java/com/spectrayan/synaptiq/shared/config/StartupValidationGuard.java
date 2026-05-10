package com.spectrayan.synaptiq.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Startup validation guard — prevents production launch with unsafe defaults.
 * <p>
 * Checks:
 * <ul>
 *   <li>JWT secret must NOT be the dev default in production profiles</li>
 *   <li>LLM API key must be set when debug is false</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartupValidationGuard {

    private static final String DEFAULT_JWT_SECRET = "synaptiq-local-dev-secret-change-in-prod";

    private final SynaptiqProperties properties;
    private final Environment env;

    @EventListener(ApplicationReadyEvent.class)
    public void validateConfiguration() {
        var activeProfiles = Arrays.asList(env.getActiveProfiles());
        boolean isProd = activeProfiles.contains("prod") || activeProfiles.contains("production");
        boolean isDev = activeProfiles.isEmpty()
                || activeProfiles.contains("dev")
                || activeProfiles.contains("local");

        // ── JWT Secret Guard ──
        if (DEFAULT_JWT_SECRET.equals(properties.getAuth().getJwtSecret())) {
            if (isProd) {
                log.error("╔══════════════════════════════════════════════════════════════╗");
                log.error("║  FATAL: Default JWT secret detected in PRODUCTION profile!  ║");
                log.error("║  Set JWT_SECRET environment variable before deploying.       ║");
                log.error("╚══════════════════════════════════════════════════════════════╝");
                throw new IllegalStateException(
                    "Application cannot start in production with the default JWT secret. " +
                    "Set the JWT_SECRET environment variable.");
            } else if (!isDev) {
                log.warn("⚠️  Using default JWT secret — acceptable for development only. " +
                         "Set JWT_SECRET for non-dev environments.");
            }
        }

        // ── LLM API Key Guard ──
        String geminiKey = properties.getLlm().getGeminiApiKey();
        if ((geminiKey == null || geminiKey.isBlank()) && !properties.isDebug()) {
            log.warn("⚠️  No Gemini API key configured (GEMINI_API_KEY). " +
                     "LLM features will fail at runtime.");
        }

        log.info("✅ Synaptiq startup validation passed (profiles={})", activeProfiles);
    }
}
