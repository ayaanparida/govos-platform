package com.govos.cmp.validator;

import com.govos.cmp.dto.ComplaintDuplicateCreateRequest;
import com.govos.cmp.exception.ComplaintDuplicateException;
import com.govos.cmp.exception.ComplaintNotFoundException;
import com.govos.cmp.repository.ComplaintDuplicateRepository;
import com.govos.cmp.repository.ComplaintRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ComplaintDuplicateValidator {

    private final ComplaintRepository complaintRepository;
    private final ComplaintDuplicateRepository complaintDuplicateRepository;

    public ComplaintDuplicateValidator(
            ComplaintRepository complaintRepository,
            ComplaintDuplicateRepository complaintDuplicateRepository) {
        this.complaintRepository = complaintRepository;
        this.complaintDuplicateRepository = complaintDuplicateRepository;
    }

    public void validateCreate(ComplaintDuplicateCreateRequest request) {
        requireComplaintExists(request.primaryComplaintId());
        requireComplaintExists(request.duplicateComplaintId());
        validateNotSelfDuplicate(request.primaryComplaintId(), request.duplicateComplaintId());
        validateDuplicatePairUniqueness(request.primaryComplaintId(), request.duplicateComplaintId());
    }

    private void requireComplaintExists(UUID complaintId) {
        if (!complaintRepository.findByIdAndDeletedFalse(complaintId).isPresent()) {
            throw new ComplaintNotFoundException(complaintId);
        }
    }

    private void validateNotSelfDuplicate(UUID primaryComplaintId, UUID duplicateComplaintId) {
        if (primaryComplaintId.equals(duplicateComplaintId)) {
            throw new ComplaintDuplicateException("Complaint cannot be marked as duplicate of itself");
        }
    }

    private void validateDuplicatePairUniqueness(UUID primaryComplaintId, UUID duplicateComplaintId) {
        boolean pairExists = complaintDuplicateRepository.findAllByPrimaryComplaintIdAndDeletedFalse(primaryComplaintId)
                .stream()
                .anyMatch(link -> duplicateComplaintId.equals(link.getDuplicateComplaintId()));
        if (pairExists) {
            throw new ComplaintDuplicateException(
                    "Duplicate link already exists for primary=" + primaryComplaintId
                            + ", duplicate=" + duplicateComplaintId);
        }
    }
}
