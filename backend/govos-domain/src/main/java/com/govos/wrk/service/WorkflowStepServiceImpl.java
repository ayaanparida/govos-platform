package com.govos.wrk.service;

import com.govos.wrk.dto.CreateWorkflowStepRequest;
import com.govos.wrk.dto.UpdateWorkflowStepRequest;
import com.govos.wrk.dto.WorkflowStepDto;
import com.govos.wrk.entity.WorkflowStep;
import com.govos.wrk.entity.WorkflowVersion;
import com.govos.wrk.exception.WorkflowStepNotFoundException;
import com.govos.wrk.exception.WorkflowVersionNotFoundException;
import com.govos.wrk.mapper.WorkflowStepMapper;
import com.govos.wrk.repository.WorkflowStepRepository;
import com.govos.wrk.repository.WorkflowVersionRepository;
import com.govos.wrk.validator.WorkflowStepValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WorkflowStepServiceImpl implements WorkflowStepService {

    private final WorkflowStepRepository workflowStepRepository;
    private final WorkflowVersionRepository workflowVersionRepository;
    private final WorkflowStepMapper workflowStepMapper;
    private final WorkflowStepValidator workflowStepValidator;

    public WorkflowStepServiceImpl(
            WorkflowStepRepository workflowStepRepository,
            WorkflowVersionRepository workflowVersionRepository,
            WorkflowStepMapper workflowStepMapper,
            WorkflowStepValidator workflowStepValidator) {
        this.workflowStepRepository = workflowStepRepository;
        this.workflowVersionRepository = workflowVersionRepository;
        this.workflowStepMapper = workflowStepMapper;
        this.workflowStepValidator = workflowStepValidator;
    }

    @Override
    public WorkflowStepDto getById(UUID id) {
        return workflowStepMapper.toDto(findActiveById(id));
    }

    @Override
    public List<WorkflowStepDto> getByWorkflowVersionId(UUID workflowVersionId) {
        return workflowStepRepository
                .findByWorkflowVersion_IdAndDeletedFalseOrderBySequenceNumberAsc(workflowVersionId).stream()
                .map(workflowStepMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public WorkflowStepDto create(CreateWorkflowStepRequest request) {
        workflowStepValidator.validateCreate(request);

        WorkflowStep entity = new WorkflowStep();
        entity.setCode(request.code());
        entity.setWorkflowVersion(resolveWorkflowVersion(request.workflowVersionId()));
        entity.setStepName(request.stepName());
        entity.setStepType(request.stepType());
        entity.setSequenceNumber(request.sequenceNumber());
        entity.setSlaHours(request.slaHours());
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return workflowStepMapper.toDto(workflowStepRepository.save(entity));
    }

    @Override
    @Transactional
    public WorkflowStepDto update(UUID id, UpdateWorkflowStepRequest request) {
        WorkflowStep entity = findActiveById(id);
        assertVersion(entity, request.version());
        workflowStepValidator.validateUpdate(id, entity.getWorkflowVersion().getId(), request);

        entity.setCode(request.code());
        entity.setStepName(request.stepName());
        entity.setStepType(request.stepType());
        entity.setSequenceNumber(request.sequenceNumber());
        entity.setSlaHours(request.slaHours());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return workflowStepMapper.toDto(workflowStepRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        WorkflowStep entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        workflowStepRepository.save(entity);
    }

    private WorkflowVersion resolveWorkflowVersion(UUID workflowVersionId) {
        return workflowVersionRepository.findByIdAndDeletedFalse(workflowVersionId)
                .orElseThrow(() -> new WorkflowVersionNotFoundException(workflowVersionId));
    }

    private WorkflowStep findActiveById(UUID id) {
        return workflowStepRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new WorkflowStepNotFoundException(id));
    }

    private void assertVersion(WorkflowStep entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "WorkflowStep version mismatch for id: " + entity.getId());
        }
    }
}
