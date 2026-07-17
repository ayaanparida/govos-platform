package com.govos.cmp.validator;

import com.govos.cmp.dto.ComplaintEscalationCreateRequest;
import com.govos.cmp.entity.Complaint;
import com.govos.cmp.enums.ComplaintEscalationLevel;
import com.govos.cmp.enums.ComplaintEscalationReason;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.cmp.exception.ComplaintEscalationException;
import com.govos.cmp.exception.ComplaintLifecycleException;
import com.govos.cmp.exception.ComplaintNotFoundException;
import com.govos.cmp.repository.ComplaintRepository;
import org.springframework.stereotype.Component;

@Component
public class ComplaintEscalationValidator {

    private final ComplaintRepository complaintRepository;

    public ComplaintEscalationValidator(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    public void validateCreate(ComplaintEscalationCreateRequest request) {
        Complaint complaint = complaintRepository.findByIdAndDeletedFalse(request.complaintId())
                .orElseThrow(() -> new ComplaintNotFoundException(request.complaintId()));
        validateComplaintEscalatable(complaint);
        validateEscalationLevel(request.escalationLevel());
        validateEscalationReason(request.escalationReason());
    }

    private void validateComplaintEscalatable(Complaint complaint) {
        if (ComplaintStatus.ARCHIVED == complaint.getStatus()) {
            throw new ComplaintLifecycleException("Cannot escalate archived complaint: " + complaint.getId());
        }
    }

    private void validateEscalationLevel(ComplaintEscalationLevel escalationLevel) {
        if (escalationLevel == null) {
            throw new ComplaintEscalationException("Escalation level is mandatory");
        }
    }

    private void validateEscalationReason(ComplaintEscalationReason escalationReason) {
        if (escalationReason == null) {
            throw new ComplaintEscalationException("Escalation reason is mandatory");
        }
    }
}
