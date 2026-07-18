package com.govos.cmp.validator;

import com.govos.cmp.dto.ComplaintFeedbackCreateRequest;
import com.govos.cmp.dto.ComplaintFeedbackUpdateRequest;
import com.govos.cmp.entity.ComplaintFeedback;
import com.govos.cmp.enums.ComplaintFeedbackRating;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.cmp.exception.ComplaintFeedbackException;
import com.govos.cmp.exception.ComplaintLifecycleException;
import com.govos.cmp.exception.ComplaintNotFoundException;
import com.govos.cmp.repository.ComplaintFeedbackRepository;
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
class ComplaintFeedbackValidatorTest {

    @Mock private ComplaintRepository complaintRepository;
    @Mock private ComplaintFeedbackRepository complaintFeedbackRepository;

    private ComplaintFeedbackValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ComplaintFeedbackValidator(complaintRepository, complaintFeedbackRepository);
    }

    @Test
    void shouldValidateCreateWhenInputValid() {
        UUID complaintId = UUID.randomUUID();
        ComplaintFeedbackCreateRequest request = CmpTestFixtures.feedbackCreateRequest(complaintId);
        when(complaintRepository.findByIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(complaintId, ComplaintStatus.CLOSED)));
        when(complaintFeedbackRepository.existsByComplaintIdAndDeletedFalse(complaintId)).thenReturn(false);

        assertThatCode(() -> validator.validateCreate(request)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectWhenComplaintNotFound() {
        UUID complaintId = UUID.randomUUID();
        ComplaintFeedbackCreateRequest request = CmpTestFixtures.feedbackCreateRequest(complaintId);
        when(complaintRepository.findByIdAndDeletedFalse(complaintId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintNotFoundException.class);
    }

    @Test
    void shouldRejectWhenComplaintNotClosed() {
        UUID complaintId = UUID.randomUUID();
        ComplaintFeedbackCreateRequest request = CmpTestFixtures.feedbackCreateRequest(complaintId);
        when(complaintRepository.findByIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(complaintId, ComplaintStatus.IN_PROGRESS)));

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintLifecycleException.class);
    }

    @Test
    void shouldRejectDuplicateFeedback() {
        UUID complaintId = UUID.randomUUID();
        ComplaintFeedbackCreateRequest request = CmpTestFixtures.feedbackCreateRequest(complaintId);
        when(complaintRepository.findByIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(complaintId, ComplaintStatus.CLOSED)));
        when(complaintFeedbackRepository.existsByComplaintIdAndDeletedFalse(complaintId)).thenReturn(true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintFeedbackException.class);
    }

    @Test
    void shouldRejectMissingRatingOnCreate() {
        UUID complaintId = UUID.randomUUID();
        ComplaintFeedbackCreateRequest request = new ComplaintFeedbackCreateRequest(
                complaintId, CmpTestFixtures.CITIZEN_ID, null, null,
                CmpTestFixtures.feedbackCreateRequest(complaintId).ratedAt(), true);
        when(complaintRepository.findByIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(complaintId, ComplaintStatus.CLOSED)));

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintFeedbackException.class);
    }

    @Test
    void shouldValidateUpdateWhenInputValid() {
        UUID complaintId = UUID.randomUUID();
        UUID feedbackId = UUID.randomUUID();
        ComplaintFeedbackUpdateRequest request = CmpTestFixtures.feedbackUpdateRequest();
        when(complaintRepository.findByIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(complaintId, ComplaintStatus.CLOSED)));
        when(complaintFeedbackRepository.findByComplaintIdAndDeletedFalse(complaintId)).thenReturn(Optional.empty());

        assertThatCode(() -> validator.validateUpdate(feedbackId, complaintId, request)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectUpdateWhenAnotherFeedbackExists() {
        UUID complaintId = UUID.randomUUID();
        UUID feedbackId = UUID.randomUUID();
        ComplaintFeedback other = CmpTestFixtures.feedback(UUID.randomUUID(), complaintId);
        ComplaintFeedbackUpdateRequest request = CmpTestFixtures.feedbackUpdateRequest();

        when(complaintRepository.findByIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(complaintId, ComplaintStatus.CLOSED)));
        when(complaintFeedbackRepository.findByComplaintIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(other));

        assertThatThrownBy(() -> validator.validateUpdate(feedbackId, complaintId, request))
                .isInstanceOf(ComplaintFeedbackException.class);
    }

    @Test
    void shouldRejectUpdateWhenRatingMissing() {
        UUID complaintId = UUID.randomUUID();
        ComplaintFeedbackUpdateRequest request = new ComplaintFeedbackUpdateRequest(null, null, true, 0L);
        when(complaintRepository.findByIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(complaintId, ComplaintStatus.CLOSED)));

        assertThatThrownBy(() -> validator.validateUpdate(UUID.randomUUID(), complaintId, request))
                .isInstanceOf(ComplaintFeedbackException.class);
    }
}
