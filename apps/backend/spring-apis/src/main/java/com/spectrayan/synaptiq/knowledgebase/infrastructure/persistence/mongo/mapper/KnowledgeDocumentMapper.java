package com.spectrayan.synaptiq.knowledgebase.infrastructure.persistence.mongo.mapper;

import com.spectrayan.synaptiq.knowledgebase.domain.model.KnowledgeDocument;
import com.spectrayan.synaptiq.knowledgebase.infrastructure.persistence.mongo.KnowledgeDocumentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface KnowledgeDocumentMapper {
    KnowledgeDocumentEntity toEntity(KnowledgeDocument domain);
    KnowledgeDocument toDomain(KnowledgeDocumentEntity entity);
}
