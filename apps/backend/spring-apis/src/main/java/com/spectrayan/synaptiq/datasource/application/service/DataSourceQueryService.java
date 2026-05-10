package com.spectrayan.synaptiq.datasource.application.service;

import com.spectrayan.synaptiq.datasource.application.port.in.DataSourceQueryUseCase;
import com.spectrayan.synaptiq.datasource.application.port.out.DataSourcePersistencePort;
import com.spectrayan.synaptiq.datasource.domain.model.DataSource;
import com.spectrayan.synaptiq.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Handles DataSource read operations (get, list).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSourceQueryService implements DataSourceQueryUseCase {

    private final DataSourcePersistencePort persistence;

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
}
