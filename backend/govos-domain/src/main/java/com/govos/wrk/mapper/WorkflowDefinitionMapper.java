package com.govos.wrk.mapper;

import com.govos.wrk.dto.WorkflowDefinitionDto;
import com.govos.wrk.entity.WorkflowDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkflowDefinitionMapper {

    WorkflowDefinitionDto toDto(WorkflowDefinition entity);
}
