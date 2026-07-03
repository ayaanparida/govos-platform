package com.govos.wrk.mapper;

import com.govos.wrk.dto.WorkflowStepDto;
import com.govos.wrk.entity.WorkflowStep;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkflowStepMapper {

    @Mapping(source = "workflowVersion.id", target = "workflowVersionId")
    WorkflowStepDto toDto(WorkflowStep entity);
}
