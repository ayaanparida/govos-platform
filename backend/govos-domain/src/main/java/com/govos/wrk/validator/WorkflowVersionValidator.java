package com.govos.wrk.validator;

import com.govos.wrk.dto.CreateWorkflowVersionRequest;
import com.govos.wrk.dto.UpdateWorkflowVersionRequest;
import com.govos.wrk.exception.InvalidWorkflowException;
import com.govos.wrk.repository.WorkflowVersionRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WorkflowVersionValidator {

    private final WorkflowVersionRepository workflowVersionRepository;

    public WorkflowVersionValidator(WorkflowVersionRepository workflowVersionRepository) {
        this.workflowVersionRepository = workflowVersionRepository;
    }

    public void validateCreate(CreateWorkflowVersionRequest request) {
        if (workflowVersionRepository.existsByDefinition_IdAndVersionNumberAndDeletedFalse(
                request.definitionId(), request.versionNumber())) {
            throw new InvalidWorkflowException(
                    "Workflow version number already exists for definition: " + request.definitionId()
                            + ", versionNumber=" + request.versionNumber());
        }
    }

    public void validateUpdate(UUID id, UUID definitionId, UpdateWorkflowVersionRequest request) {
        workflowVersionRepository
                .findByDefinition_IdAndVersionNumberAndDeletedFalse(definitionId, request.versionNumber())
                .filter(entity -> !entity.getId().equals(id))
                .ifPresent(entity -> {
                    throw new InvalidWorkflowException(
                            "Workflow version number already exists for definition: " + definitionId
                                    + ", versionNumber=" + request.versionNumber());
                });
    }
}
