package com.govos.wrk.mapper;

import com.govos.wrk.dto.WorkflowVersionDto;
import com.govos.wrk.entity.WorkflowVersion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkflowVersionMapper {

    @Mapping(source = "definition.id", target = "definitionId")
    WorkflowVersionDto toDto(WorkflowVersion entity);
}
