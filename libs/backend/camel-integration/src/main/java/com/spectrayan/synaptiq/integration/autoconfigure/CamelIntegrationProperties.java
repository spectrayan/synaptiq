package com.spectrayan.synaptiq.integration.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Camel integration engine.
 * <p>
 * Prefix: {@code synaptiq.integration}
 */
@Data
@ConfigurationProperties(prefix = "synaptiq.integration")
public class CamelIntegrationProperties {

    /** Whether the integration engine is enabled. */
    private boolean enabled = true;

    /** Health indicator configuration. */
    private Health health = new Health();

    /** Tenant-level limits. */
    private Tenant tenant = new Tenant();

    /** Camel engine configuration. */
    private Camel camel = new Camel();

    @Data
    public static class Health {
        /** Whether to register the Camel health indicator. */
        private boolean enabled = true;
    }

    @Data
    public static class Tenant {
        /** Maximum number of active routes per tenant. */
        private int maxRoutesPerTenant = 25;
        /** Maximum executions per tenant per day. */
        private int maxExecutionsPerDay = 5000;
        /** Rate limit window in seconds. */
        private int rateLimitWindowSeconds = 60;
    }

    @Data
    public static class Camel {
        /** Graceful shutdown timeout in seconds. */
        private int shutdownTimeoutSeconds = 30;
        /** Thread pool size for Camel processing. */
        private int threadPoolSize = 20;
    }
}
