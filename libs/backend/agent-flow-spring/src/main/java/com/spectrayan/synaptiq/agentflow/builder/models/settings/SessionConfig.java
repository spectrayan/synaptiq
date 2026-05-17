package com.spectrayan.synaptiq.agentflow.builder.models.settings;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for session storage backend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionConfig {

    @Builder.Default
    private SessionType type = SessionType.IN_MEMORY;

    /** Firestore project ID or Vertex AI endpoint (when applicable). */
    private String projectId;

    /** Firestore/Vertex AI location. */
    private String location;

    /** Additional provider-specific properties. */
    @Builder.Default
    private Map<String, String> properties = new HashMap<>();

    public enum SessionType {
        IN_MEMORY, FIRESTORE, VERTEX_AI
    }
}
