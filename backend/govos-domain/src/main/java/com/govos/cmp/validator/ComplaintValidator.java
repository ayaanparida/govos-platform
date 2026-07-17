package com.govos.cmp.validator;

import com.govos.cmp.dto.ComplaintCreateRequest;
import com.govos.cmp.dto.ComplaintUpdateRequest;
import com.govos.cmp.entity.Complaint;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.cmp.exception.ComplaintAlreadyExistsException;
import com.govos.cmp.exception.ComplaintCategoryException;
import com.govos.cmp.exception.ComplaintLifecycleException;
import com.govos.cmp.exception.ComplaintLocationException;
import com.govos.cmp.exception.ComplaintNotFoundException;
import com.govos.cmp.exception.ComplaintStateTransitionException;
import com.govos.cmp.exception.ComplaintValidationException;
import com.govos.cmp.repository.ComplaintRepository;
import com.govos.cmp.valueobject.ComplaintLocation;
import com.govos.mdm.repository.MasterDataRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class ComplaintValidator {

    private static final String MDM_COMPLAINT_CATEGORY = "COMPLAINT_CATEGORY";
    private static final String MDM_COMPLAINT_SUB_CATEGORY = "COMPLAINT_SUB_CATEGORY";

    private static final BigDecimal LATITUDE_MIN = new BigDecimal("-90");
    private static final BigDecimal LATITUDE_MAX = new BigDecimal("90");
    private static final BigDecimal LONGITUDE_MIN = new BigDecimal("-180");
    private static final BigDecimal LONGITUDE_MAX = new BigDecimal("180");

    private static final Set<ComplaintStatus> TERMINAL_STATUSES = EnumSet.of(
            ComplaintStatus.ARCHIVED,
            ComplaintStatus.REJECTED,
            ComplaintStatus.CANCELLED);

    private static final Map<ComplaintStatus, Set<ComplaintStatus>> ALLOWED_TRANSITIONS = Map.ofEntries(
            Map.entry(ComplaintStatus.DRAFT, Set.of(ComplaintStatus.SUBMITTED)),
            Map.entry(ComplaintStatus.SUBMITTED, Set.of(ComplaintStatus.ACCEPTED, ComplaintStatus.REJECTED)),
            Map.entry(ComplaintStatus.ACCEPTED, Set.of(ComplaintStatus.ASSIGNED)),
            Map.entry(ComplaintStatus.ASSIGNED, Set.of(ComplaintStatus.IN_PROGRESS, ComplaintStatus.PENDING_REASSIGNMENT)),
            Map.entry(ComplaintStatus.PENDING_REASSIGNMENT, Set.of(ComplaintStatus.ASSIGNED)),
            Map.entry(ComplaintStatus.IN_PROGRESS, Set.of(ComplaintStatus.RESOLVED)),
            Map.entry(ComplaintStatus.RESOLVED, Set.of(ComplaintStatus.CLOSED)),
            Map.entry(ComplaintStatus.CLOSED, Set.of(ComplaintStatus.ARCHIVED, ComplaintStatus.REOPENED)),
            Map.entry(ComplaintStatus.REOPENED, Set.of(ComplaintStatus.ASSIGNED)));

    private final ComplaintRepository complaintRepository;
    private final MasterDataRepository masterDataRepository;

    public ComplaintValidator(
            ComplaintRepository complaintRepository,
            MasterDataRepository masterDataRepository) {
        this.complaintRepository = complaintRepository;
        this.masterDataRepository = masterDataRepository;
    }

    public Complaint requireExists(UUID id) {
        return complaintRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ComplaintNotFoundException(id));
    }

    public void validateCreate(ComplaintCreateRequest request) {
        validateTitle(request.title());
        validateDescription(request.description());
        validateOrganizationMandatory(request.organizationId());
        validateCitizenMandatory(request.citizenUserId());
        validateCategoryMandatory(request.categoryKey());
        validateSubCategoryWhenRequired(request.categoryKey(), request.subCategoryKey());
        validateCategoryKeys(request.categoryKey(), request.subCategoryKey());
        validateLocation(
                request.stateKey(),
                request.districtKey(),
                request.latitude(),
                request.longitude());
    }

    public void validateUpdate(UUID id, ComplaintUpdateRequest request) {
        requireExists(id);
        validateTitle(request.title());
        validateDescription(request.description());
        validateSubCategoryWhenRequired(request.categoryKey(), request.subCategoryKey());
        validateCategoryKeys(request.categoryKey(), request.subCategoryKey());
        validateLocation(
                request.stateKey(),
                request.districtKey(),
                request.latitude(),
                request.longitude());
    }

    public void validateCodeUniqueness(String code) {
        if (StringUtils.hasText(code) && complaintRepository.existsByCodeAndDeletedFalse(code)) {
            throw new ComplaintAlreadyExistsException(code);
        }
    }

    public void validateCodeUniqueness(String code, UUID excludeId) {
        if (!StringUtils.hasText(code)) {
            return;
        }
        complaintRepository.findByCodeAndDeletedFalse(code)
                .filter(complaint -> !complaint.getId().equals(excludeId))
                .ifPresent(complaint -> {
                    throw new ComplaintAlreadyExistsException(code);
                });
    }

    public void validateNotDeleted(Complaint complaint) {
        if (Boolean.TRUE.equals(complaint.getDeleted())) {
            throw new ComplaintValidationException("Complaint is deleted: " + complaint.getId());
        }
    }

    public void validateLifecycleEligible(Complaint complaint) {
        requireExists(complaint.getId());
        validateNotDeleted(complaint);
        if (Boolean.FALSE.equals(complaint.getActive())) {
            throw new ComplaintLifecycleException("Complaint is inactive: " + complaint.getId());
        }
        if (TERMINAL_STATUSES.contains(complaint.getStatus())) {
            throw new ComplaintLifecycleException(
                    "Complaint is in terminal status " + complaint.getStatus() + ": " + complaint.getId());
        }
    }

    public void validateTransition(Complaint complaint, ComplaintStatus targetStatus) {
        validateLifecycleEligible(complaint);
        ComplaintStatus currentStatus = complaint.getStatus();
        Set<ComplaintStatus> allowedTargets = ALLOWED_TRANSITIONS.get(currentStatus);
        if (allowedTargets == null || !allowedTargets.contains(targetStatus)) {
            throw new ComplaintStateTransitionException(
                    "Illegal transition from " + currentStatus + " to " + targetStatus
                            + " for complaint: " + complaint.getId());
        }
    }

    public void validateAssignTransition(Complaint complaint) {
        validateLifecycleEligible(complaint);
        ComplaintStatus currentStatus = complaint.getStatus();
        if (currentStatus != ComplaintStatus.ACCEPTED
                && currentStatus != ComplaintStatus.PENDING_REASSIGNMENT
                && currentStatus != ComplaintStatus.REOPENED) {
            throw new ComplaintStateTransitionException(
                    "Assignment requires status ACCEPTED, PENDING_REASSIGNMENT, or REOPENED; current="
                            + currentStatus + " for complaint: " + complaint.getId());
        }
    }

    public void validateReadyForSubmit(Complaint complaint) {
        validateLifecycleEligible(complaint);
        validateTitle(complaint.getTitle());
        validateDescription(complaint.getDescription());
        validateOrganizationMandatory(complaint.getOrganizationId());
        validateCitizenMandatory(complaint.getCitizenUserId());
        validateCategoryMandatory(complaint.getCategoryKey());
        validateSubCategoryWhenRequired(complaint.getCategoryKey(), complaint.getSubCategoryKey());
        validateCategoryKeys(complaint.getCategoryKey(), complaint.getSubCategoryKey());
        ComplaintLocation location = complaint.getLocation();
        if (location == null) {
            throw new ComplaintLocationException("Complaint location requires state and district");
        }
        validateLocation(
                location.getStateKey(),
                location.getDistrictKey(),
                location.getLatitude(),
                location.getLongitude());
    }

    public void validateMarkDuplicateEligible(Complaint complaint) {
        validateLifecycleEligible(complaint);
        if (Boolean.TRUE.equals(complaint.getIsDuplicate())) {
            throw new ComplaintLifecycleException("Complaint is already marked duplicate: " + complaint.getId());
        }
    }

    public void validateMergeEligible(Complaint complaint) {
        validateLifecycleEligible(complaint);
    }

    public void validateArchiveEligible(Complaint complaint) {
        validateLifecycleEligible(complaint);
        if (complaint.getStatus() != ComplaintStatus.CLOSED) {
            throw new ComplaintStateTransitionException(
                    "Archive requires CLOSED status; current=" + complaint.getStatus()
                            + " for complaint: " + complaint.getId());
        }
    }

    public void validateReopenEligible(Complaint complaint) {
        validateLifecycleEligible(complaint);
        if (complaint.getStatus() != ComplaintStatus.CLOSED) {
            throw new ComplaintStateTransitionException(
                    "Reopen requires CLOSED status; current=" + complaint.getStatus()
                            + " for complaint: " + complaint.getId());
        }
    }

    private void validateTitle(String title) {
        if (!StringUtils.hasText(title)) {
            throw new ComplaintValidationException("Complaint title is required");
        }
    }

    private void validateDescription(String description) {
        if (!StringUtils.hasText(description)) {
            throw new ComplaintValidationException("Complaint description is required");
        }
    }

    private void validateOrganizationMandatory(UUID organizationId) {
        if (organizationId == null) {
            throw new ComplaintValidationException("Organization is mandatory for a complaint");
        }
    }

    private void validateCitizenMandatory(UUID citizenUserId) {
        if (citizenUserId == null) {
            throw new ComplaintValidationException("Citizen is mandatory for a complaint");
        }
    }

    private void validateCategoryMandatory(String categoryKey) {
        if (!StringUtils.hasText(categoryKey)) {
            throw new ComplaintCategoryException("Complaint category is mandatory");
        }
    }

    private void validateSubCategoryWhenRequired(String categoryKey, String subCategoryKey) {
        if (StringUtils.hasText(categoryKey) && !StringUtils.hasText(subCategoryKey)) {
            throw new ComplaintCategoryException("Complaint subcategory is required when category is set");
        }
    }

    private void validateCategoryKeys(String categoryKey, String subCategoryKey) {
        if (StringUtils.hasText(categoryKey)) {
            masterDataRepository.findByTypeAndKeyAndDeletedFalse(MDM_COMPLAINT_CATEGORY, categoryKey)
                    .orElseThrow(() -> new ComplaintCategoryException(MDM_COMPLAINT_CATEGORY, categoryKey));
        }
        if (StringUtils.hasText(subCategoryKey)) {
            masterDataRepository.findByTypeAndKeyAndDeletedFalse(MDM_COMPLAINT_SUB_CATEGORY, subCategoryKey)
                    .orElseThrow(() -> new ComplaintCategoryException(MDM_COMPLAINT_SUB_CATEGORY, subCategoryKey));
        }
    }

    private void validateLocation(
            String stateKey,
            String districtKey,
            BigDecimal latitude,
            BigDecimal longitude) {
        if (!StringUtils.hasText(stateKey) || !StringUtils.hasText(districtKey)) {
            throw new ComplaintLocationException("Complaint location requires state and district");
        }
        validateCoordinate(latitude, LATITUDE_MIN, LATITUDE_MAX, "latitude");
        validateCoordinate(longitude, LONGITUDE_MIN, LONGITUDE_MAX, "longitude");
    }

    private void validateCoordinate(BigDecimal value, BigDecimal min, BigDecimal max, String name) {
        if (value == null) {
            return;
        }
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new ComplaintLocationException(
                    "Complaint " + name + " must be between " + min + " and " + max);
        }
    }
}
