package com.govos.api.cmp.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.cmp.dto.ComplaintAssignmentCreateRequest;
import com.govos.cmp.dto.ComplaintCommentDto;
import com.govos.cmp.dto.ComplaintDto;
import com.govos.cmp.dto.ComplaintEscalationDto;
import com.govos.cmp.enums.ComplaintVisibility;
import com.govos.ntf.dto.CreateNotificationRequest;
import com.govos.ntf.dto.NotificationChannelDto;
import com.govos.ntf.entity.NotificationPriority;
import com.govos.ntf.entity.NotificationStatus;
import com.govos.ntf.exception.NtfException;
import com.govos.ntf.service.NotificationChannelService;
import com.govos.ntf.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class ComplaintNotificationIntegrationImpl implements ComplaintNotificationIntegration {

    private final NotificationService notificationService;
    private final NotificationChannelService notificationChannelService;
    private final ObjectMapper objectMapper;

    public ComplaintNotificationIntegrationImpl(
            NotificationService notificationService,
            NotificationChannelService notificationChannelService,
            ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.notificationChannelService = notificationChannelService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onSubmitted(ComplaintDto complaint) {
        publishToUser(ComplaintNotificationEvents.COMPLAINT_SUBMITTED, complaint.citizenUserId(), complaint, null);
    }

    @Override
    public void onAccepted(ComplaintDto complaint) {
        publishToUser(ComplaintNotificationEvents.COMPLAINT_ACCEPTED, complaint.citizenUserId(), complaint, null);
    }

    @Override
    public void onRejected(ComplaintDto complaint, String rejectionReasonKey) {
        publishToUser(
                ComplaintNotificationEvents.COMPLAINT_REJECTED,
                complaint.citizenUserId(),
                complaint,
                rejectionReasonKey);
    }

    @Override
    public void onAssigned(ComplaintDto complaint, ComplaintAssignmentCreateRequest assignmentRequest) {
        publishToUser(
                ComplaintNotificationEvents.COMPLAINT_ASSIGNED,
                assignmentRequest.officerUserId(),
                complaint,
                null);
    }

    @Override
    public void onReassignmentRequested(ComplaintDto complaint) {
        publishToRecipient(
                ComplaintNotificationEvents.COMPLAINT_REASSIGNED,
                resolveDepartmentSupervisorRecipient(complaint),
                complaint,
                null);
    }

    @Override
    public void onInProgress(ComplaintDto complaint) {
        publishToUser(ComplaintNotificationEvents.COMPLAINT_IN_PROGRESS, complaint.citizenUserId(), complaint, null);
    }

    @Override
    public void onResolved(ComplaintDto complaint) {
        publishToUser(ComplaintNotificationEvents.COMPLAINT_RESOLVED, complaint.citizenUserId(), complaint, null);
    }

    @Override
    public void onClosed(ComplaintDto complaint) {
        publishToUser(ComplaintNotificationEvents.COMPLAINT_CLOSED, complaint.citizenUserId(), complaint, null);
        publishToUser(ComplaintNotificationEvents.COMPLAINT_CLOSED, complaint.assignedOfficerId(), complaint, null);
    }

    @Override
    public void onReopened(ComplaintDto complaint) {
        publishToUser(ComplaintNotificationEvents.COMPLAINT_REOPENED, complaint.citizenUserId(), complaint, null);
        publishToUser(ComplaintNotificationEvents.COMPLAINT_REOPENED, complaint.assignedOfficerId(), complaint, null);
    }

    @Override
    public void onCommentAdded(ComplaintDto complaint, ComplaintCommentDto comment) {
        if (comment.visibility() != ComplaintVisibility.CITIZEN_VISIBLE) {
            return;
        }
        publishToUser(ComplaintNotificationEvents.COMMENT_ADDED, complaint.citizenUserId(), complaint, null);
    }

    @Override
    public void onEscalated(ComplaintDto complaint, ComplaintEscalationDto escalation) {
        publishToUser(
                ComplaintNotificationEvents.ESCALATED,
                escalation.escalatedToUserId(),
                complaint,
                null);
    }

    private void publishToUser(
            String eventCode,
            UUID recipientUserId,
            ComplaintDto complaint,
            String rejectionReasonKey) {
        if (recipientUserId == null) {
            return;
        }
        publishToRecipient(eventCode, recipientUserId.toString(), complaint, rejectionReasonKey);
    }

    private void publishToRecipient(
            String eventCode,
            String recipient,
            ComplaintDto complaint,
            String rejectionReasonKey) {
        runVoid(() -> {
            Instant eventTime = Instant.now();
            ComplaintNotificationPayload payload = ComplaintNotificationPayload.from(
                    complaint, eventTime, rejectionReasonKey);
            NotificationChannelDto channel = notificationChannelService.getByCode(
                    ComplaintNotificationEvents.CHANNEL_CODE);

            notificationService.create(
                    new CreateNotificationRequest(
                            generateNotificationCode(eventCode),
                            recipient,
                            eventCode,
                            payload.toJson(objectMapper),
                            channel.id(),
                            NotificationStatus.PENDING,
                            NotificationPriority.NORMAL,
                            null,
                            null,
                            true));
        });
    }

    private String resolveDepartmentSupervisorRecipient(ComplaintDto complaint) {
        if (complaint.departmentId() == null) {
            throw new ComplaintNotificationIntegrationException(
                    "Cannot notify department supervisor without departmentId for complaint: " + complaint.id());
        }
        return ComplaintNotificationEvents.RECIPIENT_DEPT_SUPERVISOR_PREFIX + complaint.departmentId();
    }

    private String generateNotificationCode(String eventCode) {
        return eventCode + "-" + UUID.randomUUID();
    }

    private void runVoid(Runnable action) {
        try {
            action.run();
        } catch (ComplaintNotificationIntegrationException ex) {
            throw ex;
        } catch (NtfException ex) {
            throw new ComplaintNotificationIntegrationException(
                    "Notification integration failed: " + ex.getMessage(), ex);
        }
    }
}
