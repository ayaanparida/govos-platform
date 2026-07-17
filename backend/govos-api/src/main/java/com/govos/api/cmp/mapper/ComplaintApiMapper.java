package com.govos.api.cmp.mapper;

import com.govos.api.cmp.request.AddAttachmentRequest;
import com.govos.api.cmp.request.AddCommentRequest;
import com.govos.api.cmp.request.AssignComplaintRequest;
import com.govos.api.cmp.request.CreateEscalationRequest;
import com.govos.api.cmp.request.CreateFeedbackRequest;
import com.govos.api.cmp.request.MarkDuplicateRequest;
import com.govos.api.cmp.request.MergeComplaintRequest;
import com.govos.cmp.dto.ComplaintAssignmentCreateRequest;
import com.govos.cmp.dto.ComplaintAttachmentCreateRequest;
import com.govos.cmp.dto.ComplaintCommentCreateRequest;
import com.govos.cmp.dto.ComplaintDuplicateCreateRequest;
import com.govos.cmp.dto.ComplaintEscalationCreateRequest;
import com.govos.cmp.dto.ComplaintFeedbackCreateRequest;
import com.govos.cmp.dto.ComplaintMergeCreateRequest;
import com.govos.security.jwt.JwtPrincipal;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ComplaintApiMapper {

    public ComplaintAssignmentCreateRequest toAssignmentCreateRequest(
            UUID complaintId,
            AssignComplaintRequest request,
            JwtPrincipal currentUser) {
        return new ComplaintAssignmentCreateRequest(
                complaintId,
                request.assignmentType(),
                request.departmentId(),
                request.officeId(),
                request.officerUserId(),
                currentUser.getUserId(),
                request.remarks(),
                request.active());
    }

    public ComplaintCommentCreateRequest toCommentCreateRequest(
            UUID complaintId,
            AddCommentRequest request,
            JwtPrincipal currentUser) {
        return new ComplaintCommentCreateRequest(
                complaintId,
                currentUser.getUserId(),
                request.commentText(),
                request.visibility(),
                request.commentType(),
                request.active());
    }

    public ComplaintAttachmentCreateRequest toAttachmentCreateRequest(
            UUID complaintId,
            AddAttachmentRequest request,
            JwtPrincipal currentUser) {
        return new ComplaintAttachmentCreateRequest(
                complaintId,
                request.documentId(),
                request.documentVersionId(),
                request.attachmentType(),
                request.displayName(),
                currentUser.getUserId(),
                request.sortOrder(),
                request.active());
    }

    public ComplaintFeedbackCreateRequest toFeedbackCreateRequest(
            UUID complaintId,
            CreateFeedbackRequest request,
            JwtPrincipal currentUser) {
        return new ComplaintFeedbackCreateRequest(
                complaintId,
                currentUser.getUserId(),
                request.rating(),
                request.feedback(),
                request.ratedAt(),
                request.active());
    }

    public ComplaintEscalationCreateRequest toEscalationCreateRequest(
            UUID complaintId,
            CreateEscalationRequest request,
            JwtPrincipal currentUser) {
        return new ComplaintEscalationCreateRequest(
                complaintId,
                request.escalationLevel(),
                request.escalationReason(),
                currentUser.getUserId(),
                request.escalatedToUserId(),
                request.escalatedToDepartmentId(),
                request.remarks(),
                request.escalatedAt(),
                request.active());
    }

    public ComplaintDuplicateCreateRequest toDuplicateCreateRequest(
            UUID duplicateComplaintId,
            MarkDuplicateRequest request,
            JwtPrincipal currentUser) {
        return new ComplaintDuplicateCreateRequest(
                request.primaryComplaintId(),
                duplicateComplaintId,
                request.detectedBy(),
                currentUser.getUserId(),
                request.similarityScore(),
                request.remarks(),
                request.active());
    }

    public ComplaintMergeCreateRequest toMergeCreateRequest(
            MergeComplaintRequest request,
            JwtPrincipal currentUser) {
        return new ComplaintMergeCreateRequest(
                request.survivingComplaintId(),
                request.mergedComplaintId(),
                currentUser.getUserId(),
                request.mergeReason(),
                request.mergedAt(),
                request.active());
    }
}
