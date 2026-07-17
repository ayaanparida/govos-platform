package com.govos.cmp.mapper;

import com.govos.cmp.dto.ComplaintCommentCreateRequest;
import com.govos.cmp.dto.ComplaintCommentDto;
import com.govos.cmp.entity.ComplaintComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ComplaintCommentMapper {

    @Mapping(source = "complaint.id", target = "complaintId")
    ComplaintCommentDto toDto(ComplaintComment entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "complaint", ignore = true)
    ComplaintComment toEntity(ComplaintCommentCreateRequest request);
}
