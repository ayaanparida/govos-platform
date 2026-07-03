package com.govos.wrk.mapper;

import com.govos.wrk.dto.WorkflowTaskDto;
import com.govos.wrk.entity.WorkflowTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkflowTaskMapper {

    @Mapping(source = "workflowInstance.id", target = "workflowInstanceId")
    @Mapping(source = "assignedTo.id", target = "assignedToId")
    @Mapping(source = "assignedRole.id", target = "assignedRoleId")
    @Mapping(source = "step.id", target = "stepId")
    WorkflowTaskDto toDto(WorkflowTask entity);
}
