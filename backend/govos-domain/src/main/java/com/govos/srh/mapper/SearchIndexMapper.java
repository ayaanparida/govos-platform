package com.govos.srh.mapper;

import com.govos.srh.dto.SearchIndexCreateRequest;
import com.govos.srh.dto.SearchIndexDto;
import com.govos.srh.dto.SearchIndexUpdateRequest;
import com.govos.srh.entity.SearchIndex;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SearchIndexMapper {

    SearchIndexDto toDto(SearchIndex entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "physicalIndexName", ignore = true)
    @Mapping(target = "activeDocumentCount", ignore = true)
    @Mapping(target = "lastReindexedAt", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "aliases", ignore = true)
    @Mapping(target = "syncJobs", ignore = true)
    SearchIndex toEntity(SearchIndexCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "physicalIndexName", ignore = true)
    @Mapping(target = "activeDocumentCount", ignore = true)
    @Mapping(target = "lastReindexedAt", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "aliases", ignore = true)
    @Mapping(target = "syncJobs", ignore = true)
    void updateEntity(SearchIndexUpdateRequest request, @MappingTarget SearchIndex entity);
}
