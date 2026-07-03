package com.govos.wrk.service;

import com.govos.wrk.dto.CreateWorkflowAssignmentRequest;
import com.govos.wrk.dto.UpdateWorkflowAssignmentRequest;
import com.govos.wrk.dto.WorkflowAssignmentDto;

import java.util.List;
import java.util.UUID;

public interface WorkflowAssignmentService {

    WorkflowAssignmentDto getById(UUID id);

    List<WorkflowAssignmentDto> getByWorkflowTaskId(UUID workflowTaskId);

    List<WorkflowAssignmentDto> getByUserId(UUID userId);

    WorkflowAssignmentDto create(CreateWorkflowAssignmentRequest request);

    WorkflowAssignmentDto update(UUID id, UpdateWorkflowAssignmentRequest request);

    void softDelete(UUID id);
}
