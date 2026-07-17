package com.govos.api.cmp.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.govos.cmp.dto.ComplaintAssignmentCreateRequest;
import com.govos.cmp.dto.ComplaintCommentDto;
import com.govos.cmp.dto.ComplaintDto;
import com.govos.cmp.dto.ComplaintEscalationDto;
import com.govos.cmp.enums.ComplaintAssignmentType;
import com.govos.cmp.enums.ComplaintCommentType;
import com.govos.cmp.enums.ComplaintEscalationLevel;
import com.govos.cmp.enums.ComplaintEscalationReason;
import com.govos.cmp.enums.ComplaintPriority;
import com.govos.cmp.enums.ComplaintSource;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.cmp.enums.ComplaintVisibility;
import com.govos.ntf.dto.CreateNotificationRequest;
import com.govos.ntf.dto.NotificationChannelDto;
import com.govos.ntf.entity.ChannelProvider;
import com.govos.ntf.entity.NotificationStatus;
import com.govos.ntf.exception.NotificationChannelNotFoundException;
import com.govos.ntf.service.NotificationChannelService;
import com.govos.ntf.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplaintNotificationIntegrationTest {

    @Mock private NotificationService notificationService;
    @Mock private NotificationChannelService notificationChannelService;

    private ComplaintNotificationIntegration integration;

    private static final UUID COMPLAINT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID CITIZEN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OFFICER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID DEPARTMENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ESCALATION_OFFICER_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID CHANNEL_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
    private static final UUID WORKFLOW_INSTANCE_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");

    @BeforeEach
    void setUp() {
        integration = new ComplaintNotificationIntegrationImpl(
                notificationService,
                notificationChannelService,
                new ObjectMapper().registerModule(new JavaTimeModule()));
    }

    @Test
    void shouldPublishSubmittedNotificationToCitizen() {
        stubChannel();
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.SUBMITTED, null, CITIZEN_ID, null);

        integration.onSubmitted(complaint);

        verifyNotificationCreated(ComplaintNotificationEvents.COMPLAINT_SUBMITTED, CITIZEN_ID.toString());
    }

    @Test
    void shouldPublishAcceptedNotificationToCitizen() {
        stubChannel();
        integration.onAccepted(sampleComplaint(ComplaintStatus.ACCEPTED, null, CITIZEN_ID, null));
        verifyNotificationCreated(ComplaintNotificationEvents.COMPLAINT_ACCEPTED, CITIZEN_ID.toString());
    }

    @Test
    void shouldPublishRejectedNotificationWithReason() {
        stubChannel();
        integration.onRejected(sampleComplaint(ComplaintStatus.REJECTED, null, CITIZEN_ID, null), "INVALID");
        ArgumentCaptor<CreateNotificationRequest> captor = ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(notificationService).create(captor.capture());
        assertThat(captor.getValue().subject()).isEqualTo(ComplaintNotificationEvents.COMPLAINT_REJECTED);
        assertThat(captor.getValue().body()).contains("INVALID");
    }

    @Test
    void shouldPublishAssignedNotificationToOfficer() {
        stubChannel();
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.ASSIGNED, null, CITIZEN_ID, OFFICER_ID);
        ComplaintAssignmentCreateRequest assignmentRequest = new ComplaintAssignmentCreateRequest(
                COMPLAINT_ID, ComplaintAssignmentType.INITIAL, DEPARTMENT_ID, null, OFFICER_ID, CITIZEN_ID, null, true);

        integration.onAssigned(complaint, assignmentRequest);

        verifyNotificationCreated(ComplaintNotificationEvents.COMPLAINT_ASSIGNED, OFFICER_ID.toString());
    }

    @Test
    void shouldPublishReassignmentNotificationToDepartmentSupervisor() {
        stubChannel();
        ComplaintDto complaint = withDepartment(
                sampleComplaint(ComplaintStatus.PENDING_REASSIGNMENT, WORKFLOW_INSTANCE_ID, CITIZEN_ID, OFFICER_ID),
                DEPARTMENT_ID);

        integration.onReassignmentRequested(complaint);

        verifyNotificationCreated(
                ComplaintNotificationEvents.COMPLAINT_REASSIGNED,
                ComplaintNotificationEvents.RECIPIENT_DEPT_SUPERVISOR_PREFIX + DEPARTMENT_ID);
    }

    @Test
    void shouldPublishInProgressNotificationToCitizen() {
        stubChannel();
        integration.onInProgress(sampleComplaint(ComplaintStatus.IN_PROGRESS, WORKFLOW_INSTANCE_ID, CITIZEN_ID, OFFICER_ID));
        verifyNotificationCreated(ComplaintNotificationEvents.COMPLAINT_IN_PROGRESS, CITIZEN_ID.toString());
    }

    @Test
    void shouldPublishResolvedNotificationToCitizen() {
        stubChannel();
        integration.onResolved(sampleComplaint(ComplaintStatus.RESOLVED, WORKFLOW_INSTANCE_ID, CITIZEN_ID, OFFICER_ID));
        verifyNotificationCreated(ComplaintNotificationEvents.COMPLAINT_RESOLVED, CITIZEN_ID.toString());
    }

    @Test
    void shouldPublishClosedNotificationToCitizenAndOfficer() {
        stubChannel();
        integration.onClosed(sampleComplaint(ComplaintStatus.CLOSED, WORKFLOW_INSTANCE_ID, CITIZEN_ID, OFFICER_ID));
        verify(notificationService, times(2)).create(any(CreateNotificationRequest.class));
    }

    @Test
    void shouldPublishReopenedNotificationToCitizenAndOfficer() {
        stubChannel();
        integration.onReopened(sampleComplaint(ComplaintStatus.REOPENED, WORKFLOW_INSTANCE_ID, CITIZEN_ID, OFFICER_ID));
        verify(notificationService, times(2)).create(any(CreateNotificationRequest.class));
    }

    @Test
    void shouldPublishCommentAddedNotificationWhenCitizenVisible() {
        stubChannel();
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.IN_PROGRESS, WORKFLOW_INSTANCE_ID, CITIZEN_ID, OFFICER_ID);
        ComplaintCommentDto comment = sampleComment(ComplaintVisibility.CITIZEN_VISIBLE);

        integration.onCommentAdded(complaint, comment);

        verifyNotificationCreated(ComplaintNotificationEvents.COMMENT_ADDED, CITIZEN_ID.toString());
    }

    @Test
    void shouldNotPublishCommentAddedNotificationWhenInternal() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.IN_PROGRESS, WORKFLOW_INSTANCE_ID, CITIZEN_ID, OFFICER_ID);
        ComplaintCommentDto comment = sampleComment(ComplaintVisibility.INTERNAL);

        integration.onCommentAdded(complaint, comment);

        verify(notificationService, never()).create(any());
        verify(notificationChannelService, never()).getByCode(any());
    }

    @Test
    void shouldPublishEscalationNotificationToEscalationOfficer() {
        stubChannel();
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.IN_PROGRESS, WORKFLOW_INSTANCE_ID, CITIZEN_ID, OFFICER_ID);
        ComplaintEscalationDto escalation = new ComplaintEscalationDto(
                UUID.randomUUID(), "ESC-1", COMPLAINT_ID, ComplaintEscalationLevel.L2,
                ComplaintEscalationReason.SLA_BREACH, OFFICER_ID, ESCALATION_OFFICER_ID, DEPARTMENT_ID,
                null, Instant.now(), true, 0L, null, null, null, null);

        integration.onEscalated(complaint, escalation);

        verifyNotificationCreated(ComplaintNotificationEvents.ESCALATED, ESCALATION_OFFICER_ID.toString());
    }

    @Test
    void shouldWrapNotificationFailureAsIntegrationException() {
        when(notificationChannelService.getByCode(ComplaintNotificationEvents.CHANNEL_CODE))
                .thenThrow(new NotificationChannelNotFoundException(ComplaintNotificationEvents.CHANNEL_CODE));

        assertThatThrownBy(() -> integration.onSubmitted(
                sampleComplaint(ComplaintStatus.SUBMITTED, null, CITIZEN_ID, null)))
                .isInstanceOf(ComplaintNotificationIntegrationException.class)
                .hasMessageContaining("Notification integration failed");
    }

    private void stubChannel() {
        when(notificationChannelService.getByCode(ComplaintNotificationEvents.CHANNEL_CODE))
                .thenReturn(new NotificationChannelDto(
                        CHANNEL_ID, ComplaintNotificationEvents.CHANNEL_CODE, "CMP In-App",
                        ChannelProvider.IN_APP, true, 0L, null, null, null, null));
        when(notificationService.create(any(CreateNotificationRequest.class))).thenReturn(null);
    }

    private void verifyNotificationCreated(String eventCode, String recipient) {
        ArgumentCaptor<CreateNotificationRequest> captor = ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(notificationService).create(captor.capture());
        CreateNotificationRequest request = captor.getValue();
        assertThat(request.subject()).isEqualTo(eventCode);
        assertThat(request.recipient()).isEqualTo(recipient);
        assertThat(request.channelId()).isEqualTo(CHANNEL_ID);
        assertThat(request.status()).isEqualTo(NotificationStatus.PENDING);
        assertThat(request.body()).contains(COMPLAINT_ID.toString());
        assertThat(request.body()).contains("CMP-2026-0001");
    }

    private static ComplaintCommentDto sampleComment(ComplaintVisibility visibility) {
        return new ComplaintCommentDto(
                UUID.randomUUID(), "CMT-1", COMPLAINT_ID, OFFICER_ID, "Update", visibility,
                ComplaintCommentType.REMARK, true, 0L, null, null, null, null);
    }

    private static ComplaintDto sampleComplaint(
            ComplaintStatus status,
            UUID workflowInstanceId,
            UUID citizenUserId,
            UUID assignedOfficerId) {
        return new ComplaintDto(
                COMPLAINT_ID, "CMP-2026-0001", "Water leak", "Description",
                status, ComplaintPriority.MEDIUM, ComplaintSource.CITIZEN_PORTAL,
                "WEB", "WATER_SUPPLY", "PIPE_LEAK", "SERVICE_REQUEST",
                citizenUserId, citizenUserId, UUID.randomUUID(),
                null, null, assignedOfficerId, null, null, workflowInstanceId,
                null, null, null, null, null, false,
                null, null, "KA", "BLR", null, null, null,
                new BigDecimal("12.9716000"), new BigDecimal("77.5946000"),
                "Main Street", null, "560001", null,
                true, 0L, null, null, null, null);
    }

    private ComplaintDto withDepartment(ComplaintDto complaint, UUID departmentId) {
        return new ComplaintDto(
                complaint.id(), complaint.code(), complaint.title(), complaint.description(),
                complaint.status(), complaint.priority(), complaint.source(),
                complaint.channel(), complaint.categoryKey(), complaint.subCategoryKey(), complaint.complaintTypeKey(),
                complaint.citizenUserId(), complaint.submittedByUserId(), complaint.organizationId(),
                departmentId, complaint.officeId(), complaint.assignedOfficerId(),
                complaint.resolvedByUserId(), complaint.closedByUserId(), complaint.workflowInstanceId(),
                complaint.submittedAt(), complaint.closedAt(), complaint.archivedAt(),
                complaint.rejectionReasonKey(), complaint.closureReasonKey(), complaint.isDuplicate(),
                complaint.primaryComplaintId(), complaint.mergedIntoComplaintId(),
                complaint.stateKey(), complaint.districtKey(), complaint.ulbKey(), complaint.wardKey(),
                complaint.villageKey(), complaint.latitude(), complaint.longitude(),
                complaint.address(), complaint.landmark(), complaint.pincode(), complaint.geoJson(),
                complaint.active(), complaint.version(), complaint.createdBy(), complaint.createdDate(),
                complaint.updatedBy(), complaint.updatedDate());
    }
}
