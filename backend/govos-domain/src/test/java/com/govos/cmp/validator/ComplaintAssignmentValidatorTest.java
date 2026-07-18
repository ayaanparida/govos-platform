package com.govos.cmp.validator;

import com.govos.cmp.dto.ComplaintAssignmentCreateRequest;
import com.govos.cmp.dto.ComplaintAssignmentUpdateRequest;
import com.govos.cmp.entity.Complaint;
import com.govos.cmp.entity.ComplaintAssignment;
import com.govos.cmp.enums.ComplaintAssignmentType;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.cmp.exception.ComplaintAssignmentException;
import com.govos.cmp.exception.ComplaintLifecycleException;
import com.govos.cmp.exception.ComplaintNotFoundException;
import com.govos.cmp.repository.ComplaintAssignmentRepository;
import com.govos.cmp.repository.ComplaintRepository;
import com.govos.cmp.support.CmpTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplaintAssignmentValidatorTest {

    @Mock private ComplaintRepository complaintRepository;
    @Mock private ComplaintAssignmentRepository complaintAssignmentRepository;

    private ComplaintAssignmentValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ComplaintAssignmentValidator(complaintRepository, complaintAssignmentRepository);
    }

    @Test
    void shouldValidateCreateWhenInputValid() {
        UUID complaintId = UUID.randomUUID();
        Complaint complaint = CmpTestFixtures.complaint(complaintId, ComplaintStatus.ACCEPTED);
        ComplaintAssignmentCreateRequest request = CmpTestFixtures.assignmentCreateRequest(complaintId);

        when(complaintRepository.findByIdAndDeletedFalse(complaintId)).thenReturn(Optional.of(complaint));
        when(complaintAssignmentRepository.findByComplaintIdAndIsCurrentTrueAndDeletedFalse(complaintId))
                .thenReturn(Optional.empty());

        assertThatCode(() -> validator.validateCreate(request)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectWhenComplaintNotFound() {
        UUID complaintId = UUID.randomUUID();
        ComplaintAssignmentCreateRequest request = CmpTestFixtures.assignmentCreateRequest(complaintId);
        when(complaintRepository.findByIdAndDeletedFalse(complaintId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintNotFoundException.class);
    }

    @Test
    void shouldRejectWhenOfficerMissing() {
        UUID complaintId = UUID.randomUUID();
        Complaint complaint = CmpTestFixtures.complaint(complaintId, ComplaintStatus.ACCEPTED);
        ComplaintAssignmentCreateRequest request = new ComplaintAssignmentCreateRequest(
                complaintId, ComplaintAssignmentType.INITIAL, null, null, null, null, null, true);

        when(complaintRepository.findByIdAndDeletedFalse(complaintId)).thenReturn(Optional.of(complaint));

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintAssignmentException.class);
    }

    @Test
    void shouldRejectWhenCurrentAssignmentExists() {
        UUID complaintId = UUID.randomUUID();
        Complaint complaint = CmpTestFixtures.complaint(complaintId, ComplaintStatus.ASSIGNED);
        ComplaintAssignmentCreateRequest request = CmpTestFixtures.assignmentCreateRequest(complaintId);

        when(complaintRepository.findByIdAndDeletedFalse(complaintId)).thenReturn(Optional.of(complaint));
        when(complaintAssignmentRepository.findByComplaintIdAndIsCurrentTrueAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(CmpTestFixtures.assignment(UUID.randomUUID(), complaintId)));

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintAssignmentException.class);
    }

    @Test
    void shouldRejectArchivedComplaintAssignment() {
        UUID complaintId = UUID.randomUUID();
        Complaint complaint = CmpTestFixtures.complaint(complaintId, ComplaintStatus.ARCHIVED);
        ComplaintAssignmentCreateRequest request = CmpTestFixtures.assignmentCreateRequest(complaintId);

        when(complaintRepository.findByIdAndDeletedFalse(complaintId)).thenReturn(Optional.of(complaint));

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintLifecycleException.class);
    }

    @Test
    void shouldRejectUpdateWhenAnotherCurrentAssignmentExists() {
        UUID complaintId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();
        ComplaintAssignment other = CmpTestFixtures.assignment(UUID.randomUUID(), complaintId);
        ComplaintAssignmentUpdateRequest request = new ComplaintAssignmentUpdateRequest(
                ComplaintAssignmentType.INITIAL, null, null, null, null, null, null, true, true, 0L);

        when(complaintRepository.findByIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(complaintId, ComplaintStatus.ASSIGNED)));
        when(complaintAssignmentRepository.findByComplaintIdAndIsCurrentTrueAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(other));

        assertThatThrownBy(() -> validator.validateUpdate(assignmentId, complaintId, request))
                .isInstanceOf(ComplaintAssignmentException.class);
    }
}
