package com.govos.wrk.mapper;

import com.govos.wrk.dto.WorkflowHistoryDto;
import com.govos.wrk.entity.WorkflowHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkflowHistoryMapper {

    @Mapping(source = "workflowInstance.id", target = "workflowInstanceId")
    @Mapping(source = "performedBy.id", target = "performedById")
    WorkflowHistoryDto toDto(WorkflowHistory entity);
}
