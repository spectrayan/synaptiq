package com.spectrayan.synaptiq.agentflow.builder.models.settings;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Configuration for context caching — maps to Google ADK's
 * {@code ContextCacheConfig} or equivalent in other providers.
 * <p>
 * Context caching stores static content (system instructions, tool definitions,
 * large reference docs) and reuses cached tokens across invocations,
 * reducing costs by up to 90%.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheConfig {

    /** Whether context caching is enabled for this flow. */
    @Builder.Default
    private boolean enabled = false;

    /** Max invocations before cache refresh. */
    @Builder.Default
    private int cacheIntervals = 5;

    /** Cache time-to-live in minutes. */
    @Builder.Default
    private int ttlMinutes = 10;

    /** Minimum token count to trigger caching. */
    @Builder.Default
    private int minTokens = 2048;
}
