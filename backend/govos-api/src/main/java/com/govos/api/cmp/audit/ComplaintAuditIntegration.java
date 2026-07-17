package com.govos.api.cmp.audit;

import com.govos.cmp.dto.ComplaintDto;

import java.util.UUID;

public interface ComplaintAuditIntegration {

    void onCreated(ComplaintDto complaint, UUID performedByUserId);

    void onUpdated(ComplaintDto complaint, UUID performedByUserId);

    void onSubmitted(ComplaintDto complaint, UUID performedByUserId);

    void onAccepted(ComplaintDto complaint, UUID performedByUserId);

    void onRejected(ComplaintDto complaint, UUID performedByUserId);

    void onAssigned(ComplaintDto complaint, UUID performedByUserId);

    void onReassigned(ComplaintDto complaint, UUID performedByUserId);

    void onInProgress(ComplaintDto complaint, UUID performedByUserId);

    void onResolved(ComplaintDto complaint, UUID performedByUserId);

    void onClosed(ComplaintDto complaint, UUID performedByUserId);

    void onArchived(ComplaintDto complaint, UUID performedByUserId);

    void onReopened(ComplaintDto complaint, UUID performedByUserId);

    void onSoftDeleted(ComplaintDto complaint, UUID performedByUserId);

    void onRestored(ComplaintDto complaint, UUID performedByUserId);

    void onCommentAdded(ComplaintDto complaint, UUID performedByUserId);

    void onAttachmentAdded(ComplaintDto complaint, UUID performedByUserId);

    void onFeedbackSubmitted(ComplaintDto complaint, UUID performedByUserId);

    void onFeedbackUpdated(ComplaintDto complaint, UUID performedByUserId);

    void onEscalated(ComplaintDto complaint, UUID performedByUserId);

    void onDuplicateCreated(ComplaintDto complaint, UUID performedByUserId);

    void onMergeCreated(ComplaintDto complaint, UUID performedByUserId);
}
