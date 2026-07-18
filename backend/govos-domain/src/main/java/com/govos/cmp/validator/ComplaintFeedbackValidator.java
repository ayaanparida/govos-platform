package com.govos.cmp.validator;

import com.govos.cmp.dto.ComplaintFeedbackCreateRequest;
import com.govos.cmp.dto.ComplaintFeedbackUpdateRequest;
import com.govos.cmp.entity.Complaint;
import com.govos.cmp.enums.ComplaintFeedbackRating;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.cmp.exception.ComplaintFeedbackException;
import com.govos.cmp.exception.ComplaintLifecycleException;
import com.govos.cmp.exception.ComplaintNotFoundException;
import com.govos.cmp.repository.ComplaintFeedbackRepository;
import com.govos.cmp.repository.ComplaintRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ComplaintFeedbackValidator {

    private final ComplaintRepository complaintRepository;
    private final ComplaintFeedbackRepository complaintFeedbackRepository;

    public ComplaintFeedbackValidator(
            ComplaintRepository complaintRepository,
            ComplaintFeedbackRepository complaintFeedbackRepository) {
        this.complaintRepository = complaintRepository;
        this.complaintFeedbackRepository = complaintFeedbackRepository;
    }

    public void validateCreate(ComplaintFeedbackCreateRequest request) {
        Complaint complaint = complaintRepository.findByIdAndDeletedFalse(request.complaintId())
                .orElseThrow(() -> new ComplaintNotFoundException(request.complaintId()));
        validateFeedbackAllowed(complaint);
        validateRatingMandatory(request.rating());
        validateSingleActiveFeedback(request.complaintId());
    }

    public void validateUpdate(UUID id, UUID complaintId, ComplaintFeedbackUpdateRequest request) {
        complaintRepository.findByIdAndDeletedFalse(complaintId)
                .orElseThrow(() -> new ComplaintNotFoundException(complaintId));
        if (request.rating() == null) {
            throw new ComplaintFeedbackException("Feedback rating is mandatory");
        }
        complaintFeedbackRepository.findByComplaintIdAndDeletedFalse(complaintId)
                .filter(feedback -> !feedback.getId().equals(id))
                .ifPresent(feedback -> {
                    throw new ComplaintFeedbackException(
                            "Active feedback already exists for complaint: " + complaintId);
                });
    }

    private void validateFeedbackAllowed(Complaint complaint) {
        if (ComplaintStatus.CLOSED != complaint.getStatus()) {
            throw new ComplaintLifecycleException(
                    "Feedback is allowed only after complaint is closed: " + complaint.getId());
        }
    }

    private void validateRatingMandatory(ComplaintFeedbackRating rating) {
        if (rating == null) {
            throw new ComplaintFeedbackException("Feedback rating is mandatory");
        }
    }

    private void validateSingleActiveFeedback(UUID complaintId) {
        if (complaintFeedbackRepository.existsByComplaintIdAndDeletedFalse(complaintId)) {
            throw new ComplaintFeedbackException(
                    "Active feedback already exists for complaint: " + complaintId);
        }
    }
}
