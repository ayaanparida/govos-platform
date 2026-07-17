package com.govos.cmp.validator;

import com.govos.cmp.dto.ComplaintCommentCreateRequest;
import com.govos.cmp.entity.Complaint;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.cmp.enums.ComplaintVisibility;
import com.govos.cmp.exception.ComplaintCommentException;
import com.govos.cmp.exception.ComplaintLifecycleException;
import com.govos.cmp.exception.ComplaintNotFoundException;
import com.govos.cmp.repository.ComplaintRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ComplaintCommentValidator {

    private final ComplaintRepository complaintRepository;

    public ComplaintCommentValidator(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    public void validateCreate(ComplaintCommentCreateRequest request) {
        Complaint complaint = complaintRepository.findByIdAndDeletedFalse(request.complaintId())
                .orElseThrow(() -> new ComplaintNotFoundException(request.complaintId()));
        validateComplaintCommentable(complaint);
        validateCommentText(request.commentText());
        validateVisibility(request.visibility());
    }

    private void validateComplaintCommentable(Complaint complaint) {
        if (ComplaintStatus.ARCHIVED == complaint.getStatus()) {
            throw new ComplaintLifecycleException("Cannot comment on archived complaint: " + complaint.getId());
        }
    }

    private void validateCommentText(String commentText) {
        if (!StringUtils.hasText(commentText)) {
            throw new ComplaintCommentException("Comment text is required");
        }
    }

    private void validateVisibility(ComplaintVisibility visibility) {
        if (visibility == null) {
            throw new ComplaintCommentException("Comment visibility is required");
        }
    }
}
