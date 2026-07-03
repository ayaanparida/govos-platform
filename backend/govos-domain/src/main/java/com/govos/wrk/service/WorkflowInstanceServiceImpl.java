package com.govos.wrk.service;

import com.govos.wrk.dto.CreateWorkflowInstanceRequest;
import com.govos.wrk.dto.UpdateWorkflowInstanceRequest;
import com.govos.wrk.dto.WorkflowInstanceDto;
import com.govos.wrk.entity.WorkflowInstance;
import com.govos.wrk.entity.WorkflowInstanceStatus;
import com.govos.wrk.entity.WorkflowVersion;
import com.govos.wrk.exception.WorkflowInstanceNotFoundException;
import com.govos.wrk.exception.WorkflowVersionNotFoundException;
import com.govos.wrk.mapper.WorkflowInstanceMapper;
import com.govos.wrk.repository.WorkflowInstanceRepository;
import com.govos.wrk.repository.WorkflowVersionRepository;
import com.govos.wrk.validator.WorkflowInstanceValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WorkflowInstanceServiceImpl implements WorkflowInstanceService {

    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final WorkflowVersionRepository workflowVersionRepository;
    private final WorkflowInstanceMapper workflowInstanceMapper;
    private final WorkflowInstanceValidator workflowInstanceValidator;

    public WorkflowInstanceServiceImpl(
            WorkflowInstanceRepository workflowInstanceRepository,
            WorkflowVersionRepository workflowVersionRepository,
            WorkflowInstanceMapper workflowInstanceMapper,
            WorkflowInstanceValidator workflowInstanceValidator) {
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.workflowVersionRepository = workflowVersionRepository;
        this.workflowInstanceMapper = workflowInstanceMapper;
        this.workflowInstanceValidator = workflowInstanceValidator;
    }

    @Override
    public WorkflowInstanceDto getById(UUID id) {
        return workflowInstanceMapper.toDto(findActiveById(id));
    }

    @Override
    public List<WorkflowInstanceDto> getByReference(String referenceType, UUID referenceId) {
        return workflowInstanceRepository
                .findByReferenceTypeAndReferenceIdAndDeletedFalseOrderByCreatedDateDesc(
                        referenceType, referenceId).stream()
                .map(workflowInstanceMapper::toDto)
                .toList();
    }

    @Override
    public List<WorkflowInstanceDto> getByStatus(WorkflowInstanceStatus status) {
        return workflowInstanceRepository.findByStatusAndDeletedFalseOrderByCreatedDateDesc(status).stream()
                .map(workflowInstanceMapper::toDto)
                .toList();
    }

    @Override
    public List<WorkflowInstanceDto> getByWorkflowVersionId(UUID workflowVersionId) {
        return workflowInstanceRepository
                .findByWorkflowVersion_IdAndDeletedFalseOrderByCreatedDateDesc(workflowVersionId).stream()
                .map(workflowInstanceMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public WorkflowInstanceDto create(CreateWorkflowInstanceRequest request) {
        workflowInstanceValidator.validateCreate(request);

        WorkflowInstance entity = new WorkflowInstance();
        entity.setCode(resolveInstanceCode(request));
        entity.setWorkflowVersion(resolveWorkflowVersion(request.workflowVersionId()));
        entity.setReferenceType(request.referenceType());
        entity.setReferenceId(request.referenceId());
        entity.setStatus(request.status() != null ? request.status() : WorkflowInstanceStatus.PENDING);
        entity.setStartedAt(request.startedAt());
        entity.setCompletedAt(request.completedAt());
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return workflowInstanceMapper.toDto(workflowInstanceRepository.save(entity));
    }

    @Override
    @Transactional
    public WorkflowInstanceDto update(UUID id, UpdateWorkflowInstanceRequest request) {
        WorkflowInstance entity = findActiveById(id);
        assertVersion(entity, request.version());
        workflowInstanceValidator.validateUpdate(id, request);

        if (request.code() != null) {
            entity.setCode(request.code());
        }
        entity.setReferenceType(request.referenceType());
        entity.setReferenceId(request.referenceId());
        if (request.status() != null) {
            entity.setStatus(request.status());
        }
        entity.setStartedAt(request.startedAt());
        entity.setCompletedAt(request.completedAt());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return workflowInstanceMapper.toDto(workflowInstanceRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        WorkflowInstance entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        workflowInstanceRepository.save(entity);
    }

    private String resolveInstanceCode(CreateWorkflowInstanceRequest request) {
        if (StringUtils.hasText(request.code())) {
            return request.code();
        }
        return "WFI-" + request.referenceType().replaceAll("[^A-Z0-9]", "")
                + "-" + request.referenceId().toString().substring(0, 8).toUpperCase();
    }

    private WorkflowVersion resolveWorkflowVersion(UUID workflowVersionId) {
        return workflowVersionRepository.findByIdAndDeletedFalse(workflowVersionId)
                .orElseThrow(() -> new WorkflowVersionNotFoundException(workflowVersionId));
    }

    private WorkflowInstance findActiveById(UUID id) {
        return workflowInstanceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new WorkflowInstanceNotFoundException(id));
    }

    private void assertVersion(WorkflowInstance entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "WorkflowInstance version mismatch for id: " + entity.getId());
        }
    }
}
