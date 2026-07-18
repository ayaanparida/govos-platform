package com.govos.api.cmp.audit;

import com.govos.audit.entity.AuditAction;
import com.govos.audit.entity.AuditEventType;

public enum ComplaintAuditAction {

    CMP_COMPLAINT_CREATED(AuditAction.CREATE, AuditEventType.ENTITY_CREATED),
    CMP_COMPLAINT_UPDATED(AuditAction.UPDATE, AuditEventType.ENTITY_UPDATED),
    CMP_COMPLAINT_SUBMITTED(AuditAction.TRANSITION, AuditEventType.ENTITY_UPDATED),
    CMP_COMPLAINT_ACCEPTED(AuditAction.TRANSITION, AuditEventType.ENTITY_UPDATED),
    CMP_COMPLAINT_REJECTED(AuditAction.TRANSITION, AuditEventType.ENTITY_UPDATED),
    CMP_COMPLAINT_ASSIGNED(AuditAction.ASSIGN, AuditEventType.ENTITY_UPDATED),
    CMP_COMPLAINT_REASSIGNED(AuditAction.TRANSITION, AuditEventType.ENTITY_UPDATED),
    CMP_COMPLAINT_IN_PROGRESS(AuditAction.TRANSITION, AuditEventType.ENTITY_UPDATED),
    CMP_COMPLAINT_RESOLVED(AuditAction.TRANSITION, AuditEventType.ENTITY_UPDATED),
    CMP_COMPLAINT_CLOSED(AuditAction.TRANSITION, AuditEventType.ENTITY_UPDATED),
    CMP_COMPLAINT_ARCHIVED(AuditAction.TRANSITION, AuditEventType.ENTITY_UPDATED),
    CMP_COMPLAINT_REOPENED(AuditAction.TRANSITION, AuditEventType.ENTITY_UPDATED),
    CMP_COMPLAINT_SOFT_DELETED(AuditAction.DELETE, AuditEventType.ENTITY_DELETED),
    CMP_COMPLAINT_RESTORED(AuditAction.UPDATE, AuditEventType.ENTITY_UPDATED),
    CMP_COMMENT_ADDED(AuditAction.CREATE, AuditEventType.ENTITY_CREATED),
    CMP_ATTACHMENT_ADDED(AuditAction.CREATE, AuditEventType.ENTITY_CREATED),
    CMP_FEEDBACK_SUBMITTED(AuditAction.CREATE, AuditEventType.ENTITY_CREATED),
    CMP_FEEDBACK_UPDATED(AuditAction.UPDATE, AuditEventType.ENTITY_UPDATED),
    CMP_ESCALATED(AuditAction.TRANSITION, AuditEventType.ENTITY_UPDATED),
    CMP_DUPLICATE_CREATED(AuditAction.CREATE, AuditEventType.ENTITY_CREATED),
    CMP_MERGE_CREATED(AuditAction.CREATE, AuditEventType.ENTITY_CREATED);

    public static final String ENTITY_TYPE = "COMPLAINT";

    private final AuditAction auditAction;
    private final AuditEventType eventType;

    ComplaintAuditAction(AuditAction auditAction, AuditEventType eventType) {
        this.auditAction = auditAction;
        this.eventType = eventType;
    }

    public AuditAction auditAction() {
        return auditAction;
    }

    public AuditEventType eventType() {
        return eventType;
    }

    public String eventCode() {
        return name();
    }
}
