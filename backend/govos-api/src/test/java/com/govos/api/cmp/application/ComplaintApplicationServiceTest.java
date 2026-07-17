package com.govos.api.cmp.application;

import com.govos.cmp.dto.ComplaintAssignmentCreateRequest;
import com.govos.cmp.dto.ComplaintCommentCreateRequest;
import com.govos.cmp.dto.ComplaintCreateRequest;
import com.govos.cmp.dto.ComplaintDuplicateCreateRequest;
import com.govos.cmp.dto.ComplaintDto;
import com.govos.cmp.dto.ComplaintFeedbackDto;
import com.govos.cmp.dto.ComplaintFeedbackUpdateRequest;
import com.govos.cmp.dto.ComplaintMergeCreateRequest;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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

    private ComplaintApplicationService complaintApplicationService;

    private static final UUID COMPLAINT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID USER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID FEEDBACK_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

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
                complaintMergeService);
    }

    @Test
    void shouldDelegateCreateToComplaintService() {
        ComplaintCreateRequest request = sampleCreateRequest();
        ComplaintDto expected = sampleComplaintDto();
        when(complaintService.create(request)).thenReturn(expected);

        assertThat(complaintApplicationService.create(request)).isEqualTo(expected);
        verify(complaintService).create(request);
    }

    @Test
    void shouldDelegateGetByIdToComplaintService() {
        ComplaintDto expected = sampleComplaintDto();
        when(complaintService.getById(COMPLAINT_ID)).thenReturn(expected);

        assertThat(complaintApplicationService.getById(COMPLAINT_ID)).isEqualTo(expected);
        verify(complaintService).getById(COMPLAINT_ID);
    }

    @Test
    void shouldDelegateSubmitToComplaintService() {
        ComplaintDto expected = sampleComplaintDto();
        when(complaintService.submit(COMPLAINT_ID, USER_ID)).thenReturn(expected);

        assertThat(complaintApplicationService.submit(COMPLAINT_ID, USER_ID)).isEqualTo(expected);
        verify(complaintService).submit(COMPLAINT_ID, USER_ID);
    }

    @Test
    void shouldDelegateRejectToComplaintService() {
        ComplaintDto expected = sampleComplaintDto();
        when(complaintService.reject(COMPLAINT_ID, USER_ID, "INVALID")).thenReturn(expected);

        assertThat(complaintApplicationService.reject(COMPLAINT_ID, USER_ID, "INVALID")).isEqualTo(expected);
        verify(complaintService).reject(COMPLAINT_ID, USER_ID, "INVALID");
    }

    @Test
    void shouldDelegateSoftDeleteToComplaintService() {
        complaintApplicationService.softDelete(COMPLAINT_ID);

        verify(complaintService).softDelete(COMPLAINT_ID);
    }

    @Test
    void shouldDelegateAssignToComplaintService() {
        ComplaintAssignmentCreateRequest request = new ComplaintAssignmentCreateRequest(
                COMPLAINT_ID, null, null, null, null, USER_ID, "remarks", true);
        ComplaintDto expected = sampleComplaintDto();
        when(complaintService.assign(COMPLAINT_ID, request)).thenReturn(expected);

        assertThat(complaintApplicationService.assign(COMPLAINT_ID, request)).isEqualTo(expected);
        verify(complaintService).assign(COMPLAINT_ID, request);
    }

    @Test
    void shouldDelegateAddCommentToCommentService() {
        ComplaintCommentCreateRequest request = new ComplaintCommentCreateRequest(
                COMPLAINT_ID, USER_ID, "text", null, null, true);
        when(complaintCommentService.addComment(request)).thenReturn(null);

        complaintApplicationService.addComment(request);

        verify(complaintCommentService).addComment(request);
    }

    @Test
    void shouldOrchestrateFeedbackUpdateThroughFeedbackService() {
        ComplaintFeedbackUpdateRequest updateRequest = new ComplaintFeedbackUpdateRequest(null, "Updated", true, 0L);
        ComplaintFeedbackDto existing = new ComplaintFeedbackDto(
                FEEDBACK_ID, "FB-001", COMPLAINT_ID, USER_ID, null, null, null, true, 0L, null, null, null, null);
        ComplaintFeedbackDto updated = new ComplaintFeedbackDto(
                FEEDBACK_ID, "FB-001", COMPLAINT_ID, USER_ID, null, "Updated", null, true, 1L, null, null, null, null);

        when(complaintFeedbackService.getFeedback(COMPLAINT_ID)).thenReturn(existing);
        when(complaintFeedbackService.updateFeedback(FEEDBACK_ID, updateRequest)).thenReturn(updated);

        assertThat(complaintApplicationService.updateFeedback(COMPLAINT_ID, updateRequest)).isEqualTo(updated);
        verify(complaintFeedbackService).getFeedback(COMPLAINT_ID);
        verify(complaintFeedbackService).updateFeedback(eq(FEEDBACK_ID), eq(updateRequest));
    }

    @Test
    void shouldDelegateMergeToComplaintService() {
        ComplaintMergeCreateRequest mergeRequest = new ComplaintMergeCreateRequest(
                COMPLAINT_ID, UUID.randomUUID(), USER_ID, "reason", null, true);
        ComplaintDto expected = sampleComplaintDto();
        when(complaintService.merge(COMPLAINT_ID, mergeRequest, USER_ID)).thenReturn(expected);

        assertThat(complaintApplicationService.merge(COMPLAINT_ID, mergeRequest, USER_ID)).isEqualTo(expected);
        verify(complaintService).merge(COMPLAINT_ID, mergeRequest, USER_ID);
    }

    @Test
    void shouldDelegateMarkDuplicateToComplaintService() {
        ComplaintDuplicateCreateRequest duplicateRequest = new ComplaintDuplicateCreateRequest(
                COMPLAINT_ID, UUID.randomUUID(), null, USER_ID, null, null, true);
        ComplaintDto expected = sampleComplaintDto();
        when(complaintService.markDuplicate(COMPLAINT_ID, duplicateRequest, USER_ID)).thenReturn(expected);

        assertThat(complaintApplicationService.markDuplicate(COMPLAINT_ID, duplicateRequest, USER_ID))
                .isEqualTo(expected);
        verify(complaintService).markDuplicate(COMPLAINT_ID, duplicateRequest, USER_ID);
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
