package com.govos.cmp.validator;

import com.govos.cmp.dto.ComplaintAssignmentCreateRequest;
import com.govos.cmp.dto.ComplaintAssignmentUpdateRequest;
import com.govos.cmp.entity.Complaint;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.cmp.exception.ComplaintAssignmentException;
import com.govos.cmp.exception.ComplaintLifecycleException;
import com.govos.cmp.exception.ComplaintNotFoundException;
import com.govos.cmp.repository.ComplaintAssignmentRepository;
import com.govos.cmp.repository.ComplaintRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ComplaintAssignmentValidator {

    private final ComplaintRepository complaintRepository;
    private final ComplaintAssignmentRepository complaintAssignmentRepository;

    public ComplaintAssignmentValidator(
            ComplaintRepository complaintRepository,
            ComplaintAssignmentRepository complaintAssignmentRepository) {
        this.complaintRepository = complaintRepository;
        this.complaintAssignmentRepository = complaintAssignmentRepository;
    }

    public void validateCreate(ComplaintAssignmentCreateRequest request) {
        Complaint complaint = requireComplaint(request.complaintId());
        validateComplaintAssignable(complaint);
        validateOfficerSupplied(request.officerUserId());
        validateCurrentAssignmentUniqueness(request.complaintId());
    }

    public void validateUpdate(UUID id, UUID complaintId, ComplaintAssignmentUpdateRequest request) {
        requireComplaint(complaintId);
        if (Boolean.TRUE.equals(request.isCurrent())) {
            complaintAssignmentRepository.findByComplaintIdAndIsCurrentTrueAndDeletedFalse(complaintId)
                    .filter(assignment -> !assignment.getId().equals(id))
                    .ifPresent(assignment -> {
                        throw new ComplaintAssignmentException(
                                "Current assignment already exists for complaint: " + complaintId);
                    });
        }
    }

    private Complaint requireComplaint(UUID complaintId) {
        return complaintRepository.findByIdAndDeletedFalse(complaintId)
                .orElseThrow(() -> new ComplaintNotFoundException(complaintId));
    }

    private void validateComplaintAssignable(Complaint complaint) {
        if (Boolean.FALSE.equals(complaint.getActive())) {
            throw new ComplaintLifecycleException("Cannot assign inactive complaint: " + complaint.getId());
        }
        if (ComplaintStatus.ARCHIVED == complaint.getStatus()) {
            throw new ComplaintLifecycleException("Cannot assign archived complaint: " + complaint.getId());
        }
        if (ComplaintStatus.CLOSED == complaint.getStatus()) {
            throw new ComplaintLifecycleException("Cannot assign closed complaint: " + complaint.getId());
        }
    }

    private void validateOfficerSupplied(UUID officerUserId) {
        if (officerUserId == null) {
            throw new ComplaintAssignmentException("Officer is required for complaint assignment");
        }
    }

    private void validateCurrentAssignmentUniqueness(UUID complaintId) {
        if (complaintAssignmentRepository.findByComplaintIdAndIsCurrentTrueAndDeletedFalse(complaintId).isPresent()) {
            throw new ComplaintAssignmentException(
                    "Current assignment already exists for complaint: " + complaintId);
        }
    }
}
