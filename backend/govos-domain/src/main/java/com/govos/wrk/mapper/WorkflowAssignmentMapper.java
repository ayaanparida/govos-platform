package com.govos.wrk.mapper;

import com.govos.wrk.dto.WorkflowAssignmentDto;
import com.govos.wrk.entity.WorkflowAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkflowAssignmentMapper {

    @Mapping(source = "workflowTask.id", target = "workflowTaskId")
    @Mapping(source = "user.id", target = "userId")
    WorkflowAssignmentDto toDto(WorkflowAssignment entity);
}
