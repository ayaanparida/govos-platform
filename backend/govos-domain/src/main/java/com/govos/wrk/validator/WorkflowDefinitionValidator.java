package com.govos.wrk.validator;

import com.govos.wrk.dto.CreateWorkflowDefinitionRequest;
import com.govos.wrk.dto.UpdateWorkflowDefinitionRequest;
import com.govos.wrk.exception.DuplicateCodeException;
import com.govos.wrk.repository.WorkflowDefinitionRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WorkflowDefinitionValidator {

    private final WorkflowDefinitionRepository workflowDefinitionRepository;

    public WorkflowDefinitionValidator(WorkflowDefinitionRepository workflowDefinitionRepository) {
        this.workflowDefinitionRepository = workflowDefinitionRepository;
    }

    public void validateCreate(CreateWorkflowDefinitionRequest request) {
        if (workflowDefinitionRepository.existsByCodeAndDeletedFalse(request.code())) {
            throw new DuplicateCodeException("WorkflowDefinition", request.code());
        }
    }

    public void validateUpdate(UUID id, UpdateWorkflowDefinitionRequest request) {
        workflowDefinitionRepository.findByCodeAndDeletedFalse(request.code())
                .filter(entity -> !entity.getId().equals(id))
                .ifPresent(entity -> {
                    throw new DuplicateCodeException("WorkflowDefinition", request.code());
                });
    }
}
