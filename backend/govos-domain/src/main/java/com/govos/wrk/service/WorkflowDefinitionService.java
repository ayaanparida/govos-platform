package com.govos.wrk.service;

import com.govos.wrk.dto.CreateWorkflowDefinitionRequest;
import com.govos.wrk.dto.UpdateWorkflowDefinitionRequest;
import com.govos.wrk.dto.WorkflowDefinitionDto;

import java.util.List;
import java.util.UUID;

public interface WorkflowDefinitionService {

    WorkflowDefinitionDto getById(UUID id);

    WorkflowDefinitionDto getByCode(String code);

    List<WorkflowDefinitionDto> getAll();

    WorkflowDefinitionDto create(CreateWorkflowDefinitionRequest request);

    WorkflowDefinitionDto update(UUID id, UpdateWorkflowDefinitionRequest request);

    void softDelete(UUID id);
}
