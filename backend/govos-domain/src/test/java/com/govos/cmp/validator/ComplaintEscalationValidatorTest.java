package com.govos.cmp.validator;

import com.govos.cmp.dto.ComplaintEscalationCreateRequest;
import com.govos.cmp.enums.ComplaintEscalationLevel;
import com.govos.cmp.enums.ComplaintEscalationReason;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.cmp.exception.ComplaintEscalationException;
import com.govos.cmp.exception.ComplaintLifecycleException;
import com.govos.cmp.exception.ComplaintNotFoundException;
import com.govos.cmp.repository.ComplaintRepository;
import com.govos.cmp.support.CmpTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplaintEscalationValidatorTest {

    @Mock private ComplaintRepository complaintRepository;

    private ComplaintEscalationValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ComplaintEscalationValidator(complaintRepository);
    }

    @Test
    void shouldValidateCreateWhenInputValid() {
        UUID complaintId = UUID.randomUUID();
        ComplaintEscalationCreateRequest request = CmpTestFixtures.escalationCreateRequest(complaintId);
        when(complaintRepository.findByIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(complaintId, ComplaintStatus.IN_PROGRESS)));

        assertThatCode(() -> validator.validateCreate(request)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectWhenComplaintNotFound() {
        UUID complaintId = UUID.randomUUID();
        ComplaintEscalationCreateRequest request = CmpTestFixtures.escalationCreateRequest(complaintId);
        when(complaintRepository.findByIdAndDeletedFalse(complaintId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintNotFoundException.class);
    }

    @Test
    void shouldRejectArchivedComplaint() {
        UUID complaintId = UUID.randomUUID();
        ComplaintEscalationCreateRequest request = CmpTestFixtures.escalationCreateRequest(complaintId);
        when(complaintRepository.findByIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(complaintId, ComplaintStatus.ARCHIVED)));

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintLifecycleException.class);
    }

    @Test
    void shouldRejectMissingLevel() {
        UUID complaintId = UUID.randomUUID();
        ComplaintEscalationCreateRequest request = new ComplaintEscalationCreateRequest(
                complaintId, null, ComplaintEscalationReason.SLA_BREACH,
                CmpTestFixtures.OFFICER_ID, null, CmpTestFixtures.DEPARTMENT_ID, null,
                Instant.now(), true);
        when(complaintRepository.findByIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(complaintId, ComplaintStatus.IN_PROGRESS)));

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintEscalationException.class);
    }

    @Test
    void shouldRejectMissingReason() {
        UUID complaintId = UUID.randomUUID();
        ComplaintEscalationCreateRequest request = new ComplaintEscalationCreateRequest(
                complaintId, ComplaintEscalationLevel.L2, null,
                CmpTestFixtures.OFFICER_ID, null, CmpTestFixtures.DEPARTMENT_ID, null,
                Instant.now(), true);
        when(complaintRepository.findByIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(complaintId, ComplaintStatus.IN_PROGRESS)));

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintEscalationException.class);
    }
}
