package com.govos.wrk.service;

import com.govos.idm.entity.User;
import com.govos.idm.exception.UserNotFoundException;
import com.govos.idm.repository.UserRepository;
import com.govos.wrk.dto.CreateWorkflowHistoryRequest;
import com.govos.wrk.dto.WorkflowHistoryDto;
import com.govos.wrk.entity.WorkflowHistory;
import com.govos.wrk.entity.WorkflowInstance;
import com.govos.wrk.exception.WorkflowHistoryNotFoundException;
import com.govos.wrk.exception.WorkflowInstanceNotFoundException;
import com.govos.wrk.mapper.WorkflowHistoryMapper;
import com.govos.wrk.repository.WorkflowHistoryRepository;
import com.govos.wrk.repository.WorkflowInstanceRepository;
import com.govos.wrk.validator.WorkflowHistoryValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WorkflowHistoryServiceImpl implements WorkflowHistoryService {

    private final WorkflowHistoryRepository workflowHistoryRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final UserRepository userRepository;
    private final WorkflowHistoryMapper workflowHistoryMapper;
    private final WorkflowHistoryValidator workflowHistoryValidator;

    public WorkflowHistoryServiceImpl(
            WorkflowHistoryRepository workflowHistoryRepository,
            WorkflowInstanceRepository workflowInstanceRepository,
            UserRepository userRepository,
            WorkflowHistoryMapper workflowHistoryMapper,
            WorkflowHistoryValidator workflowHistoryValidator) {
        this.workflowHistoryRepository = workflowHistoryRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.userRepository = userRepository;
        this.workflowHistoryMapper = workflowHistoryMapper;
        this.workflowHistoryValidator = workflowHistoryValidator;
    }

    @Override
    public WorkflowHistoryDto getById(UUID id) {
        return workflowHistoryMapper.toDto(findActiveById(id));
    }

    @Override
    public List<WorkflowHistoryDto> getByWorkflowInstanceId(UUID workflowInstanceId) {
        return workflowHistoryRepository
                .findByWorkflowInstance_IdAndDeletedFalseOrderByPerformedAtDesc(workflowInstanceId).stream()
                .map(workflowHistoryMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public WorkflowHistoryDto create(CreateWorkflowHistoryRequest request) {
        workflowHistoryValidator.validateCreate(request);

        WorkflowHistory entity = new WorkflowHistory();
        entity.setCode(request.code());
        entity.setWorkflowInstance(resolveWorkflowInstance(request.workflowInstanceId()));
        entity.setAction(request.action());
        entity.setPerformedBy(resolveUser(request.performedById()));
        entity.setPerformedAt(request.performedAt());
        entity.setRemarks(request.remarks());
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return workflowHistoryMapper.toDto(workflowHistoryRepository.save(entity));
    }

    private WorkflowInstance resolveWorkflowInstance(UUID workflowInstanceId) {
        return workflowInstanceRepository.findByIdAndDeletedFalse(workflowInstanceId)
                .orElseThrow(() -> new WorkflowInstanceNotFoundException(workflowInstanceId));
    }

    private User resolveUser(UUID userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private WorkflowHistory findActiveById(UUID id) {
        return workflowHistoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new WorkflowHistoryNotFoundException(id));
    }
}
