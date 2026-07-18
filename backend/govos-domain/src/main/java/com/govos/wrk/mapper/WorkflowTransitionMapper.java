package com.govos.wrk.mapper;

import com.govos.wrk.dto.WorkflowTransitionDto;
import com.govos.wrk.entity.WorkflowTransition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkflowTransitionMapper {

    @Mapping(source = "fromStep.id", target = "fromStepId")
    @Mapping(source = "toStep.id", target = "toStepId")
    WorkflowTransitionDto toDto(WorkflowTransition entity);
}
