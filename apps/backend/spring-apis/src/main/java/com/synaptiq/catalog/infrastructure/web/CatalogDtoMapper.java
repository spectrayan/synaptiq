package com.synaptiq.catalog.infrastructure.web;

import com.synaptiq.catalog.domain.model.CatalogItem;
import com.synaptiq.catalog.domain.model.CatalogSchema;
import com.synaptiq.infrastructure.in.web.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring", imports = {com.synaptiq.infrastructure.in.web.dto.CatalogItemStatus.class})
public interface CatalogDtoMapper {

    CatalogSchemaResponse toSchemaDto(CatalogSchema schema);

    @Mapping(target = "status", expression = "java(item.getStatus() != null ? CatalogItemStatus.fromValue(item.getStatus()) : null)")
    CatalogItemResponse toItemDto(CatalogItem item);

    default CatalogItemListResponse toListDto(List<CatalogItemResponse> items, long total) {
        return new CatalogItemListResponse().items(items).total(total);
    }

    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }

    default SchemaFieldResponse toFieldDto(CatalogSchema.SchemaField field) {
        return new SchemaFieldResponse()
            .fieldId(field.getFieldId())
            .label(field.getLabel())
            .type(field.getType())
            .required(field.isRequired())
            .searchable(field.isSearchable())
            .displayable(field.isDisplayable())
            .filterable(field.isFilterable())
            .displayOrder(field.getDisplayOrder())
            .enumValues(field.getEnumValues());
    }
}
