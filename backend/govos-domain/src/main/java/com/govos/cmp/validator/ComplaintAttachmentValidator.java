package com.govos.cmp.validator;

import com.govos.cmp.dto.ComplaintAttachmentCreateRequest;
import com.govos.cmp.entity.Complaint;
import com.govos.cmp.enums.ComplaintAttachmentType;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.cmp.exception.ComplaintAttachmentException;
import com.govos.cmp.exception.ComplaintLifecycleException;
import com.govos.cmp.exception.ComplaintNotFoundException;
import com.govos.cmp.repository.ComplaintRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ComplaintAttachmentValidator {

    private final ComplaintRepository complaintRepository;

    public ComplaintAttachmentValidator(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    public void validateCreate(ComplaintAttachmentCreateRequest request) {
        Complaint complaint = complaintRepository.findByIdAndDeletedFalse(request.complaintId())
                .orElseThrow(() -> new ComplaintNotFoundException(request.complaintId()));
        validateComplaintAttachable(complaint);
        validateDocumentId(request.documentId());
        validateAttachmentType(request.attachmentType());
    }

    private void validateComplaintAttachable(Complaint complaint) {
        if (ComplaintStatus.ARCHIVED == complaint.getStatus()) {
            throw new ComplaintLifecycleException("Cannot attach files to archived complaint: " + complaint.getId());
        }
    }

    private void validateDocumentId(UUID documentId) {
        if (documentId == null) {
            throw new ComplaintAttachmentException("Document id is mandatory for complaint attachment");
        }
    }

    private void validateAttachmentType(ComplaintAttachmentType attachmentType) {
        if (attachmentType == null) {
            throw new ComplaintAttachmentException("Attachment type is mandatory");
        }
    }
}
