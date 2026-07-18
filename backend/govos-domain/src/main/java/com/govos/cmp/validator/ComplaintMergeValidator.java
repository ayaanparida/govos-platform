package com.govos.cmp.validator;

import com.govos.cmp.dto.ComplaintMergeCreateRequest;
import com.govos.cmp.entity.Complaint;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.cmp.exception.ComplaintLifecycleException;
import com.govos.cmp.exception.ComplaintMergeException;
import com.govos.cmp.exception.ComplaintNotFoundException;
import com.govos.cmp.repository.ComplaintRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ComplaintMergeValidator {

    private final ComplaintRepository complaintRepository;

    public ComplaintMergeValidator(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    public void validateCreate(ComplaintMergeCreateRequest request) {
        Complaint surviving = requireComplaintExists(request.survivingComplaintId());
        Complaint merged = requireComplaintExists(request.mergedComplaintId());
        validateNotSelfMerge(request.survivingComplaintId(), request.mergedComplaintId());
        validateComplaintMergeable(surviving);
        validateComplaintMergeable(merged);
    }

    private Complaint requireComplaintExists(UUID complaintId) {
        return complaintRepository.findByIdAndDeletedFalse(complaintId)
                .orElseThrow(() -> new ComplaintNotFoundException(complaintId));
    }

    private void validateNotSelfMerge(UUID survivingComplaintId, UUID mergedComplaintId) {
        if (survivingComplaintId.equals(mergedComplaintId)) {
            throw new ComplaintMergeException("Complaint cannot be merged with itself");
        }
    }

    private void validateComplaintMergeable(Complaint complaint) {
        if (ComplaintStatus.ARCHIVED == complaint.getStatus()) {
            throw new ComplaintLifecycleException("Cannot merge archived complaint: " + complaint.getId());
        }
        if (ComplaintStatus.CLOSED == complaint.getStatus()) {
            throw new ComplaintLifecycleException("Cannot merge closed complaint: " + complaint.getId());
        }
    }
}
