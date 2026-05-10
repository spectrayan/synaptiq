package com.spectrayan.synaptiq.action.infrastructure.web;

import com.spectrayan.synaptiq.action.application.port.in.ExecuteActionUseCase.ActionResult;
import com.spectrayan.synaptiq.action.domain.model.SavedItem;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.ActionResultResponse;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.ItemSnapshotResponse;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.SavedItemListResponse;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.SavedItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ActionDtoMapper {

    @Mapping(target = "data", expression = "java(result.resultId() != null ? java.util.Map.of(\"id\", result.resultId()) : java.util.Map.of())")
    ActionResultResponse toDto(ActionResult result);

    @Mapping(target = "itemSnapshot", source = "itemSnapshot")
    SavedItemResponse toDto(SavedItem item);

    ItemSnapshotResponse toDto(SavedItem.ItemSnapshot snapshot);

    default SavedItemListResponse toListDto(List<SavedItemResponse> items) {
        return new SavedItemListResponse().items(items).total(items.size());
    }

    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
