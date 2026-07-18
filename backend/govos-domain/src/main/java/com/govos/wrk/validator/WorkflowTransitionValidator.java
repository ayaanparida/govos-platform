package com.govos.wrk.validator;

import com.govos.wrk.dto.CreateWorkflowTransitionRequest;
import com.govos.wrk.dto.UpdateWorkflowTransitionRequest;
import com.govos.wrk.entity.WorkflowStep;
import com.govos.wrk.exception.InvalidWorkflowException;
import com.govos.wrk.exception.WorkflowStepNotFoundException;
import com.govos.wrk.repository.WorkflowStepRepository;
import com.govos.wrk.repository.WorkflowTransitionRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WorkflowTransitionValidator {

    private final WorkflowTransitionRepository workflowTransitionRepository;
    private final WorkflowStepRepository workflowStepRepository;

    public WorkflowTransitionValidator(
            WorkflowTransitionRepository workflowTransitionRepository,
            WorkflowStepRepository workflowStepRepository) {
        this.workflowTransitionRepository = workflowTransitionRepository;
        this.workflowStepRepository = workflowStepRepository;
    }

    public void validateCreate(CreateWorkflowTransitionRequest request) {
        validateSteps(request.fromStepId(), request.toStepId());
        if (workflowTransitionRepository.existsByFromStep_IdAndToStep_IdAndDeletedFalse(
                request.fromStepId(), request.toStepId())) {
            throw new InvalidWorkflowException(
                    "Workflow transition already exists from step " + request.fromStepId()
                            + " to step " + request.toStepId());
        }
    }

    public void validateUpdate(
            UUID id, UUID fromStepId, UUID toStepId, UpdateWorkflowTransitionRequest request) {
        validateSteps(fromStepId, toStepId);
        workflowTransitionRepository
                .findByFromStep_IdAndToStep_IdAndDeletedFalse(fromStepId, toStepId)
                .filter(entity -> !entity.getId().equals(id))
                .ifPresent(entity -> {
                    throw new InvalidWorkflowException(
                            "Workflow transition already exists from step " + fromStepId
                                    + " to step " + toStepId);
                });
    }

    private void validateSteps(UUID fromStepId, UUID toStepId) {
        if (fromStepId.equals(toStepId)) {
            throw new InvalidWorkflowException("Workflow transition from and to steps must differ");
        }

        WorkflowStep fromStep = workflowStepRepository.findByIdAndDeletedFalse(fromStepId)
                .orElseThrow(() -> new WorkflowStepNotFoundException(fromStepId));
        WorkflowStep toStep = workflowStepRepository.findByIdAndDeletedFalse(toStepId)
                .orElseThrow(() -> new WorkflowStepNotFoundException(toStepId));

        if (!fromStep.getWorkflowVersion().getId().equals(toStep.getWorkflowVersion().getId())) {
            throw new InvalidWorkflowException(
                    "Workflow transition steps must belong to the same workflow version");
        }
    }
}
