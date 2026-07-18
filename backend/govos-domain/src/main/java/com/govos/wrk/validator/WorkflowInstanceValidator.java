package com.govos.wrk.validator;

import com.govos.wrk.dto.CreateWorkflowInstanceRequest;
import com.govos.wrk.dto.UpdateWorkflowInstanceRequest;
import com.govos.wrk.exception.DuplicateCodeException;
import com.govos.wrk.repository.WorkflowInstanceRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Component
public class WorkflowInstanceValidator {

    private final WorkflowInstanceRepository workflowInstanceRepository;

    public WorkflowInstanceValidator(WorkflowInstanceRepository workflowInstanceRepository) {
        this.workflowInstanceRepository = workflowInstanceRepository;
    }

    public void validateCreate(CreateWorkflowInstanceRequest request) {
        if (StringUtils.hasText(request.code())
                && workflowInstanceRepository.existsByCodeAndDeletedFalse(request.code())) {
            throw new DuplicateCodeException("WorkflowInstance", request.code());
        }
    }

    public void validateUpdate(UUID id, UpdateWorkflowInstanceRequest request) {
        if (StringUtils.hasText(request.code())) {
            workflowInstanceRepository.findByCodeAndDeletedFalse(request.code())
                    .filter(entity -> !entity.getId().equals(id))
                    .ifPresent(entity -> {
                        throw new DuplicateCodeException("WorkflowInstance", request.code());
                    });
        }
    }
}
