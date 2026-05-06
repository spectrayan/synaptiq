package com.synaptiq.tenant.infrastructure.web;

import com.synaptiq.tenant.domain.model.Tenant;
import com.synaptiq.infrastructure.in.web.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface TenantDtoMapper {

    @Mapping(target = "status", source = "status", qualifiedByName = "mapTenantStatus")
    @Mapping(target = "accessMode", source = "accessMode", qualifiedByName = "mapAccessMode")
    TenantResponse toDto(Tenant tenant);

    @Named("mapTenantStatus")
    default TenantStatus mapTenantStatus(com.synaptiq.tenant.domain.model.TenantStatus status) {
        return status == null ? null : TenantStatus.fromValue(status.name());
    }

    @Named("mapAccessMode")
    default AccessMode mapAccessMode(com.synaptiq.tenant.domain.model.AccessMode mode) {
        return mode == null ? null : AccessMode.fromValue(mode.name());
    }

    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
