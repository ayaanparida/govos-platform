package com.govos.cmp.validator;

import com.govos.cmp.dto.ComplaintCreateRequest;
import com.govos.cmp.dto.ComplaintUpdateRequest;
import com.govos.cmp.entity.Complaint;
import com.govos.cmp.enums.ComplaintPriority;
import com.govos.cmp.enums.ComplaintSource;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.cmp.exception.ComplaintAlreadyExistsException;
import com.govos.cmp.exception.ComplaintCategoryException;
import com.govos.cmp.exception.ComplaintLifecycleException;
import com.govos.cmp.exception.ComplaintLocationException;
import com.govos.cmp.exception.ComplaintNotFoundException;
import com.govos.cmp.exception.ComplaintStateTransitionException;
import com.govos.cmp.exception.ComplaintValidationException;
import com.govos.cmp.repository.ComplaintRepository;
import com.govos.cmp.support.CmpTestFixtures;
import com.govos.mdm.repository.MasterDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplaintValidatorTest {

    @Mock private ComplaintRepository complaintRepository;
    @Mock private MasterDataRepository masterDataRepository;

    private ComplaintValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ComplaintValidator(complaintRepository, masterDataRepository);
    }

    @Test
    void shouldValidateCreateWhenInputValid() {
        ComplaintCreateRequest request = CmpTestFixtures.createRequest();
        stubCategoryMasterData();

        assertThatCode(() -> validator.validateCreate(request)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectCreateWhenTitleMissing() {
        ComplaintCreateRequest request = new ComplaintCreateRequest(
                " ", "desc", ComplaintPriority.MEDIUM, ComplaintSource.CITIZEN_PORTAL, null,
                CmpTestFixtures.CATEGORY_KEY, CmpTestFixtures.SUB_CATEGORY_KEY, null,
                CmpTestFixtures.CITIZEN_ID, null, CmpTestFixtures.ORG_ID, null, null,
                "KA", "BLR", null, null, null, null, null, null, null, null, null, true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintValidationException.class);
    }

    @Test
    void shouldRejectCreateWhenOrganizationMissing() {
        ComplaintCreateRequest request = new ComplaintCreateRequest(
                "Title", "desc", ComplaintPriority.MEDIUM, ComplaintSource.CITIZEN_PORTAL, null,
                CmpTestFixtures.CATEGORY_KEY, CmpTestFixtures.SUB_CATEGORY_KEY, null,
                CmpTestFixtures.CITIZEN_ID, null, null, null, null,
                "KA", "BLR", null, null, null, null, null, null, null, null, null, true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintValidationException.class);
    }

    @Test
    void shouldRejectCreateWhenCategoryMissing() {
        ComplaintCreateRequest request = new ComplaintCreateRequest(
                "Title", "desc", ComplaintPriority.MEDIUM, ComplaintSource.CITIZEN_PORTAL, null,
                null, null, null, CmpTestFixtures.CITIZEN_ID, null, CmpTestFixtures.ORG_ID, null, null,
                "KA", "BLR", null, null, null, null, null, null, null, null, null, true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintCategoryException.class);
    }

    @Test
    void shouldRejectCreateWhenSubCategoryMissing() {
        ComplaintCreateRequest request = new ComplaintCreateRequest(
                "Title", "desc", ComplaintPriority.MEDIUM, ComplaintSource.CITIZEN_PORTAL, null,
                CmpTestFixtures.CATEGORY_KEY, null, null, CmpTestFixtures.CITIZEN_ID, null,
                CmpTestFixtures.ORG_ID, null, null, "KA", "BLR", null, null, null,
                null, null, null, null, null, null, true);

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintCategoryException.class);
    }

    @Test
    void shouldRejectCreateWhenCategoryUnknown() {
        ComplaintCreateRequest request = CmpTestFixtures.createRequest();
        when(masterDataRepository.findByTypeAndKeyAndDeletedFalse("COMPLAINT_CATEGORY", request.categoryKey()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintCategoryException.class);
    }

    @Test
    void shouldRejectCreateWhenLocationIncomplete() {
        ComplaintCreateRequest request = new ComplaintCreateRequest(
                "Title", "desc", ComplaintPriority.MEDIUM, ComplaintSource.CITIZEN_PORTAL, null,
                CmpTestFixtures.CATEGORY_KEY, CmpTestFixtures.SUB_CATEGORY_KEY, null,
                CmpTestFixtures.CITIZEN_ID, null, CmpTestFixtures.ORG_ID, null, null,
                null, null, null, null, null, null, null, null, null, null, null, true);
        stubCategoryMasterData();

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintLocationException.class);
    }

    @Test
    void shouldRejectCreateWhenLatitudeOutOfRange() {
        ComplaintCreateRequest request = new ComplaintCreateRequest(
                "Title", "desc", ComplaintPriority.MEDIUM, ComplaintSource.CITIZEN_PORTAL, null,
                CmpTestFixtures.CATEGORY_KEY, CmpTestFixtures.SUB_CATEGORY_KEY, null,
                CmpTestFixtures.CITIZEN_ID, null, CmpTestFixtures.ORG_ID, null, null,
                "KA", "BLR", null, null, null, new BigDecimal("95"), null,
                null, null, null, null, true);
        stubCategoryMasterData();

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(ComplaintLocationException.class);
    }

    @Test
    void shouldValidateUpdateWhenComplaintExists() {
        UUID id = UUID.randomUUID();
        ComplaintUpdateRequest request = CmpTestFixtures.updateRequest();
        when(complaintRepository.findByIdAndDeletedFalse(id))
                .thenReturn(Optional.of(CmpTestFixtures.complaint(id, ComplaintStatus.DRAFT)));
        stubCategoryMasterData();

        assertThatCode(() -> validator.validateUpdate(id, request)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectDuplicateCode() {
        when(complaintRepository.existsByCodeAndDeletedFalse("CMP-001")).thenReturn(true);

        assertThatThrownBy(() -> validator.validateCodeUniqueness("CMP-001"))
                .isInstanceOf(ComplaintAlreadyExistsException.class);
    }

    @Test
    void shouldRejectDuplicateCodeForOtherComplaint() {
        UUID id = UUID.randomUUID();
        Complaint other = CmpTestFixtures.complaint(UUID.randomUUID(), ComplaintStatus.DRAFT);
        when(complaintRepository.findByCodeAndDeletedFalse("CMP-001")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> validator.validateCodeUniqueness("CMP-001", id))
                .isInstanceOf(ComplaintAlreadyExistsException.class);
    }

    @Test
    void shouldAllowSameCodeForSameComplaint() {
        UUID id = UUID.randomUUID();
        Complaint complaint = CmpTestFixtures.complaint(id, ComplaintStatus.DRAFT);
        when(complaintRepository.findByCodeAndDeletedFalse("CMP-001")).thenReturn(Optional.of(complaint));

        assertThatCode(() -> validator.validateCodeUniqueness("CMP-001", id)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectDeletedComplaint() {
        Complaint complaint = CmpTestFixtures.complaint(UUID.randomUUID(), ComplaintStatus.DRAFT);
        complaint.setDeleted(true);

        assertThatThrownBy(() -> validator.validateNotDeleted(complaint))
                .isInstanceOf(ComplaintValidationException.class);
    }

    @Test
    void shouldRejectTerminalStatusForLifecycle() {
        Complaint complaint = CmpTestFixtures.complaint(UUID.randomUUID(), ComplaintStatus.ARCHIVED);
        when(complaintRepository.findByIdAndDeletedFalse(complaint.getId())).thenReturn(Optional.of(complaint));

        assertThatThrownBy(() -> validator.validateLifecycleEligible(complaint))
                .isInstanceOf(ComplaintLifecycleException.class);
    }

    @Test
    void shouldRejectIllegalTransition() {
        Complaint complaint = CmpTestFixtures.complaint(UUID.randomUUID(), ComplaintStatus.DRAFT);
        when(complaintRepository.findByIdAndDeletedFalse(complaint.getId())).thenReturn(Optional.of(complaint));

        assertThatThrownBy(() -> validator.validateTransition(complaint, ComplaintStatus.ACCEPTED))
                .isInstanceOf(ComplaintStateTransitionException.class);
    }

    @Test
    void shouldAllowValidTransition() {
        Complaint complaint = CmpTestFixtures.complaint(UUID.randomUUID(), ComplaintStatus.DRAFT);
        when(complaintRepository.findByIdAndDeletedFalse(complaint.getId())).thenReturn(Optional.of(complaint));

        assertThatCode(() -> validator.validateTransition(complaint, ComplaintStatus.SUBMITTED))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectAssignFromInvalidStatus() {
        Complaint complaint = CmpTestFixtures.complaint(UUID.randomUUID(), ComplaintStatus.DRAFT);
        when(complaintRepository.findByIdAndDeletedFalse(complaint.getId())).thenReturn(Optional.of(complaint));

        assertThatThrownBy(() -> validator.validateAssignTransition(complaint))
                .isInstanceOf(ComplaintStateTransitionException.class);
    }

    @Test
    void shouldRejectSubmitWhenLocationMissing() {
        Complaint complaint = CmpTestFixtures.complaint(UUID.randomUUID(), ComplaintStatus.DRAFT);
        complaint.setLocation(null);
        when(complaintRepository.findByIdAndDeletedFalse(complaint.getId())).thenReturn(Optional.of(complaint));
        stubCategoryMasterData();

        assertThatThrownBy(() -> validator.validateReadyForSubmit(complaint))
                .isInstanceOf(ComplaintLocationException.class);
    }

    @Test
    void shouldRejectAlreadyDuplicateComplaint() {
        Complaint complaint = CmpTestFixtures.complaint(UUID.randomUUID(), ComplaintStatus.IN_PROGRESS);
        complaint.setIsDuplicate(true);
        when(complaintRepository.findByIdAndDeletedFalse(complaint.getId())).thenReturn(Optional.of(complaint));

        assertThatThrownBy(() -> validator.validateMarkDuplicateEligible(complaint))
                .isInstanceOf(ComplaintLifecycleException.class);
    }

    @Test
    void shouldRejectArchiveWhenNotClosed() {
        Complaint complaint = CmpTestFixtures.complaint(UUID.randomUUID(), ComplaintStatus.IN_PROGRESS);
        when(complaintRepository.findByIdAndDeletedFalse(complaint.getId())).thenReturn(Optional.of(complaint));

        assertThatThrownBy(() -> validator.validateArchiveEligible(complaint))
                .isInstanceOf(ComplaintStateTransitionException.class);
    }

    @Test
    void shouldRejectReopenWhenNotClosed() {
        Complaint complaint = CmpTestFixtures.complaint(UUID.randomUUID(), ComplaintStatus.RESOLVED);
        when(complaintRepository.findByIdAndDeletedFalse(complaint.getId())).thenReturn(Optional.of(complaint));

        assertThatThrownBy(() -> validator.validateReopenEligible(complaint))
                .isInstanceOf(ComplaintStateTransitionException.class);
    }

    @Test
    void shouldRequireExists() {
        UUID id = UUID.randomUUID();
        Complaint complaint = CmpTestFixtures.complaint(id, ComplaintStatus.DRAFT);
        when(complaintRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(complaint));

        assertThat(validator.requireExists(id)).isEqualTo(complaint);
    }

    @Test
    void shouldThrowWhenRequireExistsNotFound() {
        UUID id = UUID.randomUUID();
        when(complaintRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.requireExists(id))
                .isInstanceOf(ComplaintNotFoundException.class);
    }

    private void stubCategoryMasterData() {
        when(masterDataRepository.findByTypeAndKeyAndDeletedFalse("COMPLAINT_CATEGORY", CmpTestFixtures.CATEGORY_KEY))
                .thenReturn(Optional.of(CmpTestFixtures.masterData("COMPLAINT_CATEGORY", CmpTestFixtures.CATEGORY_KEY)));
        when(masterDataRepository.findByTypeAndKeyAndDeletedFalse("COMPLAINT_SUB_CATEGORY", CmpTestFixtures.SUB_CATEGORY_KEY))
                .thenReturn(Optional.of(CmpTestFixtures.masterData("COMPLAINT_SUB_CATEGORY", CmpTestFixtures.SUB_CATEGORY_KEY)));
    }
}
