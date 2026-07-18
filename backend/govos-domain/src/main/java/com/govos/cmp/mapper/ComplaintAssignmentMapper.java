package com.govos.cmp.mapper;

import com.govos.cmp.dto.ComplaintAssignmentCreateRequest;
import com.govos.cmp.dto.ComplaintAssignmentDto;
import com.govos.cmp.dto.ComplaintAssignmentUpdateRequest;
import com.govos.cmp.entity.ComplaintAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ComplaintAssignmentMapper {

    @Mapping(source = "complaint.id", target = "complaintId")
    ComplaintAssignmentDto toDto(ComplaintAssignment entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "complaint", ignore = true)
    @Mapping(target = "assignmentStatus", ignore = true)
    @Mapping(target = "assignedAt", ignore = true)
    @Mapping(target = "acceptedAt", ignore = true)
    @Mapping(target = "rejectedAt", ignore = true)
    @Mapping(target = "rejectionReasonKey", ignore = true)
    @Mapping(target = "isCurrent", ignore = true)
    ComplaintAssignment toEntity(ComplaintAssignmentCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "complaint", ignore = true)
    @Mapping(target = "assignedByUserId", ignore = true)
    @Mapping(target = "assignedAt", ignore = true)
    @Mapping(target = "acceptedAt", ignore = true)
    @Mapping(target = "rejectedAt", ignore = true)
    void updateEntity(ComplaintAssignmentUpdateRequest request, @MappingTarget ComplaintAssignment entity);
}
