package com.spectrayan.synaptiq.knowledgebase.infrastructure.persistence.mongo.mapper;

import com.spectrayan.synaptiq.knowledgebase.domain.model.KnowledgeCategory;
import com.spectrayan.synaptiq.knowledgebase.infrastructure.persistence.mongo.KnowledgeCategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface KnowledgeCategoryMapper {
    KnowledgeCategoryEntity toEntity(KnowledgeCategory domain);
    KnowledgeCategory toDomain(KnowledgeCategoryEntity entity);
}
