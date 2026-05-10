package com.spectrayan.synaptiq.auth.infrastructure.persistence.mongo.mapper;

import com.spectrayan.synaptiq.auth.domain.model.User;
import com.spectrayan.synaptiq.auth.infrastructure.persistence.mongo.entity.UserDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {
    UserDocument toDocument(User user);

    @Mapping(target = "domainEvents", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    User toDomain(UserDocument doc);
}
