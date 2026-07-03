package com.govos.wrk.service;

import com.govos.wrk.dto.CreateWorkflowStepRequest;
import com.govos.wrk.dto.UpdateWorkflowStepRequest;
import com.govos.wrk.dto.WorkflowStepDto;

import java.util.List;
import java.util.UUID;

public interface WorkflowStepService {

    WorkflowStepDto getById(UUID id);

    List<WorkflowStepDto> getByWorkflowVersionId(UUID workflowVersionId);

    WorkflowStepDto create(CreateWorkflowStepRequest request);

    WorkflowStepDto update(UUID id, UpdateWorkflowStepRequest request);

    void softDelete(UUID id);
}
