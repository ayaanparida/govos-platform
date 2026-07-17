package com.govos.api.cmp.application;

import com.govos.api.cmp.audit.ComplaintAuditIntegration;
import com.govos.api.cmp.notification.ComplaintNotificationIntegration;
import com.govos.api.cmp.search.ComplaintSearchIntegration;
import com.govos.api.cmp.workflow.ComplaintWorkflowIntegration;
import com.govos.cmp.dto.ComplaintAssignmentCreateRequest;
import com.govos.cmp.dto.ComplaintAssignmentDto;
import com.govos.cmp.dto.ComplaintAttachmentCreateRequest;
import com.govos.cmp.dto.ComplaintAttachmentDto;
import com.govos.cmp.dto.ComplaintCommentCreateRequest;
import com.govos.cmp.dto.ComplaintCommentDto;
import com.govos.cmp.dto.ComplaintCreateRequest;
import com.govos.cmp.dto.ComplaintDuplicateCreateRequest;
import com.govos.cmp.dto.ComplaintDuplicateDto;
import com.govos.cmp.dto.ComplaintDto;
import com.govos.cmp.dto.ComplaintEscalationCreateRequest;
import com.govos.cmp.dto.ComplaintEscalationDto;
import com.govos.cmp.dto.ComplaintFeedbackCreateRequest;
import com.govos.cmp.dto.ComplaintFeedbackDto;
import com.govos.cmp.dto.ComplaintFeedbackUpdateRequest;
import com.govos.cmp.dto.ComplaintMergeCreateRequest;
import com.govos.cmp.dto.ComplaintMergeDto;
import com.govos.cmp.dto.ComplaintUpdateRequest;
import com.govos.cmp.service.ComplaintAssignmentService;
import com.govos.cmp.service.ComplaintAttachmentService;
import com.govos.cmp.service.ComplaintCommentService;
import com.govos.cmp.service.ComplaintDuplicateService;
import com.govos.cmp.service.ComplaintEscalationService;
import com.govos.cmp.service.ComplaintFeedbackService;
import com.govos.cmp.service.ComplaintMergeService;
import com.govos.cmp.service.ComplaintService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ComplaintApplicationServiceImpl implements ComplaintApplicationService {

    private final ComplaintService complaintService;
    private final ComplaintCommentService complaintCommentService;
    private final ComplaintAttachmentService complaintAttachmentService;
    private final ComplaintFeedbackService complaintFeedbackService;
    private final ComplaintAssignmentService complaintAssignmentService;
    private final ComplaintEscalationService complaintEscalationService;
    private final ComplaintDuplicateService complaintDuplicateService;
    private final ComplaintMergeService complaintMergeService;
    private final ComplaintWorkflowIntegration complaintWorkflowIntegration;
    private final ComplaintNotificationIntegration complaintNotificationIntegration;
    private final ComplaintAuditIntegration complaintAuditIntegration;
    private final ComplaintSearchIntegration complaintSearchIntegration;

    public ComplaintApplicationServiceImpl(
            ComplaintService complaintService,
            ComplaintCommentService complaintCommentService,
            ComplaintAttachmentService complaintAttachmentService,
            ComplaintFeedbackService complaintFeedbackService,
            ComplaintAssignmentService complaintAssignmentService,
            ComplaintEscalationService complaintEscalationService,
            ComplaintDuplicateService complaintDuplicateService,
            ComplaintMergeService complaintMergeService,
            ComplaintWorkflowIntegration complaintWorkflowIntegration,
            ComplaintNotificationIntegration complaintNotificationIntegration,
            ComplaintAuditIntegration complaintAuditIntegration,
            ComplaintSearchIntegration complaintSearchIntegration) {
        this.complaintService = complaintService;
        this.complaintCommentService = complaintCommentService;
        this.complaintAttachmentService = complaintAttachmentService;
        this.complaintFeedbackService = complaintFeedbackService;
        this.complaintAssignmentService = complaintAssignmentService;
        this.complaintEscalationService = complaintEscalationService;
        this.complaintDuplicateService = complaintDuplicateService;
        this.complaintMergeService = complaintMergeService;
        this.complaintWorkflowIntegration = complaintWorkflowIntegration;
        this.complaintNotificationIntegration = complaintNotificationIntegration;
        this.complaintAuditIntegration = complaintAuditIntegration;
        this.complaintSearchIntegration = complaintSearchIntegration;
    }

    @Override
    @Transactional
    public ComplaintDto create(ComplaintCreateRequest request) {
        ComplaintDto created = complaintService.create(request);
        complaintAuditIntegration.onCreated(created, created.submittedByUserId());
        complaintSearchIntegration.onCreated(created);
        return created;
    }

    @Override
    @Transactional
    public ComplaintDto update(UUID id, ComplaintUpdateRequest request) {
        ComplaintDto updated = complaintService.update(id, request);
        complaintAuditIntegration.onUpdated(updated, null);
        complaintSearchIntegration.onUpdated(updated);
        return updated;
    }

    @Override
    public ComplaintDto getById(UUID id) {
        return complaintService.getById(id);
    }

    @Override
    public ComplaintDto getByCode(String code) {
        return complaintService.getByCode(code);
    }

    @Override
    public Page<ComplaintDto> list(Pageable pageable) {
        return complaintService.list(pageable);
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        ComplaintDto complaint = complaintService.getById(id);
        complaintService.softDelete(id);
        complaintAuditIntegration.onSoftDeleted(complaint, null);
        complaintSearchIntegration.onSoftDeleted(complaint);
    }

    @Override
    @Transactional
    public ComplaintDto restore(UUID id) {
        ComplaintDto restored = complaintService.restore(id);
        complaintAuditIntegration.onRestored(restored, null);
        complaintSearchIntegration.onRestored(restored);
        return restored;
    }

    @Override
    @Transactional
    public ComplaintDto submit(UUID id, UUID changedByUserId) {
        ComplaintDto submitted = complaintService.submit(id, changedByUserId);
        UUID workflowInstanceId = complaintWorkflowIntegration.startWorkflowOnSubmit(submitted, changedByUserId);
        ComplaintDto linked = complaintService.linkWorkflowInstance(id, workflowInstanceId);
        complaintNotificationIntegration.onSubmitted(linked);
        complaintAuditIntegration.onSubmitted(linked, changedByUserId);
        complaintSearchIntegration.onSubmitted(linked);
        return linked;
    }

    @Override
    @Transactional
    public ComplaintDto accept(UUID id, UUID changedByUserId) {
        ComplaintDto accepted = complaintService.accept(id, changedByUserId);
        complaintNotificationIntegration.onAccepted(accepted);
        complaintAuditIntegration.onAccepted(accepted, changedByUserId);
        return accepted;
    }

    @Override
    @Transactional
    public ComplaintDto reject(UUID id, UUID changedByUserId, String rejectionReasonKey) {
        ComplaintDto rejected = complaintService.reject(id, changedByUserId, rejectionReasonKey);
        complaintNotificationIntegration.onRejected(rejected, rejectionReasonKey);
        complaintAuditIntegration.onRejected(rejected, changedByUserId);
        return rejected;
    }

    @Override
    @Transactional
    public ComplaintDto assign(UUID id, ComplaintAssignmentCreateRequest assignmentRequest) {
        ComplaintDto assigned = complaintService.assign(id, assignmentRequest);
        complaintWorkflowIntegration.createTaskOnAssign(
                assigned, assignmentRequest, assignmentRequest.assignedByUserId());
        complaintNotificationIntegration.onAssigned(assigned, assignmentRequest);
        complaintAuditIntegration.onAssigned(assigned, assignmentRequest.assignedByUserId());
        complaintSearchIntegration.onAssigned(assigned);
        return assigned;
    }

    @Override
    @Transactional
    public ComplaintDto startProgress(UUID id, UUID changedByUserId) {
        ComplaintDto inProgress = complaintService.startProgress(id, changedByUserId);
        complaintNotificationIntegration.onInProgress(inProgress);
        complaintAuditIntegration.onInProgress(inProgress, changedByUserId);
        complaintSearchIntegration.onInProgress(inProgress);
        return inProgress;
    }

    @Override
    @Transactional
    public ComplaintDto resolve(UUID id, UUID changedByUserId, String reasonKey) {
        ComplaintDto resolved = complaintService.resolve(id, changedByUserId, reasonKey);
        complaintWorkflowIntegration.moveToApprovalOnResolve(resolved, changedByUserId);
        complaintNotificationIntegration.onResolved(resolved);
        complaintAuditIntegration.onResolved(resolved, changedByUserId);
        complaintSearchIntegration.onResolved(resolved);
        return resolved;
    }

    @Override
    @Transactional
    public ComplaintDto close(UUID id, UUID changedByUserId, String closureReasonKey) {
        ComplaintDto closed = complaintService.close(id, changedByUserId, closureReasonKey);
        complaintWorkflowIntegration.completeWorkflowOnClose(closed, changedByUserId);
        complaintNotificationIntegration.onClosed(closed);
        complaintAuditIntegration.onClosed(closed, changedByUserId);
        complaintSearchIntegration.onClosed(closed);
        return closed;
    }

    @Override
    @Transactional
    public ComplaintDto archive(UUID id, UUID changedByUserId) {
        ComplaintDto archived = complaintService.archive(id, changedByUserId);
        complaintAuditIntegration.onArchived(archived, changedByUserId);
        complaintSearchIntegration.onArchived(archived);
        return archived;
    }

    @Override
    @Transactional
    public ComplaintDto reopen(UUID id, UUID changedByUserId, String reasonKey) {
        ComplaintDto reopened = complaintService.reopen(id, changedByUserId, reasonKey);
        complaintWorkflowIntegration.createFollowUpTaskOnReopen(reopened, changedByUserId);
        complaintNotificationIntegration.onReopened(reopened);
        complaintAuditIntegration.onReopened(reopened, changedByUserId);
        complaintSearchIntegration.onReopened(reopened);
        return reopened;
    }

    @Override
    @Transactional
    public ComplaintDto requestReassignment(UUID id, UUID changedByUserId, String rejectionReasonKey) {
        ComplaintDto pending = complaintService.requestReassignment(id, changedByUserId, rejectionReasonKey);
        complaintWorkflowIntegration.reassignCurrentTask(pending, changedByUserId);
        complaintNotificationIntegration.onReassignmentRequested(pending);
        complaintAuditIntegration.onReassigned(pending, changedByUserId);
        return pending;
    }

    @Override
    @Transactional
    public ComplaintDto markDuplicate(
            UUID id,
            ComplaintDuplicateCreateRequest duplicateRequest,
            UUID changedByUserId) {
        ComplaintDto duplicate = complaintService.markDuplicate(id, duplicateRequest, changedByUserId);
        complaintAuditIntegration.onDuplicateCreated(duplicate, changedByUserId);
        complaintSearchIntegration.onDuplicateCreated(duplicate);
        return duplicate;
    }

    @Override
    @Transactional
    public ComplaintDto merge(
            UUID survivingComplaintId,
            ComplaintMergeCreateRequest mergeRequest,
            UUID changedByUserId) {
        ComplaintDto merged = complaintService.merge(survivingComplaintId, mergeRequest, changedByUserId);
        complaintAuditIntegration.onMergeCreated(merged, changedByUserId);
        complaintSearchIntegration.onMergeCreated(merged);
        return merged;
    }

    @Override
    @Transactional
    public ComplaintCommentDto addComment(ComplaintCommentCreateRequest request) {
        ComplaintCommentDto comment = complaintCommentService.addComment(request);
        ComplaintDto complaint = complaintService.getById(request.complaintId());
        complaintNotificationIntegration.onCommentAdded(complaint, comment);
        complaintAuditIntegration.onCommentAdded(complaint, request.authorUserId());
        complaintSearchIntegration.onCommentAdded(complaint);
        return comment;
    }

    @Override
    public List<ComplaintCommentDto> listComments(UUID complaintId) {
        return complaintCommentService.listComments(complaintId);
    }

    @Override
    @Transactional
    public ComplaintAttachmentDto addAttachment(ComplaintAttachmentCreateRequest request) {
        ComplaintAttachmentDto attachment = complaintAttachmentService.addAttachment(request);
        ComplaintDto complaint = complaintService.getById(request.complaintId());
        complaintAuditIntegration.onAttachmentAdded(complaint, request.uploadedByUserId());
        complaintSearchIntegration.onAttachmentAdded(complaint);
        return attachment;
    }

    @Override
    public List<ComplaintAttachmentDto> listAttachments(UUID complaintId) {
        return complaintAttachmentService.listAttachments(complaintId);
    }

    @Override
    @Transactional
    public ComplaintFeedbackDto createFeedback(ComplaintFeedbackCreateRequest request) {
        ComplaintFeedbackDto feedback = complaintFeedbackService.createFeedback(request);
        ComplaintDto complaint = complaintService.getById(request.complaintId());
        complaintAuditIntegration.onFeedbackSubmitted(complaint, request.ratedByUserId());
        return feedback;
    }

    @Override
    @Transactional
    public ComplaintFeedbackDto updateFeedback(UUID complaintId, ComplaintFeedbackUpdateRequest request) {
        ComplaintFeedbackDto existing = complaintFeedbackService.getFeedback(complaintId);
        ComplaintFeedbackDto updated = complaintFeedbackService.updateFeedback(existing.id(), request);
        ComplaintDto complaint = complaintService.getById(complaintId);
        complaintAuditIntegration.onFeedbackUpdated(complaint, existing.ratedByUserId());
        return updated;
    }

    @Override
    public ComplaintFeedbackDto getFeedback(UUID complaintId) {
        return complaintFeedbackService.getFeedback(complaintId);
    }

    @Override
    public List<ComplaintAssignmentDto> listAssignments(UUID complaintId) {
        return complaintAssignmentService.listAssignments(complaintId);
    }

    @Override
    @Transactional
    public ComplaintEscalationDto createEscalation(ComplaintEscalationCreateRequest request) {
        ComplaintEscalationDto escalation = complaintEscalationService.createEscalation(request);
        ComplaintDto complaint = complaintService.getById(request.complaintId());
        complaintNotificationIntegration.onEscalated(complaint, escalation);
        complaintAuditIntegration.onEscalated(complaint, request.escalatedByUserId());
        return escalation;
    }

    @Override
    public List<ComplaintEscalationDto> listEscalations(UUID complaintId) {
        return complaintEscalationService.listEscalations(complaintId);
    }

    @Override
    public List<ComplaintDuplicateDto> listDuplicates(UUID primaryComplaintId) {
        return complaintDuplicateService.listDuplicates(primaryComplaintId);
    }

    @Override
    public List<ComplaintMergeDto> listMerges(UUID survivingComplaintId) {
        return complaintMergeService.listMerges(survivingComplaintId);
    }
}
