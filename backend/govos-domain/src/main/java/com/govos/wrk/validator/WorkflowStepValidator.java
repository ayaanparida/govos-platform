package com.govos.wrk.validator;

import com.govos.wrk.dto.CreateWorkflowStepRequest;
import com.govos.wrk.dto.UpdateWorkflowStepRequest;
import com.govos.wrk.exception.InvalidWorkflowException;
import com.govos.wrk.repository.WorkflowStepRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WorkflowStepValidator {

    private final WorkflowStepRepository workflowStepRepository;

    public WorkflowStepValidator(WorkflowStepRepository workflowStepRepository) {
        this.workflowStepRepository = workflowStepRepository;
    }

    public void validateCreate(CreateWorkflowStepRequest request) {
        if (workflowStepRepository.existsByWorkflowVersion_IdAndSequenceNumberAndDeletedFalse(
                request.workflowVersionId(), request.sequenceNumber())) {
            throw new InvalidWorkflowException(
                    "Workflow step sequence number already exists for version: " + request.workflowVersionId()
                            + ", sequenceNumber=" + request.sequenceNumber());
        }
    }

    public void validateUpdate(UUID id, UUID workflowVersionId, UpdateWorkflowStepRequest request) {
        workflowStepRepository
                .findByWorkflowVersion_IdAndSequenceNumberAndDeletedFalse(
                        workflowVersionId, request.sequenceNumber())
                .filter(entity -> !entity.getId().equals(id))
                .ifPresent(entity -> {
                    throw new InvalidWorkflowException(
                            "Workflow step sequence number already exists for version: " + workflowVersionId
                                    + ", sequenceNumber=" + request.sequenceNumber());
                });
    }
}
