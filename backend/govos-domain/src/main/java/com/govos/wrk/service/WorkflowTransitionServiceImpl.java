package com.govos.wrk.service;

import com.govos.wrk.dto.CreateWorkflowTransitionRequest;
import com.govos.wrk.dto.UpdateWorkflowTransitionRequest;
import com.govos.wrk.dto.WorkflowTransitionDto;
import com.govos.wrk.entity.WorkflowStep;
import com.govos.wrk.entity.WorkflowTransition;
import com.govos.wrk.exception.WorkflowStepNotFoundException;
import com.govos.wrk.exception.WorkflowTransitionNotFoundException;
import com.govos.wrk.mapper.WorkflowTransitionMapper;
import com.govos.wrk.repository.WorkflowStepRepository;
import com.govos.wrk.repository.WorkflowTransitionRepository;
import com.govos.wrk.validator.WorkflowTransitionValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WorkflowTransitionServiceImpl implements WorkflowTransitionService {

    private final WorkflowTransitionRepository workflowTransitionRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final WorkflowTransitionMapper workflowTransitionMapper;
    private final WorkflowTransitionValidator workflowTransitionValidator;

    public WorkflowTransitionServiceImpl(
            WorkflowTransitionRepository workflowTransitionRepository,
            WorkflowStepRepository workflowStepRepository,
            WorkflowTransitionMapper workflowTransitionMapper,
            WorkflowTransitionValidator workflowTransitionValidator) {
        this.workflowTransitionRepository = workflowTransitionRepository;
        this.workflowStepRepository = workflowStepRepository;
        this.workflowTransitionMapper = workflowTransitionMapper;
        this.workflowTransitionValidator = workflowTransitionValidator;
    }

    @Override
    public WorkflowTransitionDto getById(UUID id) {
        return workflowTransitionMapper.toDto(findActiveById(id));
    }

    @Override
    public List<WorkflowTransitionDto> getByFromStepId(UUID fromStepId) {
        return workflowTransitionRepository.findByFromStep_IdAndDeletedFalse(fromStepId).stream()
                .map(workflowTransitionMapper::toDto)
                .toList();
    }

    @Override
    public List<WorkflowTransitionDto> getByToStepId(UUID toStepId) {
        return workflowTransitionRepository.findByToStep_IdAndDeletedFalse(toStepId).stream()
                .map(workflowTransitionMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public WorkflowTransitionDto create(CreateWorkflowTransitionRequest request) {
        workflowTransitionValidator.validateCreate(request);

        WorkflowTransition entity = new WorkflowTransition();
        entity.setCode(request.code());
        entity.setFromStep(resolveStep(request.fromStepId()));
        entity.setToStep(resolveStep(request.toStepId()));
        entity.setConditionExpression(request.conditionExpression());
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return workflowTransitionMapper.toDto(workflowTransitionRepository.save(entity));
    }

    @Override
    @Transactional
    public WorkflowTransitionDto update(UUID id, UpdateWorkflowTransitionRequest request) {
        WorkflowTransition entity = findActiveById(id);
        assertVersion(entity, request.version());
        workflowTransitionValidator.validateUpdate(
                id, entity.getFromStep().getId(), entity.getToStep().getId(), request);

        entity.setCode(request.code());
        entity.setConditionExpression(request.conditionExpression());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return workflowTransitionMapper.toDto(workflowTransitionRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        WorkflowTransition entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        workflowTransitionRepository.save(entity);
    }

    private WorkflowStep resolveStep(UUID stepId) {
        return workflowStepRepository.findByIdAndDeletedFalse(stepId)
                .orElseThrow(() -> new WorkflowStepNotFoundException(stepId));
    }

    private WorkflowTransition findActiveById(UUID id) {
        return workflowTransitionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new WorkflowTransitionNotFoundException(id));
    }

    private void assertVersion(WorkflowTransition entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "WorkflowTransition version mismatch for id: " + entity.getId());
        }
    }
}
