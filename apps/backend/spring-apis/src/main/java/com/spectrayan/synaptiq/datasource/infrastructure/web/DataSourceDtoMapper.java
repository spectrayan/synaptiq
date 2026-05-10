package com.spectrayan.synaptiq.datasource.infrastructure.web;

import com.spectrayan.synaptiq.datasource.domain.model.DataSource;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.DataSourceResponse;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.DataSourceStatus;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.DataSourceType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * MapStruct mapper: DataSource domain → DataSourceResponse DTO.
 */
@Mapper(componentModel = "spring")
public interface DataSourceDtoMapper {

    @Mapping(target = "type", source = "type", qualifiedByName = "mapDsType")
    @Mapping(target = "status", source = "status", qualifiedByName = "mapDsStatus")
    @Mapping(target = "connection", ignore = true)
    @Mapping(target = "credentialRef", ignore = true)
    @Mapping(target = "schema", ignore = true)
    DataSourceResponse toDto(DataSource ds);

    @org.mapstruct.Named("mapDsType")
    default DataSourceType mapDsType(com.spectrayan.synaptiq.datasource.domain.model.DataSourceType type) {
        return type == null ? null : DataSourceType.fromValue(type.name());
    }

    @org.mapstruct.Named("mapDsStatus")
    default DataSourceStatus mapDsStatus(com.spectrayan.synaptiq.datasource.domain.model.DataSourceStatus status) {
        return status == null ? null : DataSourceStatus.fromValue(status.name());
    }

    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
