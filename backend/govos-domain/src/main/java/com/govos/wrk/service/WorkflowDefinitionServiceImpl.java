package com.govos.wrk.service;

import com.govos.wrk.dto.CreateWorkflowDefinitionRequest;
import com.govos.wrk.dto.UpdateWorkflowDefinitionRequest;
import com.govos.wrk.dto.WorkflowDefinitionDto;
import com.govos.wrk.entity.WorkflowDefinition;
import com.govos.wrk.exception.WorkflowDefinitionNotFoundException;
import com.govos.wrk.mapper.WorkflowDefinitionMapper;
import com.govos.wrk.repository.WorkflowDefinitionRepository;
import com.govos.wrk.validator.WorkflowDefinitionValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WorkflowDefinitionServiceImpl implements WorkflowDefinitionService {

    private final WorkflowDefinitionRepository workflowDefinitionRepository;
    private final WorkflowDefinitionMapper workflowDefinitionMapper;
    private final WorkflowDefinitionValidator workflowDefinitionValidator;

    public WorkflowDefinitionServiceImpl(
            WorkflowDefinitionRepository workflowDefinitionRepository,
            WorkflowDefinitionMapper workflowDefinitionMapper,
            WorkflowDefinitionValidator workflowDefinitionValidator) {
        this.workflowDefinitionRepository = workflowDefinitionRepository;
        this.workflowDefinitionMapper = workflowDefinitionMapper;
        this.workflowDefinitionValidator = workflowDefinitionValidator;
    }

    @Override
    public WorkflowDefinitionDto getById(UUID id) {
        return workflowDefinitionMapper.toDto(findActiveById(id));
    }

    @Override
    public WorkflowDefinitionDto getByCode(String code) {
        return workflowDefinitionMapper.toDto(findActiveByCode(code));
    }

    @Override
    public List<WorkflowDefinitionDto> getAll() {
        return workflowDefinitionRepository.findByDeletedFalseOrderByNameAsc().stream()
                .map(workflowDefinitionMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public WorkflowDefinitionDto create(CreateWorkflowDefinitionRequest request) {
        workflowDefinitionValidator.validateCreate(request);

        WorkflowDefinition entity = new WorkflowDefinition();
        entity.setCode(request.code());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return workflowDefinitionMapper.toDto(workflowDefinitionRepository.save(entity));
    }

    @Override
    @Transactional
    public WorkflowDefinitionDto update(UUID id, UpdateWorkflowDefinitionRequest request) {
        WorkflowDefinition entity = findActiveById(id);
        assertVersion(entity, request.version());
        workflowDefinitionValidator.validateUpdate(id, request);

        entity.setCode(request.code());
        entity.setName(request.name());
        entity.setDescription(request.description());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return workflowDefinitionMapper.toDto(workflowDefinitionRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        WorkflowDefinition entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        workflowDefinitionRepository.save(entity);
    }

    private WorkflowDefinition findActiveById(UUID id) {
        return workflowDefinitionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new WorkflowDefinitionNotFoundException(id));
    }

    private WorkflowDefinition findActiveByCode(String code) {
        return workflowDefinitionRepository.findByCodeAndDeletedFalse(code)
                .orElseThrow(() -> new WorkflowDefinitionNotFoundException(code));
    }

    private void assertVersion(WorkflowDefinition entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "WorkflowDefinition version mismatch for id: " + entity.getId());
        }
    }
}
