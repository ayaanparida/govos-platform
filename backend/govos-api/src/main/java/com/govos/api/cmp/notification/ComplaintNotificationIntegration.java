package com.govos.api.cmp.notification;

import com.govos.cmp.dto.ComplaintAssignmentCreateRequest;
import com.govos.cmp.dto.ComplaintCommentDto;
import com.govos.cmp.dto.ComplaintDto;
import com.govos.cmp.dto.ComplaintEscalationDto;

public interface ComplaintNotificationIntegration {

    void onSubmitted(ComplaintDto complaint);

    void onAccepted(ComplaintDto complaint);

    void onRejected(ComplaintDto complaint, String rejectionReasonKey);

    void onAssigned(ComplaintDto complaint, ComplaintAssignmentCreateRequest assignmentRequest);

    void onReassignmentRequested(ComplaintDto complaint);

    void onInProgress(ComplaintDto complaint);

    void onResolved(ComplaintDto complaint);

    void onClosed(ComplaintDto complaint);

    void onReopened(ComplaintDto complaint);

    void onCommentAdded(ComplaintDto complaint, ComplaintCommentDto comment);

    void onEscalated(ComplaintDto complaint, ComplaintEscalationDto escalation);
}
