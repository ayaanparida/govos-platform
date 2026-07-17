package com.govos.cmp.mapper;

import com.govos.cmp.dto.ComplaintMergeCreateRequest;
import com.govos.cmp.dto.ComplaintMergeDto;
import com.govos.cmp.entity.ComplaintMerge;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ComplaintMergeMapper {

    @Mapping(source = "complaint.id", target = "survivingComplaintId")
    ComplaintMergeDto toDto(ComplaintMerge entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "complaint", ignore = true)
    @Mapping(target = "status", ignore = true)
    ComplaintMerge toEntity(ComplaintMergeCreateRequest request);
}
