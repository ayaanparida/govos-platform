package com.govos.cmp.validator;

import com.govos.cmp.dto.ComplaintAttachmentCreateRequest;
import com.govos.cmp.enums.ComplaintAttachmentType;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.cmp.exception.ComplaintAttachmentException;
import com.govos.cmp.exception.ComplaintLifecycleException;
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
class ComplaintAttachmentValidatorTest {

    @Mock private ComplaintRepository complaintRepository;

    private ComplaintAttachmentValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ComplaintAttachmentValidator(complaintRepository);
    }

    @Test
    void shouldValidateCreateWhenInputValid() {
        UUID complaintId = UUID.randomUUID();
        ComplaintAttachmentCreateRequest request = CmpTestFixtures.attachmentCreateRequest(complaintId);
        when(complaintRepository.findByIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(complaintId, ComplaintStatus.IN_PROGRESS)));

        assertThatCode(() -> validator.validateCreate(request)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectWhenComplaintNotFound() {
        UUID complaintId = UUID.randomUUID();
        ComplaintAttachmentCreateRequest request = CmpTestFixtures.attachmentCreateRequest(complaintId);
        when(complaintRepository.findByIdAndDeletedFalse(complaintId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintNotFoundException.class);
    }

    @Test
    void shouldRejectArchivedComplaint() {
        UUID complaintId = UUID.randomUUID();
        ComplaintAttachmentCreateRequest request = CmpTestFixtures.attachmentCreateRequest(complaintId);
        when(complaintRepository.findByIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(complaintId, ComplaintStatus.ARCHIVED)));

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintLifecycleException.class);
    }

    @Test
    void shouldRejectMissingDocumentId() {
        UUID complaintId = UUID.randomUUID();
        ComplaintAttachmentCreateRequest request = new ComplaintAttachmentCreateRequest(
                complaintId, null, null, ComplaintAttachmentType.DOCUMENT, "file",
                CmpTestFixtures.OFFICER_ID, null, true);
        when(complaintRepository.findByIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(complaintId, ComplaintStatus.IN_PROGRESS)));

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintAttachmentException.class);
    }

    @Test
    void shouldRejectMissingAttachmentType() {
        UUID complaintId = UUID.randomUUID();
        ComplaintAttachmentCreateRequest request = new ComplaintAttachmentCreateRequest(
                complaintId, CmpTestFixtures.DOCUMENT_ID, null, null, "file",
                CmpTestFixtures.OFFICER_ID, null, true);
        when(complaintRepository.findByIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(complaintId, ComplaintStatus.IN_PROGRESS)));

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintAttachmentException.class);
    }
}
