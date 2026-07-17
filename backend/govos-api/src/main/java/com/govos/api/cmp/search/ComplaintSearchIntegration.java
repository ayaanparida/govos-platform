package com.govos.api.cmp.search;

import com.govos.cmp.dto.ComplaintDto;

public interface ComplaintSearchIntegration {

    void onCreated(ComplaintDto complaint);

    void onUpdated(ComplaintDto complaint);

    void onSubmitted(ComplaintDto complaint);

    void onAssigned(ComplaintDto complaint);

    void onInProgress(ComplaintDto complaint);

    void onResolved(ComplaintDto complaint);

    void onClosed(ComplaintDto complaint);

    void onArchived(ComplaintDto complaint);

    void onReopened(ComplaintDto complaint);

    void onSoftDeleted(ComplaintDto complaint);

    void onRestored(ComplaintDto complaint);

    void onCommentAdded(ComplaintDto complaint);

    void onAttachmentAdded(ComplaintDto complaint);

    void onDuplicateCreated(ComplaintDto complaint);

    void onMergeCreated(ComplaintDto complaint);
}
