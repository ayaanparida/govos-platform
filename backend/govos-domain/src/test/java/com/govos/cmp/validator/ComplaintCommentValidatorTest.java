package com.govos.cmp.validator;

import com.govos.cmp.dto.ComplaintCommentCreateRequest;
import com.govos.cmp.enums.ComplaintCommentType;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.cmp.enums.ComplaintVisibility;
import com.govos.cmp.exception.ComplaintCommentException;
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
class ComplaintCommentValidatorTest {

    @Mock private ComplaintRepository complaintRepository;

    private ComplaintCommentValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ComplaintCommentValidator(complaintRepository);
    }

    @Test
    void shouldValidateCreateWhenInputValid() {
        UUID complaintId = UUID.randomUUID();
        ComplaintCommentCreateRequest request = CmpTestFixtures.commentCreateRequest(complaintId);
        when(complaintRepository.findByIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(complaintId, ComplaintStatus.IN_PROGRESS)));

        assertThatCode(() -> validator.validateCreate(request)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectWhenComplaintNotFound() {
        UUID complaintId = UUID.randomUUID();
        ComplaintCommentCreateRequest request = CmpTestFixtures.commentCreateRequest(complaintId);
        when(complaintRepository.findByIdAndDeletedFalse(complaintId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintNotFoundException.class);
    }

    @Test
    void shouldRejectArchivedComplaint() {
        UUID complaintId = UUID.randomUUID();
        ComplaintCommentCreateRequest request = CmpTestFixtures.commentCreateRequest(complaintId);
        when(complaintRepository.findByIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(complaintId, ComplaintStatus.ARCHIVED)));

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintLifecycleException.class);
    }

    @Test
    void shouldRejectBlankCommentText() {
        UUID complaintId = UUID.randomUUID();
        ComplaintCommentCreateRequest request = new ComplaintCommentCreateRequest(
                complaintId, CmpTestFixtures.OFFICER_ID, " ", ComplaintVisibility.INTERNAL,
                ComplaintCommentType.REMARK, true);
        when(complaintRepository.findByIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(complaintId, ComplaintStatus.IN_PROGRESS)));

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintCommentException.class);
    }

    @Test
    void shouldRejectMissingVisibility() {
        UUID complaintId = UUID.randomUUID();
        ComplaintCommentCreateRequest request = new ComplaintCommentCreateRequest(
                complaintId, CmpTestFixtures.OFFICER_ID, "text", null,
                ComplaintCommentType.REMARK, true);
        when(complaintRepository.findByIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(complaintId, ComplaintStatus.IN_PROGRESS)));

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintCommentException.class);
    }
}
