package com.spectrayan.synaptiq.datasource.application.service;

import com.spectrayan.synaptiq.datasource.application.port.in.DataSourceCommandUseCase;
import com.spectrayan.synaptiq.datasource.application.port.out.DataSourcePersistencePort;
import com.spectrayan.synaptiq.datasource.domain.model.DataSource;
import com.spectrayan.synaptiq.datasource.domain.model.DataSourceStatus;
import com.spectrayan.synaptiq.datasource.domain.model.DataSourceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Handles DataSource write operations (create, delete).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSourceCommandService implements DataSourceCommandUseCase {

    private final DataSourcePersistencePort persistence;

    @Override
    public Mono<DataSource> create(CreateDataSourceCommand command) {
        var ds = DataSource.builder()
            .dataSourceId(UUID.randomUUID().toString())
            .tenantId(command.tenantId())
            .name(command.name())
            .description(command.description())
            .type(DataSourceType.valueOf(command.type()))
            .status(DataSourceStatus.PENDING)
            .build();
        log.info("Creating datasource '{}' for tenant '{}'", ds.getName(), command.tenantId());
        return persistence.save(ds);
    }

    @Override
    public Mono<Void> delete(String dataSourceId) {
        log.info("Deleting datasource '{}'", dataSourceId);
        return persistence.deleteByDataSourceId(dataSourceId);
    }
}
