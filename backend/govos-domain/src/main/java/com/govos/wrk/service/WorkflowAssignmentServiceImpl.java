package com.govos.wrk.service;

import com.govos.idm.entity.User;
import com.govos.idm.exception.UserNotFoundException;
import com.govos.idm.repository.UserRepository;
import com.govos.wrk.dto.CreateWorkflowAssignmentRequest;
import com.govos.wrk.dto.UpdateWorkflowAssignmentRequest;
import com.govos.wrk.dto.WorkflowAssignmentDto;
import com.govos.wrk.entity.WorkflowAssignment;
import com.govos.wrk.entity.WorkflowTask;
import com.govos.wrk.exception.WorkflowAssignmentNotFoundException;
import com.govos.wrk.exception.WorkflowTaskNotFoundException;
import com.govos.wrk.mapper.WorkflowAssignmentMapper;
import com.govos.wrk.repository.WorkflowAssignmentRepository;
import com.govos.wrk.repository.WorkflowTaskRepository;
import com.govos.wrk.validator.WorkflowAssignmentValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WorkflowAssignmentServiceImpl implements WorkflowAssignmentService {

    private final WorkflowAssignmentRepository workflowAssignmentRepository;
    private final WorkflowTaskRepository workflowTaskRepository;
    private final UserRepository userRepository;
    private final WorkflowAssignmentMapper workflowAssignmentMapper;
    private final WorkflowAssignmentValidator workflowAssignmentValidator;

    public WorkflowAssignmentServiceImpl(
            WorkflowAssignmentRepository workflowAssignmentRepository,
            WorkflowTaskRepository workflowTaskRepository,
            UserRepository userRepository,
            WorkflowAssignmentMapper workflowAssignmentMapper,
            WorkflowAssignmentValidator workflowAssignmentValidator) {
        this.workflowAssignmentRepository = workflowAssignmentRepository;
        this.workflowTaskRepository = workflowTaskRepository;
        this.userRepository = userRepository;
        this.workflowAssignmentMapper = workflowAssignmentMapper;
        this.workflowAssignmentValidator = workflowAssignmentValidator;
    }

    @Override
    public WorkflowAssignmentDto getById(UUID id) {
        return workflowAssignmentMapper.toDto(findActiveById(id));
    }

    @Override
    public List<WorkflowAssignmentDto> getByWorkflowTaskId(UUID workflowTaskId) {
        return workflowAssignmentRepository.findByWorkflowTask_IdAndDeletedFalse(workflowTaskId).stream()
                .map(workflowAssignmentMapper::toDto)
                .toList();
    }

    @Override
    public List<WorkflowAssignmentDto> getByUserId(UUID userId) {
        return workflowAssignmentRepository.findByUser_IdAndDeletedFalse(userId).stream()
                .map(workflowAssignmentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public WorkflowAssignmentDto create(CreateWorkflowAssignmentRequest request) {
        workflowAssignmentValidator.validateCreate(request);

        WorkflowAssignment entity = new WorkflowAssignment();
        entity.setCode(request.code());
        entity.setWorkflowTask(resolveWorkflowTask(request.workflowTaskId()));
        entity.setUser(resolveUser(request.userId()));
        entity.setAssignedDate(request.assignedDate());
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return workflowAssignmentMapper.toDto(workflowAssignmentRepository.save(entity));
    }

    @Override
    @Transactional
    public WorkflowAssignmentDto update(UUID id, UpdateWorkflowAssignmentRequest request) {
        WorkflowAssignment entity = findActiveById(id);
        assertVersion(entity, request.version());
        workflowAssignmentValidator.validateUpdate(
                id, entity.getWorkflowTask().getId(), entity.getUser().getId(), request);

        entity.setCode(request.code());
        entity.setAssignedDate(request.assignedDate());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return workflowAssignmentMapper.toDto(workflowAssignmentRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        WorkflowAssignment entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        workflowAssignmentRepository.save(entity);
    }

    private WorkflowTask resolveWorkflowTask(UUID workflowTaskId) {
        return workflowTaskRepository.findByIdAndDeletedFalse(workflowTaskId)
                .orElseThrow(() -> new WorkflowTaskNotFoundException(workflowTaskId));
    }

    private User resolveUser(UUID userId) {
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private WorkflowAssignment findActiveById(UUID id) {
        return workflowAssignmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new WorkflowAssignmentNotFoundException(id));
    }

    private void assertVersion(WorkflowAssignment entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "WorkflowAssignment version mismatch for id: " + entity.getId());
        }
    }
}
