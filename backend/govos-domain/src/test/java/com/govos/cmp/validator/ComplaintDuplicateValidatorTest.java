package com.govos.cmp.validator;

import com.govos.cmp.dto.ComplaintDuplicateCreateRequest;
import com.govos.cmp.exception.ComplaintDuplicateException;
import com.govos.cmp.exception.ComplaintNotFoundException;
import com.govos.cmp.repository.ComplaintDuplicateRepository;
import com.govos.cmp.repository.ComplaintRepository;
import com.govos.cmp.support.CmpTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplaintDuplicateValidatorTest {

    @Mock private ComplaintRepository complaintRepository;
    @Mock private ComplaintDuplicateRepository complaintDuplicateRepository;

    private ComplaintDuplicateValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ComplaintDuplicateValidator(complaintRepository, complaintDuplicateRepository);
    }

    @Test
    void shouldValidateCreateWhenInputValid() {
        UUID primaryId = UUID.randomUUID();
        UUID duplicateId = UUID.randomUUID();
        ComplaintDuplicateCreateRequest request = CmpTestFixtures.duplicateCreateRequest(primaryId, duplicateId);

        when(complaintRepository.findByIdAndDeletedFalse(primaryId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(primaryId, com.govos.cmp.enums.ComplaintStatus.IN_PROGRESS)));
        when(complaintRepository.findByIdAndDeletedFalse(duplicateId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(duplicateId, com.govos.cmp.enums.ComplaintStatus.IN_PROGRESS)));
        when(complaintDuplicateRepository.findAllByPrimaryComplaintIdAndDeletedFalse(primaryId))
                .thenReturn(List.of());

        assertThatCode(() -> validator.validateCreate(request)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectSelfDuplicate() {
        UUID complaintId = UUID.randomUUID();
        ComplaintDuplicateCreateRequest request = CmpTestFixtures.duplicateCreateRequest(complaintId, complaintId);

        when(complaintRepository.findByIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(complaintId, com.govos.cmp.enums.ComplaintStatus.IN_PROGRESS)));

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintDuplicateException.class);
    }

    @Test
    void shouldRejectWhenPrimaryNotFound() {
        UUID primaryId = UUID.randomUUID();
        UUID duplicateId = UUID.randomUUID();
        ComplaintDuplicateCreateRequest request = CmpTestFixtures.duplicateCreateRequest(primaryId, duplicateId);

        when(complaintRepository.findByIdAndDeletedFalse(primaryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintNotFoundException.class);
    }

    @Test
    void shouldRejectDuplicatePairAlreadyExists() {
        UUID primaryId = UUID.randomUUID();
        UUID duplicateId = UUID.randomUUID();
        ComplaintDuplicateCreateRequest request = CmpTestFixtures.duplicateCreateRequest(primaryId, duplicateId);

        when(complaintRepository.findByIdAndDeletedFalse(primaryId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(primaryId, com.govos.cmp.enums.ComplaintStatus.IN_PROGRESS)));
        when(complaintRepository.findByIdAndDeletedFalse(duplicateId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(duplicateId, com.govos.cmp.enums.ComplaintStatus.IN_PROGRESS)));
        when(complaintDuplicateRepository.findAllByPrimaryComplaintIdAndDeletedFalse(primaryId))
                .thenReturn(List.of(CmpTestFixtures.duplicateLink(UUID.randomUUID(), primaryId, duplicateId)));

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintDuplicateException.class);
    }
}
