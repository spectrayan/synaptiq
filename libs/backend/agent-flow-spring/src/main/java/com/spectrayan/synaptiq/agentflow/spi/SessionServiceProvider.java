package com.spectrayan.synaptiq.agentflow.spi;

import com.spectrayan.synaptiq.agentflow.builder.models.settings.SessionConfig;

/**
 * SPI for pluggable session storage backends.
 * <p>
 * Implementations create the appropriate session service instance
 * based on configuration. Supported types:
 * <ul>
 *   <li>{@code in-memory} — development/testing (default)</li>
 *   <li>{@code firestore} — persistent via Google Firestore</li>
 *   <li>{@code vertex-ai} — managed via Vertex AI Session Service</li>
 * </ul>
 */
public interface SessionServiceProvider {

    /**
     * Identifier for this provider, e.g. {@code "in-memory"}, {@code "firestore"}.
     */
    String type();

    /**
     * Create a session service instance from the given configuration.
     *
     * @param config session configuration
     * @return the native session service object (provider-specific)
     */
    Object create(SessionConfig config);
}
