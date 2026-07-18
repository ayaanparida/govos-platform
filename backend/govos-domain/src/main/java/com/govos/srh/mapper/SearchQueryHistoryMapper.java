package com.govos.srh.mapper;

import com.govos.srh.dto.SearchQueryHistoryDto;
import com.govos.srh.entity.SearchQueryHistory;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SearchQueryHistoryMapper {

    SearchQueryHistoryDto toDto(SearchQueryHistory entity);
}
