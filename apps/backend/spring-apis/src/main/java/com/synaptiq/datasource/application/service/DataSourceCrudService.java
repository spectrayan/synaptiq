package com.synaptiq.datasource.application.service;

import com.synaptiq.datasource.application.port.in.DataSourceCrudUseCase;
import com.synaptiq.datasource.application.port.out.DataSourcePersistencePort;
import com.synaptiq.datasource.domain.model.DataSource;
import com.synaptiq.datasource.domain.model.DataSourceStatus;
import com.synaptiq.datasource.domain.model.DataSourceType;
import com.synaptiq.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * DataSource service for CRUD operations.
 * Delegates persistence to the outbound port.
 */
@Service
@RequiredArgsConstructor
public class DataSourceCrudService implements DataSourceCrudUseCase {

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
        return persistence.save(ds);
    }

    @Override
    public Mono<DataSource> getByDataSourceId(String dataSourceId) {
        return persistence.findByDataSourceId(dataSourceId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                "DataSource '" + dataSourceId + "' not found")));
    }

    @Override
    public Flux<DataSource> listByTenantId(String tenantId) {
        return persistence.findAllByTenantId(tenantId);
    }

    @Override
    public Mono<Void> delete(String dataSourceId) {
        return persistence.deleteByDataSourceId(dataSourceId);
    }
}
