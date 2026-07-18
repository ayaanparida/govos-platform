package com.govos.wrk.service;

import com.govos.wrk.dto.CreateWorkflowHistoryRequest;
import com.govos.wrk.dto.WorkflowHistoryDto;

import java.util.List;
import java.util.UUID;

public interface WorkflowHistoryService {

    WorkflowHistoryDto getById(UUID id);

    List<WorkflowHistoryDto> getByWorkflowInstanceId(UUID workflowInstanceId);

    WorkflowHistoryDto create(CreateWorkflowHistoryRequest request);
}
