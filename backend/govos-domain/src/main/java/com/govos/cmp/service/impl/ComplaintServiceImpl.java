package com.govos.cmp.service.impl;

import com.govos.cmp.dto.ComplaintAssignmentCreateRequest;
import com.govos.cmp.dto.ComplaintCreateRequest;
import com.govos.cmp.dto.ComplaintDuplicateCreateRequest;
import com.govos.cmp.dto.ComplaintDto;
import com.govos.cmp.dto.ComplaintMergeCreateRequest;
import com.govos.cmp.dto.ComplaintUpdateRequest;
import com.govos.cmp.entity.Complaint;
import com.govos.cmp.entity.ComplaintAssignment;
import com.govos.cmp.entity.ComplaintStatusHistory;
import com.govos.cmp.enums.ComplaintAssignmentStatus;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.cmp.exception.ComplaintNotFoundException;
import com.govos.cmp.exception.ComplaintValidationException;
import com.govos.cmp.mapper.ComplaintAssignmentMapper;
import com.govos.cmp.mapper.ComplaintMapper;
import com.govos.cmp.repository.ComplaintAssignmentRepository;
import com.govos.cmp.repository.ComplaintRepository;
import com.govos.cmp.repository.ComplaintStatusHistoryRepository;
import com.govos.cmp.service.ComplaintDuplicateService;
import com.govos.cmp.service.ComplaintMergeService;
import com.govos.cmp.service.ComplaintService;
import com.govos.cmp.validator.ComplaintAssignmentValidator;
import com.govos.cmp.validator.ComplaintDuplicateValidator;
import com.govos.cmp.validator.ComplaintMergeValidator;
import com.govos.cmp.validator.ComplaintValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository complaintStatusHistoryRepository;
    private final ComplaintAssignmentRepository complaintAssignmentRepository;
    private final ComplaintMapper complaintMapper;
    private final ComplaintAssignmentMapper complaintAssignmentMapper;
    private final ComplaintValidator complaintValidator;
    private final ComplaintAssignmentValidator complaintAssignmentValidator;
    private final ComplaintDuplicateValidator complaintDuplicateValidator;
    private final ComplaintMergeValidator complaintMergeValidator;
    private final ComplaintDuplicateService complaintDuplicateService;
    private final ComplaintMergeService complaintMergeService;

    public ComplaintServiceImpl(
            ComplaintRepository complaintRepository,
            ComplaintStatusHistoryRepository complaintStatusHistoryRepository,
            ComplaintAssignmentRepository complaintAssignmentRepository,
            ComplaintMapper complaintMapper,
            ComplaintAssignmentMapper complaintAssignmentMapper,
            ComplaintValidator complaintValidator,
            ComplaintAssignmentValidator complaintAssignmentValidator,
            ComplaintDuplicateValidator complaintDuplicateValidator,
            ComplaintMergeValidator complaintMergeValidator,
            ComplaintDuplicateService complaintDuplicateService,
            ComplaintMergeService complaintMergeService) {
        this.complaintRepository = complaintRepository;
        this.complaintStatusHistoryRepository = complaintStatusHistoryRepository;
        this.complaintAssignmentRepository = complaintAssignmentRepository;
        this.complaintMapper = complaintMapper;
        this.complaintAssignmentMapper = complaintAssignmentMapper;
        this.complaintValidator = complaintValidator;
        this.complaintAssignmentValidator = complaintAssignmentValidator;
        this.complaintDuplicateValidator = complaintDuplicateValidator;
        this.complaintMergeValidator = complaintMergeValidator;
        this.complaintDuplicateService = complaintDuplicateService;
        this.complaintMergeService = complaintMergeService;
    }

    @Override
    @Transactional
    public ComplaintDto create(ComplaintCreateRequest request) {
        complaintValidator.validateCreate(request);

        Complaint entity = complaintMapper.toEntity(request);
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return complaintMapper.toDto(complaintRepository.save(entity));
    }

    @Override
    @Transactional
    public ComplaintDto update(UUID id, ComplaintUpdateRequest request) {
        Complaint entity = findActiveById(id);
        assertVersion(entity, request.version());

        complaintValidator.validateUpdate(id, request);
        complaintMapper.updateEntity(request, entity);
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return complaintMapper.toDto(complaintRepository.save(entity));
    }

    @Override
    public ComplaintDto getById(UUID id) {
        return complaintMapper.toDto(findActiveById(id));
    }

    @Override
    public ComplaintDto getByCode(String code) {
        return complaintMapper.toDto(findActiveByCode(code));
    }

    @Override
    public Page<ComplaintDto> list(Pageable pageable) {
        return complaintRepository.findAllByDeletedFalse(pageable)
                .map(complaintMapper::toDto);
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        Complaint entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        complaintRepository.save(entity);
    }

    @Override
    @Transactional
    public ComplaintDto restore(UUID id) {
        Complaint entity = complaintRepository.findById(id)
                .filter(complaint -> Boolean.TRUE.equals(complaint.getDeleted()))
                .orElseThrow(() -> new ComplaintNotFoundException(id));
        entity.setDeleted(false);
        entity.setActive(true);
        return complaintMapper.toDto(complaintRepository.save(entity));
    }

    @Override
    @Transactional
    public ComplaintDto submit(UUID id, UUID changedByUserId) {
        Complaint entity = findActiveById(id);
        complaintValidator.validateTransition(entity, ComplaintStatus.SUBMITTED);
        complaintValidator.validateReadyForSubmit(entity);

        return transition(entity, ComplaintStatus.SUBMITTED, changedByUserId, null, complaint -> {
            complaint.setSubmittedAt(Instant.now());
        });
    }

    @Override
    @Transactional
    public ComplaintDto accept(UUID id, UUID changedByUserId) {
        Complaint entity = findActiveById(id);
        complaintValidator.validateTransition(entity, ComplaintStatus.ACCEPTED);
        return transition(entity, ComplaintStatus.ACCEPTED, changedByUserId, null, null);
    }

    @Override
    @Transactional
    public ComplaintDto assign(UUID id, ComplaintAssignmentCreateRequest assignmentRequest) {
        if (!id.equals(assignmentRequest.complaintId())) {
            throw new ComplaintValidationException("Assignment complaint id must match complaint id: " + id);
        }

        Complaint entity = findActiveById(id);
        complaintValidator.validateAssignTransition(entity);
        supersedeCurrentAssignment(id);
        complaintAssignmentValidator.validateCreate(assignmentRequest);

        ComplaintAssignment assignment = complaintAssignmentMapper.toEntity(assignmentRequest);
        assignment.setComplaint(entity);
        assignment.setAssignedAt(Instant.now());
        assignment.setIsCurrent(true);
        assignment.setActive(true);
        assignment.setDeleted(false);
        complaintAssignmentRepository.save(assignment);

        entity.setAssignedOfficerId(assignmentRequest.officerUserId());
        entity.setDepartmentId(assignmentRequest.departmentId());
        entity.setOfficeId(assignmentRequest.officeId());

        return transition(entity, ComplaintStatus.ASSIGNED, assignmentRequest.assignedByUserId(), null, null);
    }

    @Override
    @Transactional
    public ComplaintDto startProgress(UUID id, UUID changedByUserId) {
        Complaint entity = findActiveById(id);
        complaintValidator.validateTransition(entity, ComplaintStatus.IN_PROGRESS);
        return transition(entity, ComplaintStatus.IN_PROGRESS, changedByUserId, null, null);
    }

    @Override
    @Transactional
    public ComplaintDto resolve(UUID id, UUID changedByUserId, String reasonKey) {
        Complaint entity = findActiveById(id);
        complaintValidator.validateTransition(entity, ComplaintStatus.RESOLVED);
        return transition(entity, ComplaintStatus.RESOLVED, changedByUserId, reasonKey, complaint -> {
            complaint.setResolvedByUserId(changedByUserId);
        });
    }

    @Override
    @Transactional
    public ComplaintDto close(UUID id, UUID changedByUserId, String closureReasonKey) {
        Complaint entity = findActiveById(id);
        complaintValidator.validateTransition(entity, ComplaintStatus.CLOSED);
        return transition(entity, ComplaintStatus.CLOSED, changedByUserId, closureReasonKey, complaint -> {
            complaint.setClosedAt(Instant.now());
            complaint.setClosedByUserId(changedByUserId);
            complaint.setClosureReasonKey(closureReasonKey);
        });
    }

    @Override
    @Transactional
    public ComplaintDto archive(UUID id, UUID changedByUserId) {
        Complaint entity = findActiveById(id);
        complaintValidator.validateArchiveEligible(entity);
        complaintValidator.validateTransition(entity, ComplaintStatus.ARCHIVED);
        return transition(entity, ComplaintStatus.ARCHIVED, changedByUserId, null, complaint -> {
            complaint.setArchivedAt(Instant.now());
        });
    }

    @Override
    @Transactional
    public ComplaintDto reopen(UUID id, UUID changedByUserId, String reasonKey) {
        Complaint entity = findActiveById(id);
        complaintValidator.validateReopenEligible(entity);
        complaintValidator.validateTransition(entity, ComplaintStatus.REOPENED);
        return transition(entity, ComplaintStatus.REOPENED, changedByUserId, reasonKey, null);
    }

    @Override
    @Transactional
    public ComplaintDto reject(UUID id, UUID changedByUserId, String rejectionReasonKey) {
        Complaint entity = findActiveById(id);
        complaintValidator.validateTransition(entity, ComplaintStatus.REJECTED);
        if (!StringUtils.hasText(rejectionReasonKey)) {
            throw new ComplaintValidationException("Rejection reason is required");
        }
        return transition(entity, ComplaintStatus.REJECTED, changedByUserId, rejectionReasonKey, complaint -> {
            complaint.setRejectionReasonKey(rejectionReasonKey);
        });
    }

    @Override
    @Transactional
    public ComplaintDto requestReassignment(UUID id, UUID changedByUserId, String rejectionReasonKey) {
        Complaint entity = findActiveById(id);
        complaintValidator.validateTransition(entity, ComplaintStatus.PENDING_REASSIGNMENT);

        complaintAssignmentRepository.findByComplaintIdAndIsCurrentTrueAndDeletedFalse(id)
                .ifPresent(assignment -> {
                    assignment.setAssignmentStatus(ComplaintAssignmentStatus.REJECTED);
                    assignment.setIsCurrent(false);
                    assignment.setRejectedAt(Instant.now());
                    assignment.setRejectionReasonKey(rejectionReasonKey);
                    complaintAssignmentRepository.save(assignment);
                });

        return transition(entity, ComplaintStatus.PENDING_REASSIGNMENT, changedByUserId, rejectionReasonKey, null);
    }

    @Override
    @Transactional
    public ComplaintDto markDuplicate(UUID id, ComplaintDuplicateCreateRequest duplicateRequest, UUID changedByUserId) {
        if (!id.equals(duplicateRequest.duplicateComplaintId())) {
            throw new ComplaintValidationException(
                    "Duplicate complaint id must match complaint id: " + id);
        }

        Complaint entity = findActiveById(id);
        complaintValidator.validateMarkDuplicateEligible(entity);
        complaintDuplicateValidator.validateCreate(duplicateRequest);

        complaintDuplicateService.createDuplicate(duplicateRequest);

        entity.setIsDuplicate(true);
        entity.setPrimaryComplaintId(duplicateRequest.primaryComplaintId());

        return transition(entity, ComplaintStatus.CANCELLED, changedByUserId, "DUPLICATE", null);
    }

    @Override
    @Transactional
    public ComplaintDto merge(UUID survivingComplaintId, ComplaintMergeCreateRequest mergeRequest, UUID changedByUserId) {
        if (!survivingComplaintId.equals(mergeRequest.survivingComplaintId())) {
            throw new ComplaintValidationException(
                    "Surviving complaint id must match merge request: " + survivingComplaintId);
        }

        Complaint surviving = findActiveById(survivingComplaintId);
        Complaint merged = findActiveById(mergeRequest.mergedComplaintId());

        complaintValidator.validateMergeEligible(surviving);
        complaintValidator.validateMergeEligible(merged);
        complaintMergeValidator.validateCreate(mergeRequest);

        complaintMergeService.createMerge(mergeRequest);

        merged.setMergedIntoComplaintId(survivingComplaintId);
        transition(merged, ComplaintStatus.CANCELLED, changedByUserId, "MERGED", null);

        return complaintMapper.toDto(surviving);
    }

    @Override
    @Transactional
    public ComplaintDto linkWorkflowInstance(UUID complaintId, UUID workflowInstanceId) {
        Complaint entity = findActiveById(complaintId);
        if (entity.getWorkflowInstanceId() != null
                && !entity.getWorkflowInstanceId().equals(workflowInstanceId)) {
            throw new ComplaintValidationException(
                    "Complaint already linked to a different workflow instance");
        }
        entity.setWorkflowInstanceId(workflowInstanceId);
        return complaintMapper.toDto(complaintRepository.save(entity));
    }

    private ComplaintDto transition(
            Complaint entity,
            ComplaintStatus targetStatus,
            UUID changedByUserId,
            String reasonKey,
            java.util.function.Consumer<Complaint> entityCustomizer) {
        ComplaintStatus fromStatus = entity.getStatus();
        entity.setStatus(targetStatus);
        if (entityCustomizer != null) {
            entityCustomizer.accept(entity);
        }
        Complaint saved = complaintRepository.save(entity);
        ComplaintStatusHistory history = buildStatusHistory(saved, fromStatus, targetStatus, changedByUserId, reasonKey);
        complaintStatusHistoryRepository.save(history);
        return complaintMapper.toDto(saved);
    }

    private ComplaintStatusHistory buildStatusHistory(
            Complaint complaint,
            ComplaintStatus fromStatus,
            ComplaintStatus toStatus,
            UUID changedByUserId,
            String reasonKey) {
        ComplaintStatusHistory history = new ComplaintStatusHistory();
        history.setComplaint(complaint);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setChangedByUserId(changedByUserId);
        history.setReasonKey(reasonKey);
        history.setOccurredAt(Instant.now());
        history.setActive(true);
        history.setDeleted(false);
        return history;
    }

    private void supersedeCurrentAssignment(UUID complaintId) {
        complaintAssignmentRepository.findByComplaintIdAndIsCurrentTrueAndDeletedFalse(complaintId)
                .ifPresent(assignment -> {
                    assignment.setIsCurrent(false);
                    assignment.setAssignmentStatus(ComplaintAssignmentStatus.SUPERSEDED);
                    complaintAssignmentRepository.save(assignment);
                });
    }

    private Complaint findActiveById(UUID id) {
        return complaintRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ComplaintNotFoundException(id));
    }

    private Complaint findActiveByCode(String code) {
        return complaintRepository.findByCodeAndDeletedFalse(code)
                .orElseThrow(() -> new ComplaintNotFoundException(code));
    }

    private void assertVersion(Complaint entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "Complaint version mismatch for id: " + entity.getId());
        }
    }
}
