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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ComplaintApplicationService {

    ComplaintDto create(ComplaintCreateRequest request);

    ComplaintDto update(UUID id, ComplaintUpdateRequest request);

    ComplaintDto getById(UUID id);

    ComplaintDto getByCode(String code);

    Page<ComplaintDto> list(Pageable pageable);

    void softDelete(UUID id);

    ComplaintDto restore(UUID id);

    ComplaintDto submit(UUID id, UUID changedByUserId);

    ComplaintDto accept(UUID id, UUID changedByUserId);

    ComplaintDto reject(UUID id, UUID changedByUserId, String rejectionReasonKey);

    ComplaintDto assign(UUID id, ComplaintAssignmentCreateRequest assignmentRequest);

    ComplaintDto startProgress(UUID id, UUID changedByUserId);

    ComplaintDto resolve(UUID id, UUID changedByUserId, String reasonKey);

    ComplaintDto close(UUID id, UUID changedByUserId, String closureReasonKey);

    ComplaintDto archive(UUID id, UUID changedByUserId);

    ComplaintDto reopen(UUID id, UUID changedByUserId, String reasonKey);

    ComplaintDto requestReassignment(UUID id, UUID changedByUserId, String rejectionReasonKey);

    ComplaintDto markDuplicate(UUID id, ComplaintDuplicateCreateRequest duplicateRequest, UUID changedByUserId);

    ComplaintDto merge(UUID survivingComplaintId, ComplaintMergeCreateRequest mergeRequest, UUID changedByUserId);

    ComplaintCommentDto addComment(ComplaintCommentCreateRequest request);

    List<ComplaintCommentDto> listComments(UUID complaintId);

    ComplaintAttachmentDto addAttachment(ComplaintAttachmentCreateRequest request);

    List<ComplaintAttachmentDto> listAttachments(UUID complaintId);

    ComplaintFeedbackDto createFeedback(ComplaintFeedbackCreateRequest request);

    ComplaintFeedbackDto updateFeedback(UUID complaintId, ComplaintFeedbackUpdateRequest request);

    ComplaintFeedbackDto getFeedback(UUID complaintId);

    List<ComplaintAssignmentDto> listAssignments(UUID complaintId);

    ComplaintEscalationDto createEscalation(ComplaintEscalationCreateRequest request);

    List<ComplaintEscalationDto> listEscalations(UUID complaintId);

    List<ComplaintDuplicateDto> listDuplicates(UUID primaryComplaintId);

    List<ComplaintMergeDto> listMerges(UUID survivingComplaintId);
}
