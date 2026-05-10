package com.spectrayan.synaptiq.integration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Result of a connection test for an integration route.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionTestResult {

    private boolean success;
    private String message;
    private long durationMs;
    private Instant testedAt;

    public static ConnectionTestResult success(String message, long durationMs) {
        return ConnectionTestResult.builder()
                .success(true)
                .message(message)
                .durationMs(durationMs)
                .testedAt(Instant.now())
                .build();
    }

    public static ConnectionTestResult failure(String message, long durationMs) {
        return ConnectionTestResult.builder()
                .success(false)
                .message(message)
                .durationMs(durationMs)
                .testedAt(Instant.now())
                .build();
    }
}
