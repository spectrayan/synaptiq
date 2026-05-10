package com.spectrayan.synaptiq.datasource.infrastructure.persistence.mongo.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MongoDB document for the DataSource aggregate.
 * <p>
 * Stored in the per-tenant database. Connection configs are typed
 * embedded documents. Credentials are stored by reference (not inline).
 */
@Data
@Builder
@Document(collection = "data_sources")
public class DataSourceDocument {

    @Id private String id;
    @Indexed(unique = true) private String dataSourceId;
    @Indexed private String tenantId;
    private String name;
    private String description;
    private String type; // DataSourceType enum value
    @Builder.Default private String status = "PENDING";

    // ── Connection config (polymorphic, discriminated by 'type') ─────

    private SynaptiqNativeConfigEmbed synaptiqNativeConfig;
    private McpConnectionConfigEmbed mcpConnectionConfig;
    private RestApiConfigEmbed restApiConfig;
    private DatabaseDirectConfigEmbed databaseDirectConfig;

    // ── Schema ───────────────────────────────────────────────────────

    private DataSourceSchemaEmbed schema;

    // ── Credential reference ─────────────────────────────────────────

    private CredentialRefEmbed credentialRef;

    // ── Sync metadata ────────────────────────────────────────────────

    private Instant lastSyncedAt;

    // ── Audit ────────────────────────────────────────────────────────

    @Version private Long version;
    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;

    // ═══════════════════════════════════════════════════════════════
    //  Embedded sub-documents
    // ═══════════════════════════════════════════════════════════════

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class SynaptiqNativeConfigEmbed {
        @Builder.Default private String collectionName = "records";
    }

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class McpConnectionConfigEmbed {
        private String serverUrl;
        @Builder.Default private String transport = "HTTP";
        @Builder.Default private Map<String, String> env = new HashMap<>();
        @Builder.Default private List<String> enabledTools = new ArrayList<>();
    }

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class RestApiConfigEmbed {
        private String baseUrl;
        @Builder.Default private Map<String, String> headers = new HashMap<>();
        @Builder.Default private String authType = "none";
    }

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class DatabaseDirectConfigEmbed {
        private String connectionUri;
        private String databaseName;
        private String collectionOrTable;
    }

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class DataSourceSchemaEmbed {
        private String schemaName;
        @Builder.Default private List<SchemaFieldEmbed> fields = new ArrayList<>();
        @Builder.Default private int schemaVersion = 1;
        @Builder.Default private boolean autoInferred = false;
    }

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class SchemaFieldEmbed {
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

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class CredentialRefEmbed {
        private String type; // CredentialType enum value
        private String secretRef;
        private String oauthProvider;
    }
}
