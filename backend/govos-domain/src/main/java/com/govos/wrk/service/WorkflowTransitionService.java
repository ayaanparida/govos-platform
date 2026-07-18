package com.govos.wrk.service;

import com.govos.wrk.dto.CreateWorkflowTransitionRequest;
import com.govos.wrk.dto.UpdateWorkflowTransitionRequest;
import com.govos.wrk.dto.WorkflowTransitionDto;

import java.util.List;
import java.util.UUID;

public interface WorkflowTransitionService {

    WorkflowTransitionDto getById(UUID id);

    List<WorkflowTransitionDto> getByFromStepId(UUID fromStepId);

    List<WorkflowTransitionDto> getByToStepId(UUID toStepId);

    WorkflowTransitionDto create(CreateWorkflowTransitionRequest request);

    WorkflowTransitionDto update(UUID id, UpdateWorkflowTransitionRequest request);

    void softDelete(UUID id);
}
