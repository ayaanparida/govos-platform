package com.govos.api.cmp.application;

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

    public ComplaintApplicationServiceImpl(
            ComplaintService complaintService,
            ComplaintCommentService complaintCommentService,
            ComplaintAttachmentService complaintAttachmentService,
            ComplaintFeedbackService complaintFeedbackService,
            ComplaintAssignmentService complaintAssignmentService,
            ComplaintEscalationService complaintEscalationService,
            ComplaintDuplicateService complaintDuplicateService,
            ComplaintMergeService complaintMergeService) {
        this.complaintService = complaintService;
        this.complaintCommentService = complaintCommentService;
        this.complaintAttachmentService = complaintAttachmentService;
        this.complaintFeedbackService = complaintFeedbackService;
        this.complaintAssignmentService = complaintAssignmentService;
        this.complaintEscalationService = complaintEscalationService;
        this.complaintDuplicateService = complaintDuplicateService;
        this.complaintMergeService = complaintMergeService;
    }

    @Override
    @Transactional
    public ComplaintDto create(ComplaintCreateRequest request) {
        return complaintService.create(request);
    }

    @Override
    @Transactional
    public ComplaintDto update(UUID id, ComplaintUpdateRequest request) {
        return complaintService.update(id, request);
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
        complaintService.softDelete(id);
    }

    @Override
    @Transactional
    public ComplaintDto restore(UUID id) {
        return complaintService.restore(id);
    }

    @Override
    @Transactional
    public ComplaintDto submit(UUID id, UUID changedByUserId) {
        return complaintService.submit(id, changedByUserId);
    }

    @Override
    @Transactional
    public ComplaintDto accept(UUID id, UUID changedByUserId) {
        return complaintService.accept(id, changedByUserId);
    }

    @Override
    @Transactional
    public ComplaintDto reject(UUID id, UUID changedByUserId, String rejectionReasonKey) {
        return complaintService.reject(id, changedByUserId, rejectionReasonKey);
    }

    @Override
    @Transactional
    public ComplaintDto assign(UUID id, ComplaintAssignmentCreateRequest assignmentRequest) {
        return complaintService.assign(id, assignmentRequest);
    }

    @Override
    @Transactional
    public ComplaintDto startProgress(UUID id, UUID changedByUserId) {
        return complaintService.startProgress(id, changedByUserId);
    }

    @Override
    @Transactional
    public ComplaintDto resolve(UUID id, UUID changedByUserId, String reasonKey) {
        return complaintService.resolve(id, changedByUserId, reasonKey);
    }

    @Override
    @Transactional
    public ComplaintDto close(UUID id, UUID changedByUserId, String closureReasonKey) {
        return complaintService.close(id, changedByUserId, closureReasonKey);
    }

    @Override
    @Transactional
    public ComplaintDto archive(UUID id, UUID changedByUserId) {
        return complaintService.archive(id, changedByUserId);
    }

    @Override
    @Transactional
    public ComplaintDto reopen(UUID id, UUID changedByUserId, String reasonKey) {
        return complaintService.reopen(id, changedByUserId, reasonKey);
    }

    @Override
    @Transactional
    public ComplaintDto requestReassignment(UUID id, UUID changedByUserId, String rejectionReasonKey) {
        return complaintService.requestReassignment(id, changedByUserId, rejectionReasonKey);
    }

    @Override
    @Transactional
    public ComplaintDto markDuplicate(
            UUID id,
            ComplaintDuplicateCreateRequest duplicateRequest,
            UUID changedByUserId) {
        return complaintService.markDuplicate(id, duplicateRequest, changedByUserId);
    }

    @Override
    @Transactional
    public ComplaintDto merge(
            UUID survivingComplaintId,
            ComplaintMergeCreateRequest mergeRequest,
            UUID changedByUserId) {
        return complaintService.merge(survivingComplaintId, mergeRequest, changedByUserId);
    }

    @Override
    @Transactional
    public ComplaintCommentDto addComment(ComplaintCommentCreateRequest request) {
        return complaintCommentService.addComment(request);
    }

    @Override
    public List<ComplaintCommentDto> listComments(UUID complaintId) {
        return complaintCommentService.listComments(complaintId);
    }

    @Override
    @Transactional
    public ComplaintAttachmentDto addAttachment(ComplaintAttachmentCreateRequest request) {
        return complaintAttachmentService.addAttachment(request);
    }

    @Override
    public List<ComplaintAttachmentDto> listAttachments(UUID complaintId) {
        return complaintAttachmentService.listAttachments(complaintId);
    }

    @Override
    @Transactional
    public ComplaintFeedbackDto createFeedback(ComplaintFeedbackCreateRequest request) {
        return complaintFeedbackService.createFeedback(request);
    }

    @Override
    @Transactional
    public ComplaintFeedbackDto updateFeedback(UUID complaintId, ComplaintFeedbackUpdateRequest request) {
        ComplaintFeedbackDto existing = complaintFeedbackService.getFeedback(complaintId);
        return complaintFeedbackService.updateFeedback(existing.id(), request);
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
        return complaintEscalationService.createEscalation(request);
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
