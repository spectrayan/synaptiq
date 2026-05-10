package com.spectrayan.synaptiq.action.infrastructure.persistence.mongo.mapper;

import com.spectrayan.synaptiq.action.domain.model.ActionLog;
import com.spectrayan.synaptiq.action.domain.model.ActionOutcome;
import com.spectrayan.synaptiq.action.domain.model.SavedItem;
import com.spectrayan.synaptiq.action.infrastructure.persistence.mongo.entity.ActionLogDocument;
import com.spectrayan.synaptiq.action.infrastructure.persistence.mongo.entity.SavedItemDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper between domain models and MongoDB documents.
 * Converts at the persistence boundary.
 */
@Mapper(componentModel = "spring")
public interface ActionPersistenceMapper {

    @Mapping(target = "outcome", source = "outcome", qualifiedByName = "outcomeToString")
    ActionLogDocument toDocument(ActionLog actionLog);

    @Mapping(target = "outcome", source = "outcome", qualifiedByName = "stringToOutcome")
    @Mapping(target = "domainEvents", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    ActionLog toDomain(ActionLogDocument document);

    SavedItemDocument toDocument(SavedItem savedItem);

    @Mapping(target = "domainEvents", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    SavedItem toDomain(SavedItemDocument document);

    @Named("outcomeToString")
    default String outcomeToString(ActionOutcome outcome) {
        return outcome != null ? outcome.name() : ActionOutcome.SUCCESS.name();
    }

    @Named("stringToOutcome")
    default ActionOutcome stringToOutcome(String outcome) {
        if (outcome == null) return ActionOutcome.SUCCESS;
        try { return ActionOutcome.valueOf(outcome); }
        catch (IllegalArgumentException e) { return ActionOutcome.SUCCESS; }
    }
}
