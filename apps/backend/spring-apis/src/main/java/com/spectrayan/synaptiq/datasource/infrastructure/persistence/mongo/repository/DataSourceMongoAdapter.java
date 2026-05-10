package com.spectrayan.synaptiq.datasource.infrastructure.persistence.mongo.repository;

import com.spectrayan.synaptiq.datasource.application.port.out.DataSourcePersistencePort;
import com.spectrayan.synaptiq.datasource.domain.model.*;
import com.spectrayan.synaptiq.datasource.infrastructure.persistence.mongo.entity.DataSourceDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Adapter implementing the domain's {@link DataSourcePersistencePort}.
 * Handles polymorphic ConnectionConfig mapping manually (discriminated by type).
 */
@Component
@RequiredArgsConstructor
public class DataSourceMongoAdapter implements DataSourcePersistencePort {

    private final DataSourceReactiveMongoRepository mongoRepository;

    @Override
    public Mono<DataSource> save(DataSource ds) {
        return mongoRepository.save(toDocument(ds)).map(this::toDomain);
    }

    @Override
    public Mono<DataSource> findByDataSourceId(String dataSourceId) {
        return mongoRepository.findByDataSourceId(dataSourceId).map(this::toDomain);
    }

    @Override
    public Flux<DataSource> findAllByTenantId(String tenantId) {
        return mongoRepository.findByTenantId(tenantId).map(this::toDomain);
    }

    @Override
    public Mono<Long> countByTenantId(String tenantId) {
        return mongoRepository.countByTenantId(tenantId);
    }

    @Override
    public Mono<Void> deleteByDataSourceId(String dataSourceId) {
        return mongoRepository.deleteByDataSourceId(dataSourceId);
    }

    // ── Domain → Document ───────────────────────────────────────────

    private DataSourceDocument toDocument(DataSource ds) {
        var builder = DataSourceDocument.builder()
            .id(ds.getId())
            .dataSourceId(ds.getDataSourceId())
            .tenantId(ds.getTenantId())
            .name(ds.getName())
            .description(ds.getDescription())
            .type(ds.getType() != null ? ds.getType().name() : null)
            .status(ds.getStatus() != null ? ds.getStatus().name() : "PENDING")
            .lastSyncedAt(ds.getLastSyncedAt())
            .createdAt(ds.getCreatedAt())
            .updatedAt(ds.getUpdatedAt());

        // Polymorphic connection config → discriminated embeds
        if (ds.getConnection() != null) {
            mapConnectionToDocument(ds.getConnection(), builder);
            if (ds.getConnection().getCredentialRef() != null) {
                var cr = ds.getConnection().getCredentialRef();
                builder.credentialRef(DataSourceDocument.CredentialRefEmbed.builder()
                    .type(cr.getType() != null ? cr.getType().name() : null)
                    .secretRef(cr.getSecretRef())
                    .oauthProvider(cr.getOauthProvider())
                    .build());
            }
        }

        if (ds.getSchema() != null) {
            builder.schema(mapSchemaToEmbed(ds.getSchema()));
        }

        return builder.build();
    }

    private void mapConnectionToDocument(DataSource.ConnectionConfig conn, DataSourceDocument.DataSourceDocumentBuilder builder) {
        if (conn instanceof DataSource.SynaptiqNativeConfig c) {
            builder.synaptiqNativeConfig(DataSourceDocument.SynaptiqNativeConfigEmbed.builder()
                .collectionName(c.getCollectionName()).build());
        } else if (conn instanceof DataSource.McpConnectionConfig c) {
            builder.mcpConnectionConfig(DataSourceDocument.McpConnectionConfigEmbed.builder()
                .serverUrl(c.getServerUrl())
                .transport(c.getTransport() != null ? c.getTransport().name() : "HTTP")
                .env(c.getEnv() != null ? c.getEnv() : new HashMap<>())
                .enabledTools(c.getEnabledTools() != null ? c.getEnabledTools() : new ArrayList<>())
                .build());
        } else if (conn instanceof DataSource.RestApiConfig c) {
            builder.restApiConfig(DataSourceDocument.RestApiConfigEmbed.builder()
                .baseUrl(c.getBaseUrl())
                .headers(c.getHeaders() != null ? c.getHeaders() : new HashMap<>())
                .authType(c.getAuthType())
                .build());
        } else if (conn instanceof DataSource.DatabaseDirectConfig c) {
            builder.databaseDirectConfig(DataSourceDocument.DatabaseDirectConfigEmbed.builder()
                .connectionUri(c.getConnectionUri())
                .databaseName(c.getDatabaseName())
                .collectionOrTable(c.getCollectionOrTable())
                .build());
        }
    }

    private DataSourceDocument.DataSourceSchemaEmbed mapSchemaToEmbed(DataSource.DataSourceSchema schema) {
        return DataSourceDocument.DataSourceSchemaEmbed.builder()
            .schemaName(schema.getSchemaName())
            .schemaVersion(schema.getSchemaVersion())
            .autoInferred(schema.isAutoInferred())
            .fields(schema.getFields() != null
                ? schema.getFields().stream().map(f -> DataSourceDocument.SchemaFieldEmbed.builder()
                    .fieldId(f.getFieldId()).label(f.getLabel()).type(f.getType())
                    .required(f.isRequired()).searchable(f.isSearchable())
                    .displayable(f.isDisplayable()).filterable(f.isFilterable())
                    .displayOrder(f.getDisplayOrder())
                    .enumValues(f.getEnumValues() != null ? f.getEnumValues() : new ArrayList<>())
                    .build()).toList()
                : new ArrayList<>())
            .build();
    }

    // ── Document → Domain ───────────────────────────────────────────

    private DataSource toDomain(DataSourceDocument doc) {
        var builder = DataSource.builder()
            .id(doc.getId())
            .dataSourceId(doc.getDataSourceId())
            .tenantId(doc.getTenantId())
            .name(doc.getName())
            .description(doc.getDescription())
            .type(safeEnum(doc.getType(), DataSourceType.class, DataSourceType.SYNAPTIQ_NATIVE))
            .status(safeEnum(doc.getStatus(), DataSourceStatus.class, DataSourceStatus.PENDING))
            .lastSyncedAt(doc.getLastSyncedAt())
            .createdAt(doc.getCreatedAt())
            .updatedAt(doc.getUpdatedAt());

        // Polymorphic connection config from discriminated embeds
        DataSource.ConnectionConfig conn = mapConnectionToDomain(doc);
        if (conn != null && doc.getCredentialRef() != null) {
            var cr = doc.getCredentialRef();
            conn.setCredentialRef(DataSource.CredentialRef.builder()
                .type(safeEnum(cr.getType(), CredentialType.class, null))
                .secretRef(cr.getSecretRef())
                .oauthProvider(cr.getOauthProvider())
                .build());
        }
        builder.connection(conn);

        if (doc.getSchema() != null) {
            builder.schema(mapSchemaToDomain(doc.getSchema()));
        }

        return builder.build();
    }

    private DataSource.ConnectionConfig mapConnectionToDomain(DataSourceDocument doc) {
        if (doc.getSynaptiqNativeConfig() != null) {
            return DataSource.SynaptiqNativeConfig.builder()
                .collectionName(doc.getSynaptiqNativeConfig().getCollectionName()).build();
        } else if (doc.getMcpConnectionConfig() != null) {
            var c = doc.getMcpConnectionConfig();
            return DataSource.McpConnectionConfig.builder()
                .serverUrl(c.getServerUrl())
                .transport(safeEnum(c.getTransport(), McpTransport.class, McpTransport.HTTP))
                .env(c.getEnv()).enabledTools(c.getEnabledTools())
                .build();
        } else if (doc.getRestApiConfig() != null) {
            var c = doc.getRestApiConfig();
            return DataSource.RestApiConfig.builder()
                .baseUrl(c.getBaseUrl()).headers(c.getHeaders()).authType(c.getAuthType())
                .build();
        } else if (doc.getDatabaseDirectConfig() != null) {
            var c = doc.getDatabaseDirectConfig();
            return DataSource.DatabaseDirectConfig.builder()
                .connectionUri(c.getConnectionUri()).databaseName(c.getDatabaseName())
                .collectionOrTable(c.getCollectionOrTable())
                .build();
        }
        return null;
    }

    private DataSource.DataSourceSchema mapSchemaToDomain(DataSourceDocument.DataSourceSchemaEmbed embed) {
        return DataSource.DataSourceSchema.builder()
            .schemaName(embed.getSchemaName())
            .schemaVersion(embed.getSchemaVersion())
            .autoInferred(embed.isAutoInferred())
            .fields(embed.getFields() != null
                ? embed.getFields().stream().map(f -> DataSource.SchemaField.builder()
                    .fieldId(f.getFieldId()).label(f.getLabel()).type(f.getType())
                    .required(f.isRequired()).searchable(f.isSearchable())
                    .displayable(f.isDisplayable()).filterable(f.isFilterable())
                    .displayOrder(f.getDisplayOrder())
                    .enumValues(f.getEnumValues() != null ? f.getEnumValues() : new ArrayList<>())
                    .build()).toList()
                : new ArrayList<>())
            .build();
    }

    private <E extends Enum<E>> E safeEnum(String value, Class<E> enumClass, E defaultValue) {
        if (value == null) return defaultValue;
        try { return Enum.valueOf(enumClass, value); }
        catch (IllegalArgumentException e) { return defaultValue; }
    }
}
