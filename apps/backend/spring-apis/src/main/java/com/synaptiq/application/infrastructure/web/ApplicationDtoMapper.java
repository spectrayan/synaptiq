package com.synaptiq.application.infrastructure.web;

import com.synaptiq.application.domain.model.Application;
import com.synaptiq.infrastructure.in.web.dto.ApplicationResponse;
import com.synaptiq.infrastructure.in.web.dto.ApplicationStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * MapStruct mapper: Application domain → ApplicationResponse DTO.
 */
@Mapper(componentModel = "spring")
public interface ApplicationDtoMapper {

    @Mapping(target = "isDefault", source = "default")
    @Mapping(target = "status", source = "status", qualifiedByName = "mapAppStatus")
    @Mapping(target = "aiPersona", ignore = true)
    @Mapping(target = "guardrails", ignore = true)
    @Mapping(target = "branding", ignore = true)
    @Mapping(target = "components", ignore = true)
    @Mapping(target = "actions", ignore = true)
    @Mapping(target = "personalization", ignore = true)
    @Mapping(target = "themes", ignore = true)
    @Mapping(target = "llmOverride", ignore = true)
    ApplicationResponse toDto(Application app);

    @org.mapstruct.Named("mapAppStatus")
    default ApplicationStatus mapAppStatus(com.synaptiq.application.domain.model.ApplicationStatus status) {
        return status == null ? null : ApplicationStatus.fromValue(status.name());
    }

    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
