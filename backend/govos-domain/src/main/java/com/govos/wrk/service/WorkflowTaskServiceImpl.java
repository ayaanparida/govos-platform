package com.govos.wrk.service;

import com.govos.idm.entity.Role;
import com.govos.idm.entity.User;
import com.govos.idm.exception.RoleNotFoundException;
import com.govos.idm.exception.UserNotFoundException;
import com.govos.idm.repository.RoleRepository;
import com.govos.idm.repository.UserRepository;
import com.govos.wrk.dto.CreateWorkflowTaskRequest;
import com.govos.wrk.dto.UpdateWorkflowTaskRequest;
import com.govos.wrk.dto.WorkflowTaskDto;
import com.govos.wrk.entity.WorkflowInstance;
import com.govos.wrk.entity.WorkflowStep;
import com.govos.wrk.entity.WorkflowTask;
import com.govos.wrk.entity.WorkflowTaskStatus;
import com.govos.wrk.exception.WorkflowInstanceNotFoundException;
import com.govos.wrk.exception.WorkflowStepNotFoundException;
import com.govos.wrk.exception.WorkflowTaskNotFoundException;
import com.govos.wrk.mapper.WorkflowTaskMapper;
import com.govos.wrk.repository.WorkflowInstanceRepository;
import com.govos.wrk.repository.WorkflowStepRepository;
import com.govos.wrk.repository.WorkflowTaskRepository;
import com.govos.wrk.validator.WorkflowTaskValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WorkflowTaskServiceImpl implements WorkflowTaskService {

    private final WorkflowTaskRepository workflowTaskRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final WorkflowTaskMapper workflowTaskMapper;
    private final WorkflowTaskValidator workflowTaskValidator;

    public WorkflowTaskServiceImpl(
            WorkflowTaskRepository workflowTaskRepository,
            WorkflowInstanceRepository workflowInstanceRepository,
            WorkflowStepRepository workflowStepRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            WorkflowTaskMapper workflowTaskMapper,
            WorkflowTaskValidator workflowTaskValidator) {
        this.workflowTaskRepository = workflowTaskRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.workflowStepRepository = workflowStepRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.workflowTaskMapper = workflowTaskMapper;
        this.workflowTaskValidator = workflowTaskValidator;
    }

    @Override
    public WorkflowTaskDto getById(UUID id) {
        return workflowTaskMapper.toDto(findActiveById(id));
    }

    @Override
    public List<WorkflowTaskDto> getByWorkflowInstanceId(UUID workflowInstanceId) {
        return workflowTaskRepository
                .findByWorkflowInstance_IdAndDeletedFalseOrderByCreatedDateAsc(workflowInstanceId).stream()
                .map(workflowTaskMapper::toDto)
                .toList();
    }

    @Override
    public List<WorkflowTaskDto> getByAssignedToId(UUID assignedToId) {
        return workflowTaskRepository.findByAssignedTo_IdAndDeletedFalseOrderByCreatedDateDesc(assignedToId).stream()
                .map(workflowTaskMapper::toDto)
                .toList();
    }

    @Override
    public List<WorkflowTaskDto> getByStatus(WorkflowTaskStatus status) {
        return workflowTaskRepository.findByStatusAndDeletedFalseOrderByCreatedDateDesc(status).stream()
                .map(workflowTaskMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public WorkflowTaskDto create(CreateWorkflowTaskRequest request) {
        workflowTaskValidator.validateCreate(request);

        WorkflowTask entity = new WorkflowTask();
        entity.setCode(request.code());
        entity.setWorkflowInstance(resolveWorkflowInstance(request.workflowInstanceId()));
        entity.setStep(resolveStep(request.stepId()));
        entity.setAssignedTo(resolveUser(request.assignedToId()));
        entity.setAssignedRole(resolveRole(request.assignedRoleId()));
        entity.setStatus(request.status() != null ? request.status() : WorkflowTaskStatus.PENDING);
        entity.setDueDate(request.dueDate());
        entity.setCompletedAt(request.completedAt());
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return workflowTaskMapper.toDto(workflowTaskRepository.save(entity));
    }

    @Override
    @Transactional
    public WorkflowTaskDto update(UUID id, UpdateWorkflowTaskRequest request) {
        WorkflowTask entity = findActiveById(id);
        assertVersion(entity, request.version());
        workflowTaskValidator.validateUpdate(id, request);

        entity.setCode(request.code());
        entity.setAssignedTo(resolveUser(request.assignedToId()));
        entity.setAssignedRole(resolveRole(request.assignedRoleId()));
        if (request.status() != null) {
            entity.setStatus(request.status());
        }
        entity.setDueDate(request.dueDate());
        entity.setCompletedAt(request.completedAt());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return workflowTaskMapper.toDto(workflowTaskRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        WorkflowTask entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        workflowTaskRepository.save(entity);
    }

    private WorkflowInstance resolveWorkflowInstance(UUID workflowInstanceId) {
        return workflowInstanceRepository.findByIdAndDeletedFalse(workflowInstanceId)
                .orElseThrow(() -> new WorkflowInstanceNotFoundException(workflowInstanceId));
    }

    private WorkflowStep resolveStep(UUID stepId) {
        return workflowStepRepository.findByIdAndDeletedFalse(stepId)
                .orElseThrow(() -> new WorkflowStepNotFoundException(stepId));
    }

    private User resolveUser(UUID userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Role resolveRole(UUID roleId) {
        if (roleId == null) {
            return null;
        }
        return roleRepository.findByIdAndDeletedFalse(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
    }

    private WorkflowTask findActiveById(UUID id) {
        return workflowTaskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new WorkflowTaskNotFoundException(id));
    }

    private void assertVersion(WorkflowTask entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "WorkflowTask version mismatch for id: " + entity.getId());
        }
    }
}
