package com.govos.api.cmp.notification;

public final class ComplaintNotificationEvents {

    public static final String CHANNEL_CODE = "CMP_IN_APP";

    public static final String COMPLAINT_SUBMITTED = "CMP_COMPLAINT_SUBMITTED";
    public static final String COMPLAINT_ACCEPTED = "CMP_COMPLAINT_ACCEPTED";
    public static final String COMPLAINT_REJECTED = "CMP_COMPLAINT_REJECTED";
    public static final String COMPLAINT_ASSIGNED = "CMP_COMPLAINT_ASSIGNED";
    public static final String COMPLAINT_REASSIGNED = "CMP_COMPLAINT_REASSIGNED";
    public static final String COMPLAINT_IN_PROGRESS = "CMP_COMPLAINT_IN_PROGRESS";
    public static final String COMPLAINT_RESOLVED = "CMP_COMPLAINT_RESOLVED";
    public static final String COMPLAINT_CLOSED = "CMP_COMPLAINT_CLOSED";
    public static final String COMPLAINT_REOPENED = "CMP_COMPLAINT_REOPENED";
    public static final String COMMENT_ADDED = "CMP_COMMENT_ADDED";
    public static final String ESCALATED = "CMP_ESCALATED";

    public static final String RECIPIENT_DEPT_SUPERVISOR_PREFIX = "DEPT_SUPERVISOR:";

    private ComplaintNotificationEvents() {
    }
}
