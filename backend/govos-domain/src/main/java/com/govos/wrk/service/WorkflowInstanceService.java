package com.govos.wrk.service;

import com.govos.wrk.dto.CreateWorkflowInstanceRequest;
import com.govos.wrk.dto.UpdateWorkflowInstanceRequest;
import com.govos.wrk.dto.WorkflowInstanceDto;
import com.govos.wrk.entity.WorkflowInstanceStatus;

import java.util.List;
import java.util.UUID;

public interface WorkflowInstanceService {

    WorkflowInstanceDto getById(UUID id);

    List<WorkflowInstanceDto> getByReference(String referenceType, UUID referenceId);

    List<WorkflowInstanceDto> getByStatus(WorkflowInstanceStatus status);

    List<WorkflowInstanceDto> getByWorkflowVersionId(UUID workflowVersionId);

    WorkflowInstanceDto create(CreateWorkflowInstanceRequest request);

    WorkflowInstanceDto update(UUID id, UpdateWorkflowInstanceRequest request);

    void softDelete(UUID id);
}
