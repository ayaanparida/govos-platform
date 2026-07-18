package com.govos.wrk.validator;

import com.govos.wrk.dto.CreateWorkflowAssignmentRequest;
import com.govos.wrk.dto.UpdateWorkflowAssignmentRequest;
import com.govos.wrk.exception.DuplicateAssignmentException;
import com.govos.wrk.repository.WorkflowAssignmentRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WorkflowAssignmentValidator {

    private final WorkflowAssignmentRepository workflowAssignmentRepository;

    public WorkflowAssignmentValidator(WorkflowAssignmentRepository workflowAssignmentRepository) {
        this.workflowAssignmentRepository = workflowAssignmentRepository;
    }

    public void validateCreate(CreateWorkflowAssignmentRequest request) {
        if (workflowAssignmentRepository.existsByWorkflowTask_IdAndUser_IdAndDeletedFalse(
                request.workflowTaskId(), request.userId())) {
            throw new DuplicateAssignmentException(
                    "Workflow assignment already exists for task=" + request.workflowTaskId()
                            + ", user=" + request.userId());
        }
    }

    public void validateUpdate(UUID id, UUID workflowTaskId, UUID userId, UpdateWorkflowAssignmentRequest request) {
        workflowAssignmentRepository
                .findByWorkflowTask_IdAndUser_IdAndDeletedFalse(workflowTaskId, userId)
                .filter(entity -> !entity.getId().equals(id))
                .ifPresent(entity -> {
                    throw new DuplicateAssignmentException(
                            "Workflow assignment already exists for task=" + workflowTaskId
                                    + ", user=" + userId);
                });
    }
}
