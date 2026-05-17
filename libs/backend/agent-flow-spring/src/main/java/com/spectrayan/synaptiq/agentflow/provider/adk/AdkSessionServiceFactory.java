package com.spectrayan.synaptiq.agentflow.provider.adk;

import com.google.adk.sessions.BaseSessionService;
import com.google.adk.sessions.InMemorySessionService;
import com.spectrayan.synaptiq.agentflow.builder.models.settings.SessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating the appropriate ADK {@link BaseSessionService}
 * based on {@link SessionConfig}.
 * <p>
 * Supported backends:
 * <ul>
 *   <li>{@code IN_MEMORY} — {@link InMemorySessionService} (default, dev/test)</li>
 *   <li>{@code FIRESTORE} — Google Firestore-backed sessions (persistent)</li>
 *   <li>{@code VERTEX_AI} — Vertex AI managed sessions</li>
 * </ul>
 */
public final class AdkSessionServiceFactory {

    private static final Logger log = LoggerFactory.getLogger(AdkSessionServiceFactory.class);

    private AdkSessionServiceFactory() {
        // utility class
    }

    /**
     * Create the ADK session service matching the given configuration.
     *
     * @param config session configuration
     * @return a configured {@link BaseSessionService} instance
     */
    public static BaseSessionService create(SessionConfig config) {
        if (config == null) {
            log.debug("No session config provided, defaulting to InMemorySessionService");
            return new InMemorySessionService();
        }

        return switch (config.getType()) {
            case IN_MEMORY -> {
                log.info("Using InMemorySessionService");
                yield new InMemorySessionService();
            }
            case FIRESTORE -> {
                log.info("Using FirestoreSessionService (project: {}, location: {})",
                    config.getProjectId(), config.getLocation());
                yield createFirestoreSessionService(config);
            }
            case VERTEX_AI -> {
                log.info("Using VertexAiSessionService (project: {}, location: {})",
                    config.getProjectId(), config.getLocation());
                yield createVertexAiSessionService(config);
            }
        };
    }

    /**
     * Create a Firestore-backed session service via reflection.
     * Avoids hard compile-time dependency on google-cloud-firestore.
     */
    private static BaseSessionService createFirestoreSessionService(SessionConfig config) {
        try {
            Class<?> clazz = Class.forName("com.google.adk.sessions.FirestoreSessionService");
            var constructor = clazz.getConstructor(String.class);
            return (BaseSessionService) constructor.newInstance(config.getProjectId());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                "FirestoreSessionService requires 'google-cloud-firestore' dependency on the classpath.", e
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to create FirestoreSessionService", e);
        }
    }

    /**
     * Create a Vertex AI managed session service via reflection.
     * Avoids hard compile-time dependency on google-cloud-vertexai.
     */
    private static BaseSessionService createVertexAiSessionService(SessionConfig config) {
        try {
            Class<?> clazz = Class.forName("com.google.adk.sessions.VertexAiSessionService");
            var constructor = clazz.getConstructor(String.class, String.class);
            return (BaseSessionService) constructor.newInstance(config.getProjectId(), config.getLocation());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                "VertexAiSessionService requires 'google-cloud-vertexai' dependency on the classpath.", e
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to create VertexAiSessionService", e);
        }
    }
}
