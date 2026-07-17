package com.govos.api.cmp.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.audit.dto.CreateAuditEventRequest;
import com.govos.audit.entity.AuditEventStatus;
import com.govos.audit.exception.AuditException;
import com.govos.audit.exception.AuditSessionNotFoundException;
import com.govos.audit.service.AuditEventService;
import com.govos.audit.service.AuditSessionService;
import com.govos.cmp.dto.ComplaintDto;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class ComplaintAuditIntegrationImpl implements ComplaintAuditIntegration {

    private final AuditEventService auditEventService;
    private final AuditSessionService auditSessionService;
    private final ObjectMapper objectMapper;

    public ComplaintAuditIntegrationImpl(
            AuditEventService auditEventService,
            AuditSessionService auditSessionService,
            ObjectMapper objectMapper) {
        this.auditEventService = auditEventService;
        this.auditSessionService = auditSessionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onCreated(ComplaintDto complaint, UUID performedByUserId) {
        record(ComplaintAuditAction.CMP_COMPLAINT_CREATED, complaint, performedByUserId);
    }

    @Override
    public void onUpdated(ComplaintDto complaint, UUID performedByUserId) {
        record(ComplaintAuditAction.CMP_COMPLAINT_UPDATED, complaint, performedByUserId);
    }

    @Override
    public void onSubmitted(ComplaintDto complaint, UUID performedByUserId) {
        record(ComplaintAuditAction.CMP_COMPLAINT_SUBMITTED, complaint, performedByUserId);
    }

    @Override
    public void onAccepted(ComplaintDto complaint, UUID performedByUserId) {
        record(ComplaintAuditAction.CMP_COMPLAINT_ACCEPTED, complaint, performedByUserId);
    }

    @Override
    public void onRejected(ComplaintDto complaint, UUID performedByUserId) {
        record(ComplaintAuditAction.CMP_COMPLAINT_REJECTED, complaint, performedByUserId);
    }

    @Override
    public void onAssigned(ComplaintDto complaint, UUID performedByUserId) {
        record(ComplaintAuditAction.CMP_COMPLAINT_ASSIGNED, complaint, performedByUserId);
    }

    @Override
    public void onReassigned(ComplaintDto complaint, UUID performedByUserId) {
        record(ComplaintAuditAction.CMP_COMPLAINT_REASSIGNED, complaint, performedByUserId);
    }

    @Override
    public void onInProgress(ComplaintDto complaint, UUID performedByUserId) {
        record(ComplaintAuditAction.CMP_COMPLAINT_IN_PROGRESS, complaint, performedByUserId);
    }

    @Override
    public void onResolved(ComplaintDto complaint, UUID performedByUserId) {
        record(ComplaintAuditAction.CMP_COMPLAINT_RESOLVED, complaint, performedByUserId);
    }

    @Override
    public void onClosed(ComplaintDto complaint, UUID performedByUserId) {
        record(ComplaintAuditAction.CMP_COMPLAINT_CLOSED, complaint, performedByUserId);
    }

    @Override
    public void onArchived(ComplaintDto complaint, UUID performedByUserId) {
        record(ComplaintAuditAction.CMP_COMPLAINT_ARCHIVED, complaint, performedByUserId);
    }

    @Override
    public void onReopened(ComplaintDto complaint, UUID performedByUserId) {
        record(ComplaintAuditAction.CMP_COMPLAINT_REOPENED, complaint, performedByUserId);
    }

    @Override
    public void onSoftDeleted(ComplaintDto complaint, UUID performedByUserId) {
        record(ComplaintAuditAction.CMP_COMPLAINT_SOFT_DELETED, complaint, performedByUserId);
    }

    @Override
    public void onRestored(ComplaintDto complaint, UUID performedByUserId) {
        record(ComplaintAuditAction.CMP_COMPLAINT_RESTORED, complaint, performedByUserId);
    }

    @Override
    public void onCommentAdded(ComplaintDto complaint, UUID performedByUserId) {
        record(ComplaintAuditAction.CMP_COMMENT_ADDED, complaint, performedByUserId);
    }

    @Override
    public void onAttachmentAdded(ComplaintDto complaint, UUID performedByUserId) {
        record(ComplaintAuditAction.CMP_ATTACHMENT_ADDED, complaint, performedByUserId);
    }

    @Override
    public void onFeedbackSubmitted(ComplaintDto complaint, UUID performedByUserId) {
        record(ComplaintAuditAction.CMP_FEEDBACK_SUBMITTED, complaint, performedByUserId);
    }

    @Override
    public void onFeedbackUpdated(ComplaintDto complaint, UUID performedByUserId) {
        record(ComplaintAuditAction.CMP_FEEDBACK_UPDATED, complaint, performedByUserId);
    }

    @Override
    public void onEscalated(ComplaintDto complaint, UUID performedByUserId) {
        record(ComplaintAuditAction.CMP_ESCALATED, complaint, performedByUserId);
    }

    @Override
    public void onDuplicateCreated(ComplaintDto complaint, UUID performedByUserId) {
        record(ComplaintAuditAction.CMP_DUPLICATE_CREATED, complaint, performedByUserId);
    }

    @Override
    public void onMergeCreated(ComplaintDto complaint, UUID performedByUserId) {
        record(ComplaintAuditAction.CMP_MERGE_CREATED, complaint, performedByUserId);
    }

    private void record(ComplaintAuditAction action, ComplaintDto complaint, UUID performedByUserId) {
        runVoid(() -> {
            String requestId = resolveRequestId();
            Instant eventTime = Instant.now();
            ComplaintAuditPayload payload = ComplaintAuditPayload.from(
                    action, complaint, performedByUserId, requestId, eventTime);

            auditEventService.create(
                    new CreateAuditEventRequest(
                            generateAuditCode(action),
                            action.eventCode(),
                            action.eventType(),
                            ComplaintAuditAction.ENTITY_TYPE,
                            complaint.id(),
                            action.auditAction(),
                            payload.toJson(objectMapper),
                            null,
                            resolveAuditSessionId(requestId),
                            null,
                            null,
                            eventTime,
                            AuditEventStatus.RECORDED,
                            true));
        });
    }

    private UUID resolveAuditSessionId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            return null;
        }
        try {
            return auditSessionService.getBySessionId(requestId).id();
        } catch (AuditSessionNotFoundException ex) {
            return null;
        }
    }

    private String resolveRequestId() {
        String requestId = MDC.get("requestId");
        return requestId != null && !requestId.isBlank() ? requestId : null;
    }

    private String generateAuditCode(ComplaintAuditAction action) {
        return action.eventCode() + "-" + UUID.randomUUID();
    }

    private void runVoid(Runnable runnable) {
        try {
            runnable.run();
        } catch (ComplaintAuditIntegrationException ex) {
            throw ex;
        } catch (AuditException ex) {
            throw new ComplaintAuditIntegrationException(
                    "Audit integration failed: " + ex.getMessage(), ex);
        }
    }
}
