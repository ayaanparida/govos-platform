package com.govos.cmp.service.impl;

import com.govos.cmp.dto.ComplaintAssignmentCreateRequest;
import com.govos.cmp.dto.ComplaintCreateRequest;
import com.govos.cmp.dto.ComplaintDto;
import com.govos.cmp.dto.ComplaintDuplicateCreateRequest;
import com.govos.cmp.dto.ComplaintMergeCreateRequest;
import com.govos.cmp.dto.ComplaintUpdateRequest;
import com.govos.cmp.entity.Complaint;
import com.govos.cmp.entity.ComplaintAssignment;
import com.govos.cmp.entity.ComplaintStatusHistory;
import com.govos.cmp.enums.ComplaintAssignmentStatus;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.cmp.exception.ComplaintNotFoundException;
import com.govos.cmp.exception.ComplaintStateTransitionException;
import com.govos.cmp.exception.ComplaintValidationException;
import com.govos.cmp.mapper.ComplaintAssignmentMapper;
import com.govos.cmp.mapper.ComplaintMapper;
import com.govos.cmp.repository.ComplaintAssignmentRepository;
import com.govos.cmp.repository.ComplaintRepository;
import com.govos.cmp.repository.ComplaintStatusHistoryRepository;
import com.govos.cmp.service.ComplaintDuplicateService;
import com.govos.cmp.service.ComplaintMergeService;
import com.govos.cmp.support.CmpTestFixtures;
import com.govos.cmp.validator.ComplaintAssignmentValidator;
import com.govos.cmp.validator.ComplaintDuplicateValidator;
import com.govos.cmp.validator.ComplaintMergeValidator;
import com.govos.cmp.validator.ComplaintValidator;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceImplTest {

    @Mock private ComplaintRepository complaintRepository;
    @Mock private ComplaintStatusHistoryRepository complaintStatusHistoryRepository;
    @Mock private ComplaintAssignmentRepository complaintAssignmentRepository;
    @Mock private ComplaintMapper complaintMapper;
    @Mock private ComplaintAssignmentMapper complaintAssignmentMapper;
    @Mock private ComplaintValidator complaintValidator;
    @Mock private ComplaintAssignmentValidator complaintAssignmentValidator;
    @Mock private ComplaintDuplicateValidator complaintDuplicateValidator;
    @Mock private ComplaintMergeValidator complaintMergeValidator;
    @Mock private ComplaintDuplicateService complaintDuplicateService;
    @Mock private ComplaintMergeService complaintMergeService;

    @InjectMocks
    private ComplaintServiceImpl service;

    private UUID complaintId;
    private UUID userId;
    private Complaint complaint;
    private ComplaintDto complaintDto;

    @BeforeEach
    void setUp() {
        complaintId = UUID.randomUUID();
        userId = CmpTestFixtures.OFFICER_ID;
        complaint = CmpTestFixtures.complaint(complaintId, ComplaintStatus.DRAFT);
        complaintDto = minimalDto(complaint);
    }

    @Test
    void shouldCreateComplaint() {
        ComplaintCreateRequest request = CmpTestFixtures.createRequest();
        when(complaintMapper.toEntity(request)).thenReturn(complaint);
        when(complaintRepository.save(complaint)).thenReturn(complaint);
        when(complaintMapper.toDto(complaint)).thenReturn(complaintDto);

        assertThat(service.create(request)).isEqualTo(complaintDto);
        verify(complaintValidator).validateCreate(request);
        assertThat(complaint.getDeleted()).isFalse();
        assertThat(complaint.getActive()).isTrue();
    }

    @Test
    void shouldUpdateComplaint() {
        ComplaintUpdateRequest request = CmpTestFixtures.updateRequest();
        when(complaintRepository.findByIdAndDeletedFalse(complaintId)).thenReturn(Optional.of(complaint));
        when(complaintRepository.save(complaint)).thenReturn(complaint);
        when(complaintMapper.toDto(complaint)).thenReturn(complaintDto);

        assertThat(service.update(complaintId, request)).isEqualTo(complaintDto);
        verify(complaintValidator).validateUpdate(complaintId, request);
        verify(complaintMapper).updateEntity(request, complaint);
    }

    @Test
    void shouldThrowWhenUpdateVersionMismatch() {
        complaint.setVersion(1L);
        ComplaintUpdateRequest request = new ComplaintUpdateRequest(
                "Title", "Desc", complaint.getPriority(), null, CATEGORY(), SUB_CATEGORY(), null,
                null, null, "KA", "BLR", null, null, null, null, null, null, null, null, null, true, 0L);
        when(complaintRepository.findByIdAndDeletedFalse(complaintId)).thenReturn(Optional.of(complaint));

        assertThatThrownBy(() -> service.update(complaintId, request))
                .isInstanceOf(OptimisticLockException.class);
    }

    @Test
    void shouldGetById() {
        when(complaintRepository.findByIdAndDeletedFalse(complaintId)).thenReturn(Optional.of(complaint));
        when(complaintMapper.toDto(complaint)).thenReturn(complaintDto);

        assertThat(service.getById(complaintId)).isEqualTo(complaintDto);
    }

    @Test
    void shouldThrowWhenGetByIdNotFound() {
        when(complaintRepository.findByIdAndDeletedFalse(complaintId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(complaintId))
                .isInstanceOf(ComplaintNotFoundException.class);
    }

    @Test
    void shouldGetByCode() {
        when(complaintRepository.findByCodeAndDeletedFalse("CMP-2026-0001")).thenReturn(Optional.of(complaint));
        when(complaintMapper.toDto(complaint)).thenReturn(complaintDto);

        assertThat(service.getByCode("CMP-2026-0001")).isEqualTo(complaintDto);
    }

    @Test
    void shouldListComplaints() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(complaintRepository.findAllByDeletedFalse(pageable)).thenReturn(new PageImpl<>(List.of(complaint)));
        when(complaintMapper.toDto(complaint)).thenReturn(complaintDto);

        Page<ComplaintDto> result = service.list(pageable);

        assertThat(result.getContent()).containsExactly(complaintDto);
    }

    @Test
    void shouldSoftDelete() {
        when(complaintRepository.findByIdAndDeletedFalse(complaintId)).thenReturn(Optional.of(complaint));

        service.softDelete(complaintId);

        assertThat(complaint.getDeleted()).isTrue();
        assertThat(complaint.getActive()).isFalse();
        verify(complaintRepository).save(complaint);
    }

    @Test
    void shouldRestoreDeletedComplaint() {
        complaint.setDeleted(true);
        complaint.setActive(false);
        when(complaintRepository.findById(complaintId)).thenReturn(Optional.of(complaint));
        when(complaintRepository.save(complaint)).thenReturn(complaint);
        when(complaintMapper.toDto(complaint)).thenReturn(complaintDto);

        assertThat(service.restore(complaintId)).isEqualTo(complaintDto);
        assertThat(complaint.getDeleted()).isFalse();
        assertThat(complaint.getActive()).isTrue();
    }

    @Test
    void shouldThrowWhenRestoreNotDeleted() {
        when(complaintRepository.findById(complaintId)).thenReturn(Optional.of(complaint));

        assertThatThrownBy(() -> service.restore(complaintId))
                .isInstanceOf(ComplaintNotFoundException.class);
    }

    @Test
    void shouldSubmitComplaint() {
        stubActiveComplaint();
        when(complaintRepository.save(complaint)).thenReturn(complaint);
        when(complaintMapper.toDto(complaint)).thenReturn(complaintDto);

        assertThat(service.submit(complaintId, userId)).isEqualTo(complaintDto);

        verify(complaintValidator).validateTransition(complaint, ComplaintStatus.SUBMITTED);
        verify(complaintValidator).validateReadyForSubmit(complaint);
        assertThat(complaint.getStatus()).isEqualTo(ComplaintStatus.SUBMITTED);
        assertThat(complaint.getSubmittedAt()).isNotNull();
        verify(complaintStatusHistoryRepository).save(any(ComplaintStatusHistory.class));
    }

    @Test
    void shouldAcceptComplaint() {
        complaint.setStatus(ComplaintStatus.SUBMITTED);
        stubActiveComplaint();
        when(complaintRepository.save(complaint)).thenReturn(complaint);
        when(complaintMapper.toDto(complaint)).thenReturn(complaintDto);

        assertThat(service.accept(complaintId, userId)).isEqualTo(complaintDto);
        assertThat(complaint.getStatus()).isEqualTo(ComplaintStatus.ACCEPTED);
    }

    @Test
    void shouldRejectComplaint() {
        complaint.setStatus(ComplaintStatus.SUBMITTED);
        stubActiveComplaint();
        when(complaintRepository.save(complaint)).thenReturn(complaint);
        when(complaintMapper.toDto(complaint)).thenReturn(complaintDto);

        assertThat(service.reject(complaintId, userId, "INVALID")).isEqualTo(complaintDto);
        assertThat(complaint.getStatus()).isEqualTo(ComplaintStatus.REJECTED);
        assertThat(complaint.getRejectionReasonKey()).isEqualTo("INVALID");
    }

    @Test
    void shouldThrowWhenRejectWithoutReason() {
        stubActiveComplaint();

        assertThatThrownBy(() -> service.reject(complaintId, userId, " "))
                .isInstanceOf(ComplaintValidationException.class);
    }

    @Test
    void shouldAssignComplaint() {
        complaint.setStatus(ComplaintStatus.ACCEPTED);
        stubActiveComplaint();
        ComplaintAssignmentCreateRequest assignmentRequest = CmpTestFixtures.assignmentCreateRequest(complaintId);
        ComplaintAssignment assignment = CmpTestFixtures.assignment(UUID.randomUUID(), complaintId);

        when(complaintAssignmentMapper.toEntity(assignmentRequest)).thenReturn(assignment);
        when(complaintRepository.save(complaint)).thenReturn(complaint);
        when(complaintMapper.toDto(complaint)).thenReturn(complaintDto);

        assertThat(service.assign(complaintId, assignmentRequest)).isEqualTo(complaintDto);

        verify(complaintAssignmentValidator).validateCreate(assignmentRequest);
        verify(complaintAssignmentRepository).save(assignment);
        assertThat(complaint.getStatus()).isEqualTo(ComplaintStatus.ASSIGNED);
        assertThat(assignment.getIsCurrent()).isTrue();
    }

    @Test
    void shouldThrowWhenAssignmentComplaintIdMismatch() {
        ComplaintAssignmentCreateRequest request = CmpTestFixtures.assignmentCreateRequest(UUID.randomUUID());

        assertThatThrownBy(() -> service.assign(complaintId, request))
                .isInstanceOf(ComplaintValidationException.class);
    }

    @Test
    void shouldSupersedeCurrentAssignmentOnReassign() {
        complaint.setStatus(ComplaintStatus.PENDING_REASSIGNMENT);
        stubActiveComplaint();
        ComplaintAssignment current = CmpTestFixtures.assignment(UUID.randomUUID(), complaintId);
        ComplaintAssignmentCreateRequest assignmentRequest = CmpTestFixtures.assignmentCreateRequest(complaintId);
        ComplaintAssignment newAssignment = CmpTestFixtures.assignment(UUID.randomUUID(), complaintId);

        when(complaintAssignmentRepository.findByComplaintIdAndIsCurrentTrueAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(current))
                .thenReturn(Optional.of(current));
        when(complaintAssignmentMapper.toEntity(assignmentRequest)).thenReturn(newAssignment);
        when(complaintRepository.save(complaint)).thenReturn(complaint);
        when(complaintMapper.toDto(complaint)).thenReturn(complaintDto);

        service.assign(complaintId, assignmentRequest);

        assertThat(current.getIsCurrent()).isFalse();
        assertThat(current.getAssignmentStatus()).isEqualTo(ComplaintAssignmentStatus.SUPERSEDED);
    }

    @Test
    void shouldStartProgress() {
        complaint.setStatus(ComplaintStatus.ASSIGNED);
        stubActiveComplaint();
        when(complaintRepository.save(complaint)).thenReturn(complaint);
        when(complaintMapper.toDto(complaint)).thenReturn(complaintDto);

        assertThat(service.startProgress(complaintId, userId)).isEqualTo(complaintDto);
        assertThat(complaint.getStatus()).isEqualTo(ComplaintStatus.IN_PROGRESS);
    }

    @Test
    void shouldResolveComplaint() {
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        stubActiveComplaint();
        when(complaintRepository.save(complaint)).thenReturn(complaint);
        when(complaintMapper.toDto(complaint)).thenReturn(complaintDto);

        assertThat(service.resolve(complaintId, userId, "FIXED")).isEqualTo(complaintDto);
        assertThat(complaint.getStatus()).isEqualTo(ComplaintStatus.RESOLVED);
        assertThat(complaint.getResolvedByUserId()).isEqualTo(userId);
    }

    @Test
    void shouldCloseComplaint() {
        complaint.setStatus(ComplaintStatus.RESOLVED);
        stubActiveComplaint();
        when(complaintRepository.save(complaint)).thenReturn(complaint);
        when(complaintMapper.toDto(complaint)).thenReturn(complaintDto);

        assertThat(service.close(complaintId, userId, "SATISFIED")).isEqualTo(complaintDto);
        assertThat(complaint.getStatus()).isEqualTo(ComplaintStatus.CLOSED);
        assertThat(complaint.getClosureReasonKey()).isEqualTo("SATISFIED");
        assertThat(complaint.getClosedAt()).isNotNull();
    }

    @Test
    void shouldArchiveComplaint() {
        complaint.setStatus(ComplaintStatus.CLOSED);
        stubActiveComplaint();
        when(complaintRepository.save(complaint)).thenReturn(complaint);
        when(complaintMapper.toDto(complaint)).thenReturn(complaintDto);

        assertThat(service.archive(complaintId, userId)).isEqualTo(complaintDto);
        assertThat(complaint.getStatus()).isEqualTo(ComplaintStatus.ARCHIVED);
        assertThat(complaint.getArchivedAt()).isNotNull();
    }

    @Test
    void shouldReopenComplaint() {
        complaint.setStatus(ComplaintStatus.CLOSED);
        stubActiveComplaint();
        when(complaintRepository.save(complaint)).thenReturn(complaint);
        when(complaintMapper.toDto(complaint)).thenReturn(complaintDto);

        assertThat(service.reopen(complaintId, userId, "UNSATISFIED")).isEqualTo(complaintDto);
        assertThat(complaint.getStatus()).isEqualTo(ComplaintStatus.REOPENED);
    }

    @Test
    void shouldRequestReassignment() {
        complaint.setStatus(ComplaintStatus.ASSIGNED);
        stubActiveComplaint();
        ComplaintAssignment current = CmpTestFixtures.assignment(UUID.randomUUID(), complaintId);
        when(complaintAssignmentRepository.findByComplaintIdAndIsCurrentTrueAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(current));
        when(complaintRepository.save(complaint)).thenReturn(complaint);
        when(complaintMapper.toDto(complaint)).thenReturn(complaintDto);

        assertThat(service.requestReassignment(complaintId, userId, "OVERLOADED")).isEqualTo(complaintDto);

        assertThat(complaint.getStatus()).isEqualTo(ComplaintStatus.PENDING_REASSIGNMENT);
        assertThat(current.getAssignmentStatus()).isEqualTo(ComplaintAssignmentStatus.REJECTED);
        assertThat(current.getIsCurrent()).isFalse();
    }

    @Test
    void shouldMarkDuplicate() {
        UUID primaryId = UUID.randomUUID();
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        stubActiveComplaint();
        ComplaintDuplicateCreateRequest duplicateRequest = CmpTestFixtures.duplicateCreateRequest(primaryId, complaintId);
        when(complaintRepository.save(complaint)).thenReturn(complaint);
        when(complaintMapper.toDto(complaint)).thenReturn(complaintDto);

        assertThat(service.markDuplicate(complaintId, duplicateRequest, userId)).isEqualTo(complaintDto);

        verify(complaintDuplicateValidator).validateCreate(duplicateRequest);
        verify(complaintDuplicateService).createDuplicate(duplicateRequest);
        assertThat(complaint.getIsDuplicate()).isTrue();
        assertThat(complaint.getPrimaryComplaintId()).isEqualTo(primaryId);
        assertThat(complaint.getStatus()).isEqualTo(ComplaintStatus.CANCELLED);
    }

    @Test
    void shouldThrowWhenMarkDuplicateIdMismatch() {
        ComplaintDuplicateCreateRequest request = CmpTestFixtures.duplicateCreateRequest(UUID.randomUUID(), UUID.randomUUID());

        assertThatThrownBy(() -> service.markDuplicate(complaintId, request, userId))
                .isInstanceOf(ComplaintValidationException.class);
    }

    @Test
    void shouldMergeComplaints() {
        UUID mergedId = UUID.randomUUID();
        Complaint surviving = CmpTestFixtures.complaint(complaintId, ComplaintStatus.IN_PROGRESS);
        Complaint merged = CmpTestFixtures.complaint(mergedId, ComplaintStatus.IN_PROGRESS);
        ComplaintMergeCreateRequest mergeRequest = CmpTestFixtures.mergeCreateRequest(complaintId, mergedId);

        when(complaintRepository.findByIdAndDeletedFalse(complaintId)).thenReturn(Optional.of(surviving));
        when(complaintRepository.findByIdAndDeletedFalse(mergedId)).thenReturn(Optional.of(merged));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(complaintMapper.toDto(any(Complaint.class))).thenReturn(complaintDto);

        assertThat(service.merge(complaintId, mergeRequest, userId)).isEqualTo(complaintDto);

        verify(complaintMergeValidator).validateCreate(mergeRequest);
        verify(complaintMergeService).createMerge(mergeRequest);
        assertThat(merged.getMergedIntoComplaintId()).isEqualTo(complaintId);
        assertThat(merged.getStatus()).isEqualTo(ComplaintStatus.CANCELLED);
    }

    @Test
    void shouldThrowWhenMergeSurvivingIdMismatch() {
        ComplaintMergeCreateRequest request = CmpTestFixtures.mergeCreateRequest(UUID.randomUUID(), UUID.randomUUID());

        assertThatThrownBy(() -> service.merge(complaintId, request, userId))
                .isInstanceOf(ComplaintValidationException.class);
    }

    @Test
    void shouldThrowWhenInvalidLifecycleTransition() {
        stubActiveComplaint();
        doThrow(new ComplaintStateTransitionException("illegal"))
                .when(complaintValidator).validateTransition(complaint, ComplaintStatus.SUBMITTED);

        assertThatThrownBy(() -> service.submit(complaintId, userId))
                .isInstanceOf(ComplaintStateTransitionException.class);
        verify(complaintRepository, never()).save(complaint);
    }

    @Test
    void shouldRecordStatusHistoryOnTransition() {
        complaint.setStatus(ComplaintStatus.SUBMITTED);
        stubActiveComplaint();
        when(complaintRepository.save(complaint)).thenReturn(complaint);
        when(complaintMapper.toDto(complaint)).thenReturn(complaintDto);

        service.accept(complaintId, userId);

        ArgumentCaptor<ComplaintStatusHistory> captor = ArgumentCaptor.forClass(ComplaintStatusHistory.class);
        verify(complaintStatusHistoryRepository).save(captor.capture());
        ComplaintStatusHistory history = captor.getValue();
        assertThat(history.getFromStatus()).isEqualTo(ComplaintStatus.SUBMITTED);
        assertThat(history.getToStatus()).isEqualTo(ComplaintStatus.ACCEPTED);
        assertThat(history.getChangedByUserId()).isEqualTo(userId);
    }

    private void stubActiveComplaint() {
        when(complaintRepository.findByIdAndDeletedFalse(complaintId)).thenReturn(Optional.of(complaint));
    }

    private static String CATEGORY() {
        return CmpTestFixtures.CATEGORY_KEY;
    }

    private static String SUB_CATEGORY() {
        return CmpTestFixtures.SUB_CATEGORY_KEY;
    }

    private static ComplaintDto minimalDto(Complaint entity) {
        return new ComplaintDto(
                entity.getId(), entity.getCode(), entity.getTitle(), entity.getDescription(),
                entity.getStatus(), entity.getPriority(), entity.getSource(), entity.getChannel(),
                entity.getCategoryKey(), entity.getSubCategoryKey(), entity.getComplaintTypeKey(),
                entity.getCitizenUserId(), entity.getSubmittedByUserId(), entity.getOrganizationId(),
                entity.getDepartmentId(), entity.getOfficeId(), entity.getAssignedOfficerId(),
                entity.getResolvedByUserId(), entity.getClosedByUserId(), entity.getWorkflowInstanceId(),
                entity.getSubmittedAt(), entity.getClosedAt(), entity.getArchivedAt(),
                entity.getRejectionReasonKey(), entity.getClosureReasonKey(), entity.getIsDuplicate(),
                entity.getPrimaryComplaintId(), entity.getMergedIntoComplaintId(),
                "KA", "BLR", null, null, null,
                entity.getLocation().getLatitude(), entity.getLocation().getLongitude(),
                entity.getLocation().getAddress(), null, null, null,
                entity.getActive(), entity.getVersion(), null, null, null, null);
    }
}
