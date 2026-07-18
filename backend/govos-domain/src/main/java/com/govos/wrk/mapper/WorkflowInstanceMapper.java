package com.govos.wrk.mapper;

import com.govos.wrk.dto.WorkflowInstanceDto;
import com.govos.wrk.entity.WorkflowInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkflowInstanceMapper {

    @Mapping(source = "workflowVersion.id", target = "workflowVersionId")
    WorkflowInstanceDto toDto(WorkflowInstance entity);
}
