package com.synaptiq.application.infrastructure.persistence.mongo.mapper;

import com.synaptiq.application.domain.model.Application;
import com.synaptiq.application.infrastructure.persistence.mongo.entity.ApplicationDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper between Application domain model and MongoDB document.
 * <p>
 * Lombok boolean fields named {@code isXxx} generate {@code isXxx()} accessors.
 * MapStruct sees these as property "xxx" (dropping the "is" prefix).
 * The explicit mappings below ensure correct round-tripping.
 */
@Mapper(componentModel = "spring")
public interface ApplicationPersistenceMapper {

    @Mapping(target = "version", ignore = true)
    ApplicationDocument toDocument(Application application);

    @Mapping(target = "domainEvents", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Application toDomain(ApplicationDocument document);
}
