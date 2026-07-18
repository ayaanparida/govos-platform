package com.govos.wrk.service;

import com.govos.wrk.dto.CreateWorkflowTaskRequest;
import com.govos.wrk.dto.UpdateWorkflowTaskRequest;
import com.govos.wrk.dto.WorkflowTaskDto;
import com.govos.wrk.entity.WorkflowTaskStatus;

import java.util.List;
import java.util.UUID;

public interface WorkflowTaskService {

    WorkflowTaskDto getById(UUID id);

    List<WorkflowTaskDto> getByWorkflowInstanceId(UUID workflowInstanceId);

    List<WorkflowTaskDto> getByAssignedToId(UUID assignedToId);

    List<WorkflowTaskDto> getByStatus(WorkflowTaskStatus status);

    WorkflowTaskDto create(CreateWorkflowTaskRequest request);

    WorkflowTaskDto update(UUID id, UpdateWorkflowTaskRequest request);

    void softDelete(UUID id);
}
