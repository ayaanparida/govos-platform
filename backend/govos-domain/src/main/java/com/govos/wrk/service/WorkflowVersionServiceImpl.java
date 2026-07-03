package com.govos.wrk.service;

import com.govos.wrk.dto.CreateWorkflowVersionRequest;
import com.govos.wrk.dto.UpdateWorkflowVersionRequest;
import com.govos.wrk.dto.WorkflowVersionDto;
import com.govos.wrk.entity.WorkflowDefinition;
import com.govos.wrk.entity.WorkflowVersion;
import com.govos.wrk.exception.InvalidWorkflowException;
import com.govos.wrk.exception.WorkflowDefinitionNotFoundException;
import com.govos.wrk.exception.WorkflowVersionNotFoundException;
import com.govos.wrk.mapper.WorkflowVersionMapper;
import com.govos.wrk.repository.WorkflowDefinitionRepository;
import com.govos.wrk.repository.WorkflowVersionRepository;
import com.govos.wrk.validator.WorkflowVersionValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WorkflowVersionServiceImpl implements WorkflowVersionService {

    private final WorkflowVersionRepository workflowVersionRepository;
    private final WorkflowDefinitionRepository workflowDefinitionRepository;
    private final WorkflowVersionMapper workflowVersionMapper;
    private final WorkflowVersionValidator workflowVersionValidator;

    public WorkflowVersionServiceImpl(
            WorkflowVersionRepository workflowVersionRepository,
            WorkflowDefinitionRepository workflowDefinitionRepository,
            WorkflowVersionMapper workflowVersionMapper,
            WorkflowVersionValidator workflowVersionValidator) {
        this.workflowVersionRepository = workflowVersionRepository;
        this.workflowDefinitionRepository = workflowDefinitionRepository;
        this.workflowVersionMapper = workflowVersionMapper;
        this.workflowVersionValidator = workflowVersionValidator;
    }

    @Override
    public WorkflowVersionDto getById(UUID id) {
        return workflowVersionMapper.toDto(findActiveById(id));
    }

    @Override
    public List<WorkflowVersionDto> getByDefinitionId(UUID definitionId) {
        return workflowVersionRepository
                .findByDefinition_IdAndDeletedFalseOrderByVersionNumberDesc(definitionId).stream()
                .map(workflowVersionMapper::toDto)
                .toList();
    }

    @Override
    public WorkflowVersionDto getPublishedByDefinitionId(UUID definitionId) {
        return workflowVersionRepository.findByDefinition_IdAndPublishedTrueAndDeletedFalse(definitionId)
                .map(workflowVersionMapper::toDto)
                .orElseThrow(() -> new InvalidWorkflowException(
                        "No published workflow version found for definition: " + definitionId));
    }

    @Override
    @Transactional
    public WorkflowVersionDto create(CreateWorkflowVersionRequest request) {
        workflowVersionValidator.validateCreate(request);

        WorkflowVersion entity = new WorkflowVersion();
        entity.setCode(request.code());
        entity.setDefinition(resolveDefinition(request.definitionId()));
        entity.setVersionNumber(request.versionNumber());
        entity.setPublished(request.published() != null ? request.published() : false);
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return workflowVersionMapper.toDto(workflowVersionRepository.save(entity));
    }

    @Override
    @Transactional
    public WorkflowVersionDto update(UUID id, UpdateWorkflowVersionRequest request) {
        WorkflowVersion entity = findActiveById(id);
        assertVersion(entity, request.version());
        workflowVersionValidator.validateUpdate(id, entity.getDefinition().getId(), request);

        entity.setCode(request.code());
        entity.setVersionNumber(request.versionNumber());
        if (request.published() != null) {
            entity.setPublished(request.published());
        }
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return workflowVersionMapper.toDto(workflowVersionRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        WorkflowVersion entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        workflowVersionRepository.save(entity);
    }

    private WorkflowDefinition resolveDefinition(UUID definitionId) {
        return workflowDefinitionRepository.findByIdAndDeletedFalse(definitionId)
                .orElseThrow(() -> new WorkflowDefinitionNotFoundException(definitionId));
    }

    private WorkflowVersion findActiveById(UUID id) {
        return workflowVersionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new WorkflowVersionNotFoundException(id));
    }

    private void assertVersion(WorkflowVersion entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "WorkflowVersion version mismatch for id: " + entity.getId());
        }
    }
}
