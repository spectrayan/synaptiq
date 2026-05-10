package com.spectrayan.synaptiq.datasource.domain.model;

import com.spectrayan.synaptiq.shared.domain.AggregateRoot;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregate root for the DataSource bounded context.
 * <p>
 * A DataSource represents a connection to an external or internal data system
 * that an Application can query through the AI agent. The platform supports
 * multiple connection types: Synaptiq-native (MongoDB), MCP servers, REST APIs,
 * and direct database connections.
 * <p>
 * Pure POJO — NO framework annotations.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class DataSource extends AggregateRoot {

    private String dataSourceId;
    private String tenantId;
    private String name;
    private String description;
    private DataSourceType type;
    private ConnectionConfig connection;
    private DataSourceSchema schema;
    @Builder.Default private DataSourceStatus status = DataSourceStatus.PENDING;
    private Instant lastSyncedAt;

    // ═══════════════════════════════════════════════════════════════
    //  Connection config hierarchy (sealed-like pattern for Java 17+)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Base type for connection configuration.
     * Concrete subtypes define connection details per {@link DataSourceType}.
     */
    @Getter @Setter @NoArgsConstructor
    public static abstract class ConnectionConfig {
        private CredentialRef credentialRef;
    }

    /**
     * Config for Synaptiq-native data (stored in tenant's own MongoDB).
     * Uses tenant-isolated collections for storing records.
     */
    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class SynaptiqNativeConfig extends ConnectionConfig {
        @Builder.Default private String collectionName = "records";
    }

    /**
     * Config for connecting to an external MCP server.
     */
    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class McpConnectionConfig extends ConnectionConfig {
        private String serverUrl;
        @Builder.Default private McpTransport transport = McpTransport.HTTP;
        @Builder.Default private Map<String, String> env = new HashMap<>();
        @Builder.Default private List<String> enabledTools = new ArrayList<>();
    }

    /**
     * Config for connecting to an external REST API.
     */
    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class RestApiConfig extends ConnectionConfig {
        private String baseUrl;
        @Builder.Default private Map<String, String> headers = new HashMap<>();
        @Builder.Default private String authType = "none";
    }

    /**
     * Config for direct connection to an external database.
     */
    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class DatabaseDirectConfig extends ConnectionConfig {
        private String connectionUri;
        private String databaseName;
        private String collectionOrTable;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Schema definition (inferred or manually defined)
    // ═══════════════════════════════════════════════════════════════

    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class DataSourceSchema {
        @Builder.Default private String schemaName = "Default Schema";
        @Builder.Default private List<SchemaField> fields = new ArrayList<>();
        @Builder.Default private int schemaVersion = 1;
        @Builder.Default private boolean autoInferred = false;
    }

    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class SchemaField {
        private String fieldId;
        private String label;
        private String type;
        @Builder.Default private boolean required = false;
        @Builder.Default private boolean searchable = true;
        @Builder.Default private boolean displayable = true;
        @Builder.Default private boolean filterable = false;
        @Builder.Default private int displayOrder = 0;
        @Builder.Default private List<String> enumValues = new ArrayList<>();
    }

    // ═══════════════════════════════════════════════════════════════
    //  Credential reference (pluggable secret management)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Pluggable credential reference — supports GCP Secret Manager,
     * external secret management, MongoDB-stored encrypted secrets,
     * or external OAuth2 providers.
     */
    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class CredentialRef {
        private CredentialType type;
        /** Secret identifier — GCP secret name, Vault path, or MongoDB doc ID. */
        private String secretRef;
        /** OAuth2 provider name (e.g., "salesforce", "google") — only for OAUTH2 type. */
        private String oauthProvider;
    }
}
