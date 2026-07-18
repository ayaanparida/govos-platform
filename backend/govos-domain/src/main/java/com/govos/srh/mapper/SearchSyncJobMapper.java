package com.govos.srh.mapper;

import com.govos.srh.dto.SearchSyncJobCreateRequest;
import com.govos.srh.dto.SearchSyncJobDto;
import com.govos.srh.dto.SearchSyncJobUpdateRequest;
import com.govos.srh.entity.SearchSyncJob;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SearchSyncJobMapper {

    @Mapping(source = "searchIndex.id", target = "searchIndexId")
    @Mapping(source = "status", target = "jobStatus")
    SearchSyncJobDto toDto(SearchSyncJob entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "searchIndex", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "processedCount", ignore = true)
    @Mapping(target = "successCount", ignore = true)
    @Mapping(target = "failureCount", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    SearchSyncJob toEntity(SearchSyncJobCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "searchIndex", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "processedCount", ignore = true)
    @Mapping(target = "successCount", ignore = true)
    @Mapping(target = "failureCount", ignore = true)
    void updateEntity(SearchSyncJobUpdateRequest request, @MappingTarget SearchSyncJob entity);
}
