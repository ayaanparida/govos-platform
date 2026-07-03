package com.govos.wrk.service;

import com.govos.wrk.dto.CreateWorkflowVersionRequest;
import com.govos.wrk.dto.UpdateWorkflowVersionRequest;
import com.govos.wrk.dto.WorkflowVersionDto;

import java.util.List;
import java.util.UUID;

public interface WorkflowVersionService {

    WorkflowVersionDto getById(UUID id);

    List<WorkflowVersionDto> getByDefinitionId(UUID definitionId);

    WorkflowVersionDto getPublishedByDefinitionId(UUID definitionId);

    WorkflowVersionDto create(CreateWorkflowVersionRequest request);

    WorkflowVersionDto update(UUID id, UpdateWorkflowVersionRequest request);

    void softDelete(UUID id);
}
