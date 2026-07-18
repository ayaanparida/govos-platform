package com.govos.api.cmp.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.govos.audit.dto.AuditSessionDto;
import com.govos.audit.dto.CreateAuditEventRequest;
import com.govos.audit.entity.AuditAction;
import com.govos.audit.entity.AuditEventStatus;
import com.govos.audit.entity.AuditEventType;
import com.govos.audit.exception.AuditEventNotFoundException;
import com.govos.audit.exception.AuditSessionNotFoundException;
import com.govos.audit.service.AuditEventService;
import com.govos.audit.service.AuditSessionService;
import com.govos.cmp.dto.ComplaintDto;
import com.govos.cmp.enums.ComplaintPriority;
import com.govos.cmp.enums.ComplaintSource;
import com.govos.cmp.enums.ComplaintStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplaintAuditIntegrationTest {

    @Mock private AuditEventService auditEventService;
    @Mock private AuditSessionService auditSessionService;

    private ComplaintAuditIntegration integration;

    private static final UUID COMPLAINT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID USER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID ORG_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SESSION_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
    private static final UUID WORKFLOW_INSTANCE_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final String REQUEST_ID = "req-12345";

    @BeforeEach
    void setUp() {
        integration = new ComplaintAuditIntegrationImpl(
                auditEventService,
                auditSessionService,
                new ObjectMapper().registerModule(new JavaTimeModule()));
        MDC.put("requestId", REQUEST_ID);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @ParameterizedTest
    @MethodSource("auditActionInvocations")
    void shouldRecordAuditEvent(AuditActionInvocation invocation) {
        stubSession();
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.DRAFT);

        invocation.invoke(integration, complaint, USER_ID);

        verifyAuditEvent(invocation.action(), complaint);
    }

    @Test
    void shouldResolveAuditSessionByRequestId() {
        stubSession();
        integration.onSubmitted(sampleComplaint(ComplaintStatus.SUBMITTED), USER_ID);

        ArgumentCaptor<CreateAuditEventRequest> captor = ArgumentCaptor.forClass(CreateAuditEventRequest.class);
        verify(auditEventService).create(captor.capture());
        assertThat(captor.getValue().sessionId()).isEqualTo(SESSION_ID);
    }

    @Test
    void shouldContinueWhenAuditSessionNotFound() {
        when(auditSessionService.getBySessionId(REQUEST_ID))
                .thenThrow(new AuditSessionNotFoundException(REQUEST_ID));

        integration.onCreated(sampleComplaint(ComplaintStatus.DRAFT), USER_ID);

        ArgumentCaptor<CreateAuditEventRequest> captor = ArgumentCaptor.forClass(CreateAuditEventRequest.class);
        verify(auditEventService).create(captor.capture());
        assertThat(captor.getValue().sessionId()).isNull();
    }

    @Test
    void shouldWrapAuditFailureAsIntegrationException() {
        when(auditSessionService.getBySessionId(REQUEST_ID)).thenReturn(
                new AuditSessionDto(
                        SESSION_ID, "SES-1", REQUEST_ID, Instant.now(), null,
                        null, null, null, true, 0L, null, null, null, null));
        when(auditEventService.create(any(CreateAuditEventRequest.class)))
                .thenThrow(new AuditEventNotFoundException("failure"));

        assertThatThrownBy(() -> integration.onCreated(sampleComplaint(ComplaintStatus.DRAFT), USER_ID))
                .isInstanceOf(ComplaintAuditIntegrationException.class)
                .hasMessageContaining("Audit integration failed");
    }

    private void stubSession() {
        when(auditSessionService.getBySessionId(REQUEST_ID)).thenReturn(
                new AuditSessionDto(
                        SESSION_ID, "SES-1", REQUEST_ID, Instant.now(), null,
                        null, null, null, true, 0L, null, null, null, null));
        when(auditEventService.create(any(CreateAuditEventRequest.class))).thenReturn(null);
    }

    private void verifyAuditEvent(ComplaintAuditAction action, ComplaintDto complaint) {
        ArgumentCaptor<CreateAuditEventRequest> captor = ArgumentCaptor.forClass(CreateAuditEventRequest.class);
        verify(auditEventService).create(captor.capture());
        CreateAuditEventRequest request = captor.getValue();

        assertThat(request.eventCode()).isEqualTo(action.eventCode());
        assertThat(request.eventType()).isEqualTo(action.eventType());
        assertThat(request.entityType()).isEqualTo(ComplaintAuditAction.ENTITY_TYPE);
        assertThat(request.entityId()).isEqualTo(complaint.id());
        assertThat(request.action()).isEqualTo(action.auditAction());
        assertThat(request.status()).isEqualTo(AuditEventStatus.RECORDED);
        assertThat(request.description()).contains(COMPLAINT_ID.toString());
        assertThat(request.description()).contains("CMP-2026-0001");
        assertThat(request.description()).contains(REQUEST_ID);
        assertThat(request.description()).contains(USER_ID.toString());
    }

    private static Stream<AuditActionInvocation> auditActionInvocations() {
        return Stream.of(
                new AuditActionInvocation(ComplaintAuditAction.CMP_COMPLAINT_CREATED,
                        (integration, complaint, userId) -> integration.onCreated(complaint, userId)),
                new AuditActionInvocation(ComplaintAuditAction.CMP_COMPLAINT_UPDATED,
                        (integration, complaint, userId) -> integration.onUpdated(complaint, userId)),
                new AuditActionInvocation(ComplaintAuditAction.CMP_COMPLAINT_SUBMITTED,
                        (integration, complaint, userId) -> integration.onSubmitted(complaint, userId)),
                new AuditActionInvocation(ComplaintAuditAction.CMP_COMPLAINT_ACCEPTED,
                        (integration, complaint, userId) -> integration.onAccepted(complaint, userId)),
                new AuditActionInvocation(ComplaintAuditAction.CMP_COMPLAINT_REJECTED,
                        (integration, complaint, userId) -> integration.onRejected(complaint, userId)),
                new AuditActionInvocation(ComplaintAuditAction.CMP_COMPLAINT_ASSIGNED,
                        (integration, complaint, userId) -> integration.onAssigned(complaint, userId)),
                new AuditActionInvocation(ComplaintAuditAction.CMP_COMPLAINT_REASSIGNED,
                        (integration, complaint, userId) -> integration.onReassigned(complaint, userId)),
                new AuditActionInvocation(ComplaintAuditAction.CMP_COMPLAINT_IN_PROGRESS,
                        (integration, complaint, userId) -> integration.onInProgress(complaint, userId)),
                new AuditActionInvocation(ComplaintAuditAction.CMP_COMPLAINT_RESOLVED,
                        (integration, complaint, userId) -> integration.onResolved(complaint, userId)),
                new AuditActionInvocation(ComplaintAuditAction.CMP_COMPLAINT_CLOSED,
                        (integration, complaint, userId) -> integration.onClosed(complaint, userId)),
                new AuditActionInvocation(ComplaintAuditAction.CMP_COMPLAINT_ARCHIVED,
                        (integration, complaint, userId) -> integration.onArchived(complaint, userId)),
                new AuditActionInvocation(ComplaintAuditAction.CMP_COMPLAINT_REOPENED,
                        (integration, complaint, userId) -> integration.onReopened(complaint, userId)),
                new AuditActionInvocation(ComplaintAuditAction.CMP_COMPLAINT_SOFT_DELETED,
                        (integration, complaint, userId) -> integration.onSoftDeleted(complaint, userId)),
                new AuditActionInvocation(ComplaintAuditAction.CMP_COMPLAINT_RESTORED,
                        (integration, complaint, userId) -> integration.onRestored(complaint, userId)),
                new AuditActionInvocation(ComplaintAuditAction.CMP_COMMENT_ADDED,
                        (integration, complaint, userId) -> integration.onCommentAdded(complaint, userId)),
                new AuditActionInvocation(ComplaintAuditAction.CMP_ATTACHMENT_ADDED,
                        (integration, complaint, userId) -> integration.onAttachmentAdded(complaint, userId)),
                new AuditActionInvocation(ComplaintAuditAction.CMP_FEEDBACK_SUBMITTED,
                        (integration, complaint, userId) -> integration.onFeedbackSubmitted(complaint, userId)),
                new AuditActionInvocation(ComplaintAuditAction.CMP_FEEDBACK_UPDATED,
                        (integration, complaint, userId) -> integration.onFeedbackUpdated(complaint, userId)),
                new AuditActionInvocation(ComplaintAuditAction.CMP_ESCALATED,
                        (integration, complaint, userId) -> integration.onEscalated(complaint, userId)),
                new AuditActionInvocation(ComplaintAuditAction.CMP_DUPLICATE_CREATED,
                        (integration, complaint, userId) -> integration.onDuplicateCreated(complaint, userId)),
                new AuditActionInvocation(ComplaintAuditAction.CMP_MERGE_CREATED,
                        (integration, complaint, userId) -> integration.onMergeCreated(complaint, userId))
        );
    }

    private static ComplaintDto sampleComplaint(ComplaintStatus status) {
        return new ComplaintDto(
                COMPLAINT_ID, "CMP-2026-0001", "Water leak", "Description",
                status, ComplaintPriority.MEDIUM, ComplaintSource.CITIZEN_PORTAL,
                "WEB", "WATER_SUPPLY", "PIPE_LEAK", "SERVICE_REQUEST",
                USER_ID, USER_ID, ORG_ID,
                null, null, null, null, null, WORKFLOW_INSTANCE_ID,
                null, null, null, null, null, false,
                null, null, "KA", "BLR", null, null, null,
                new BigDecimal("12.9716000"), new BigDecimal("77.5946000"),
                "Main Street", null, "560001", null,
                true, 0L, null, null, null, null);
    }

    private record AuditActionInvocation(
            ComplaintAuditAction action,
            AuditInvoker invoker) {

        void invoke(ComplaintAuditIntegration integration, ComplaintDto complaint, UUID userId) {
            invoker.invoke(integration, complaint, userId);
        }
    }

    @FunctionalInterface
    private interface AuditInvoker {
        void invoke(ComplaintAuditIntegration integration, ComplaintDto complaint, UUID userId);
    }
}
