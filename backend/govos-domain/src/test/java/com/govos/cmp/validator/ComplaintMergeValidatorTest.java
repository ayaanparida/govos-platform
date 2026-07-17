package com.govos.cmp.validator;

import com.govos.cmp.dto.ComplaintMergeCreateRequest;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.cmp.exception.ComplaintLifecycleException;
import com.govos.cmp.exception.ComplaintMergeException;
import com.govos.cmp.exception.ComplaintNotFoundException;
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
class ComplaintMergeValidatorTest {

    @Mock private ComplaintRepository complaintRepository;

    private ComplaintMergeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ComplaintMergeValidator(complaintRepository);
    }

    @Test
    void shouldValidateCreateWhenInputValid() {
        UUID survivingId = UUID.randomUUID();
        UUID mergedId = UUID.randomUUID();
        ComplaintMergeCreateRequest request = CmpTestFixtures.mergeCreateRequest(survivingId, mergedId);

        when(complaintRepository.findByIdAndDeletedFalse(survivingId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(survivingId, ComplaintStatus.IN_PROGRESS)));
        when(complaintRepository.findByIdAndDeletedFalse(mergedId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(mergedId, ComplaintStatus.IN_PROGRESS)));

        assertThatCode(() -> validator.validateCreate(request)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectSelfMerge() {
        UUID complaintId = UUID.randomUUID();
        ComplaintMergeCreateRequest request = CmpTestFixtures.mergeCreateRequest(complaintId, complaintId);

        when(complaintRepository.findByIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(complaintId, ComplaintStatus.IN_PROGRESS)));

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintMergeException.class);
    }

    @Test
    void shouldRejectWhenSurvivingNotFound() {
        UUID survivingId = UUID.randomUUID();
        UUID mergedId = UUID.randomUUID();
        ComplaintMergeCreateRequest request = CmpTestFixtures.mergeCreateRequest(survivingId, mergedId);

        when(complaintRepository.findByIdAndDeletedFalse(survivingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintNotFoundException.class);
    }

    @Test
    void shouldRejectClosedComplaintMerge() {
        UUID survivingId = UUID.randomUUID();
        UUID mergedId = UUID.randomUUID();
        ComplaintMergeCreateRequest request = CmpTestFixtures.mergeCreateRequest(survivingId, mergedId);

        when(complaintRepository.findByIdAndDeletedFalse(survivingId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(survivingId, ComplaintStatus.CLOSED)));
        when(complaintRepository.findByIdAndDeletedFalse(mergedId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(mergedId, ComplaintStatus.IN_PROGRESS)));

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintLifecycleException.class);
    }

    @Test
    void shouldRejectArchivedComplaintMerge() {
        UUID survivingId = UUID.randomUUID();
        UUID mergedId = UUID.randomUUID();
        ComplaintMergeCreateRequest request = CmpTestFixtures.mergeCreateRequest(survivingId, mergedId);

        when(complaintRepository.findByIdAndDeletedFalse(survivingId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(survivingId, ComplaintStatus.IN_PROGRESS)));
        when(complaintRepository.findByIdAndDeletedFalse(mergedId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(mergedId, ComplaintStatus.ARCHIVED)));

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintLifecycleException.class);
    }
}
