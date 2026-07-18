package com.govos.api.cmp.application;

import com.govos.api.cmp.audit.ComplaintAuditIntegration;
import com.govos.api.cmp.audit.ComplaintAuditIntegrationException;
import com.govos.api.cmp.notification.ComplaintNotificationIntegration;
import com.govos.api.cmp.search.ComplaintSearchIntegration;
import com.govos.api.cmp.search.ComplaintSearchIntegrationException;
import com.govos.api.cmp.workflow.ComplaintWorkflowIntegration;
import com.govos.cmp.dto.ComplaintAssignmentCreateRequest;
import com.govos.cmp.dto.ComplaintCommentCreateRequest;
import com.govos.cmp.dto.ComplaintCreateRequest;
import com.govos.cmp.dto.ComplaintDuplicateCreateRequest;
import com.govos.cmp.dto.ComplaintDto;
import com.govos.cmp.dto.ComplaintFeedbackDto;
import com.govos.cmp.dto.ComplaintFeedbackUpdateRequest;
import com.govos.cmp.dto.ComplaintMergeCreateRequest;
import com.govos.cmp.dto.ComplaintUpdateRequest;
import com.govos.cmp.enums.ComplaintPriority;
import com.govos.cmp.enums.ComplaintSource;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.cmp.service.ComplaintAssignmentService;
import com.govos.cmp.service.ComplaintAttachmentService;
import com.govos.cmp.service.ComplaintCommentService;
import com.govos.cmp.service.ComplaintDuplicateService;
import com.govos.cmp.service.ComplaintEscalationService;
import com.govos.cmp.service.ComplaintFeedbackService;
import com.govos.cmp.service.ComplaintMergeService;
import com.govos.cmp.service.ComplaintService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplaintApplicationServiceTest {

    @Mock private ComplaintService complaintService;
    @Mock private ComplaintCommentService complaintCommentService;
    @Mock private ComplaintAttachmentService complaintAttachmentService;
    @Mock private ComplaintFeedbackService complaintFeedbackService;
    @Mock private ComplaintAssignmentService complaintAssignmentService;
    @Mock private ComplaintEscalationService complaintEscalationService;
    @Mock private ComplaintDuplicateService complaintDuplicateService;
    @Mock private ComplaintMergeService complaintMergeService;
    @Mock private ComplaintWorkflowIntegration complaintWorkflowIntegration;
    @Mock private ComplaintNotificationIntegration complaintNotificationIntegration;
    @Mock private ComplaintAuditIntegration complaintAuditIntegration;
    @Mock private ComplaintSearchIntegration complaintSearchIntegration;

    private ComplaintApplicationService complaintApplicationService;

    private static final UUID COMPLAINT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID USER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID FEEDBACK_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID WORKFLOW_INSTANCE_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");

    @BeforeEach
    void setUp() {
        complaintApplicationService = new ComplaintApplicationServiceImpl(
                complaintService,
                complaintCommentService,
                complaintAttachmentService,
                complaintFeedbackService,
                complaintAssignmentService,
                complaintEscalationService,
                complaintDuplicateService,
                complaintMergeService,
                complaintWorkflowIntegration,
                complaintNotificationIntegration,
                complaintAuditIntegration,
                complaintSearchIntegration);
    }

    @Test
    void shouldIndexAfterCreate() {
        ComplaintCreateRequest request = sampleCreateRequest();
        ComplaintDto created = sampleComplaintDto();
        when(complaintService.create(request)).thenReturn(created);

        assertThat(complaintApplicationService.create(request)).isEqualTo(created);

        InOrder order = inOrder(complaintService, complaintAuditIntegration, complaintSearchIntegration);
        order.verify(complaintService).create(request);
        order.verify(complaintAuditIntegration).onCreated(created, created.submittedByUserId());
        order.verify(complaintSearchIntegration).onCreated(created);
    }

    @Test
    void shouldOrchestrateSubmitInDomainWorkflowNotificationAuditSearchOrder() {
        ComplaintDto submitted = sampleComplaintDto();
        ComplaintDto linked = sampleComplaintDtoWithWorkflow();
        when(complaintService.submit(COMPLAINT_ID, USER_ID)).thenReturn(submitted);
        when(complaintWorkflowIntegration.startWorkflowOnSubmit(submitted, USER_ID)).thenReturn(WORKFLOW_INSTANCE_ID);
        when(complaintService.linkWorkflowInstance(COMPLAINT_ID, WORKFLOW_INSTANCE_ID)).thenReturn(linked);

        assertThat(complaintApplicationService.submit(COMPLAINT_ID, USER_ID)).isEqualTo(linked);

        InOrder order = inOrder(
                complaintService,
                complaintWorkflowIntegration,
                complaintNotificationIntegration,
                complaintAuditIntegration,
                complaintSearchIntegration);
        order.verify(complaintService).submit(COMPLAINT_ID, USER_ID);
        order.verify(complaintWorkflowIntegration).startWorkflowOnSubmit(submitted, USER_ID);
        order.verify(complaintService).linkWorkflowInstance(COMPLAINT_ID, WORKFLOW_INSTANCE_ID);
        order.verify(complaintNotificationIntegration).onSubmitted(linked);
        order.verify(complaintAuditIntegration).onSubmitted(linked, USER_ID);
        order.verify(complaintSearchIntegration).onSubmitted(linked);
    }

    @Test
    void shouldOrchestrateAssignInDomainWorkflowNotificationAuditSearchOrder() {
        ComplaintAssignmentCreateRequest request = new ComplaintAssignmentCreateRequest(
                COMPLAINT_ID, null, null, null, USER_ID, USER_ID, "remarks", true);
        ComplaintDto assigned = sampleComplaintDto();
        when(complaintService.assign(COMPLAINT_ID, request)).thenReturn(assigned);

        assertThat(complaintApplicationService.assign(COMPLAINT_ID, request)).isEqualTo(assigned);

        InOrder order = inOrder(
                complaintService,
                complaintWorkflowIntegration,
                complaintNotificationIntegration,
                complaintAuditIntegration,
                complaintSearchIntegration);
        order.verify(complaintService).assign(COMPLAINT_ID, request);
        order.verify(complaintWorkflowIntegration).createTaskOnAssign(assigned, request, USER_ID);
        order.verify(complaintNotificationIntegration).onAssigned(assigned, request);
        order.verify(complaintAuditIntegration).onAssigned(assigned, USER_ID);
        order.verify(complaintSearchIntegration).onAssigned(assigned);
    }

    @Test
    void shouldOrchestrateRejectWithNotificationAndAuditWithoutSearch() {
        ComplaintDto rejected = sampleComplaintDto();
        when(complaintService.reject(COMPLAINT_ID, USER_ID, "INVALID")).thenReturn(rejected);

        assertThat(complaintApplicationService.reject(COMPLAINT_ID, USER_ID, "INVALID")).isEqualTo(rejected);
        verify(complaintNotificationIntegration).onRejected(rejected, "INVALID");
        verify(complaintAuditIntegration).onRejected(rejected, USER_ID);
        verifyNoInteractions(complaintSearchIntegration);
    }

    @Test
    void shouldNotIndexOnAccept() {
        ComplaintDto accepted = sampleComplaintDto();
        when(complaintService.accept(COMPLAINT_ID, USER_ID)).thenReturn(accepted);

        assertThat(complaintApplicationService.accept(COMPLAINT_ID, USER_ID)).isEqualTo(accepted);
        verifyNoInteractions(complaintSearchIntegration);
    }

    @Test
    void shouldPropagateSearchFailureAfterAuditForRollback() {
        ComplaintDto updated = sampleComplaintDto();
        ComplaintUpdateRequest updateRequest = new ComplaintUpdateRequest(
                "Updated title", null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, true, 0L);
        when(complaintService.update(COMPLAINT_ID, updateRequest)).thenReturn(updated);
        doThrow(new ComplaintSearchIntegrationException("Search integration failed"))
                .when(complaintSearchIntegration).onUpdated(updated);

        assertThatThrownBy(() -> complaintApplicationService.update(COMPLAINT_ID, updateRequest))
                .isInstanceOf(ComplaintSearchIntegrationException.class);

        verify(complaintService).update(COMPLAINT_ID, updateRequest);
        verify(complaintAuditIntegration).onUpdated(updated, null);
    }

    @Test
    void shouldSearchSoftDeleteAfterAudit() {
        ComplaintDto complaint = sampleComplaintDto();
        when(complaintService.getById(COMPLAINT_ID)).thenReturn(complaint);

        complaintApplicationService.softDelete(COMPLAINT_ID);

        InOrder order = inOrder(complaintService, complaintAuditIntegration, complaintSearchIntegration);
        order.verify(complaintService).getById(COMPLAINT_ID);
        order.verify(complaintService).softDelete(COMPLAINT_ID);
        order.verify(complaintAuditIntegration).onSoftDeleted(complaint, null);
        order.verify(complaintSearchIntegration).onSoftDeleted(complaint);
    }

    @Test
    void shouldNotInvokeWorkflowOrNotificationOnArchiveButShouldAuditAndSearch() {
        ComplaintDto archived = sampleComplaintDto();
        when(complaintService.archive(COMPLAINT_ID, USER_ID)).thenReturn(archived);

        assertThat(complaintApplicationService.archive(COMPLAINT_ID, USER_ID)).isEqualTo(archived);
        verify(complaintAuditIntegration).onArchived(archived, USER_ID);
        verify(complaintSearchIntegration).onArchived(archived);
        verifyNoInteractions(complaintWorkflowIntegration, complaintNotificationIntegration);
    }

    @Test
    void shouldOrchestrateFeedbackUpdateWithoutSearchIndexing() {
        ComplaintFeedbackUpdateRequest updateRequest = new ComplaintFeedbackUpdateRequest(null, "Updated", true, 0L);
        ComplaintFeedbackDto existing = new ComplaintFeedbackDto(
                FEEDBACK_ID, "FB-001", COMPLAINT_ID, USER_ID, null, null, null, true, 0L, null, null, null, null);
        ComplaintFeedbackDto updated = new ComplaintFeedbackDto(
                FEEDBACK_ID, "FB-001", COMPLAINT_ID, USER_ID, null, "Updated", null, true, 1L, null, null, null, null);
        ComplaintDto complaint = sampleComplaintDto();

        when(complaintFeedbackService.getFeedback(COMPLAINT_ID)).thenReturn(existing);
        when(complaintFeedbackService.updateFeedback(FEEDBACK_ID, updateRequest)).thenReturn(updated);
        when(complaintService.getById(COMPLAINT_ID)).thenReturn(complaint);

        assertThat(complaintApplicationService.updateFeedback(COMPLAINT_ID, updateRequest)).isEqualTo(updated);
        verify(complaintAuditIntegration).onFeedbackUpdated(complaint, USER_ID);
        verifyNoInteractions(complaintNotificationIntegration, complaintSearchIntegration);
    }

    private static ComplaintDto sampleComplaintDtoWithWorkflow() {
        return new ComplaintDto(
                COMPLAINT_ID, "CMP-2026-0001", "Water leak", "Description",
                ComplaintStatus.DRAFT, ComplaintPriority.MEDIUM, ComplaintSource.CITIZEN_PORTAL,
                "WEB", "WATER_SUPPLY", "PIPE_LEAK", "SERVICE_REQUEST",
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                null, null, null, null, null, WORKFLOW_INSTANCE_ID,
                null, null, null, null, null, false,
                null, null, "KA", "BLR", null, null, null,
                new BigDecimal("12.9716000"), new BigDecimal("77.5946000"),
                "Main Street", null, "560001", null,
                true, 0L, null, null, null, null);
    }

    private static ComplaintCreateRequest sampleCreateRequest() {
        return new ComplaintCreateRequest(
                "Water leak", "Description", ComplaintPriority.MEDIUM, ComplaintSource.CITIZEN_PORTAL,
                "WEB", "WATER_SUPPLY", "PIPE_LEAK", "SERVICE_REQUEST",
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, null,
                "KA", "BLR", null, null, null,
                new BigDecimal("12.9716000"), new BigDecimal("77.5946000"),
                "Main Street", null, "560001", null, true);
    }

    private static ComplaintDto sampleComplaintDto() {
        return new ComplaintDto(
                COMPLAINT_ID, "CMP-2026-0001", "Water leak", "Description",
                ComplaintStatus.DRAFT, ComplaintPriority.MEDIUM, ComplaintSource.CITIZEN_PORTAL,
                "WEB", "WATER_SUPPLY", "PIPE_LEAK", "SERVICE_REQUEST",
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                null, null, null, null, null, null,
                null, null, null, null, null, false,
                null, null, "KA", "BLR", null, null, null,
                new BigDecimal("12.9716000"), new BigDecimal("77.5946000"),
                "Main Street", null, "560001", null,
                true, 0L, null, null, null, null);
    }
}
